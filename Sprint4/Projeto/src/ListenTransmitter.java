import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.Naming;
import java.util.List;
import java.util.ArrayList;

public class ListenTransmitter extends Thread {
    private final String multicastAddress;
    private final int port;
    private final Node node;
    private DocumentManager fileManager;
    private List<Message> pendingUpdates = new ArrayList<>();
    private boolean isInitialSyncComplete = false;
    private Document tempFile;

    public ListenTransmitter(String multicastAddress, int port, Node node) {
        this.multicastAddress = multicastAddress;
        this.port = port;
        this.fileManager = new DocumentManager();
        this.node = node;
    }

    public void run() {
        try (MulticastSocket socket = new MulticastSocket(port)) {
            InetAddress group = InetAddress.getByName(multicastAddress);
            socket.joinGroup(group);

            // Perform initial synchronization
            performInitialSync();

            byte[] buffer = new byte[256];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                Message receivedMessage = deserializeMessage(packet.getData(), packet.getLength());
                System.out.println("Mensagem recebida: " + receivedMessage.getType());

                if (!isInitialSyncComplete) {
                    // Store pending updates during sync
                    if (receivedMessage.getType().equals("FILE") || receivedMessage.getType().equals("COMMIT")) {
                        pendingUpdates.add(receivedMessage);
                        continue;
                    }
                }

                processMessage(receivedMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void performInitialSync() {
        try {
            LeaderCommunication leader = (LeaderCommunication) Naming.lookup("rmi://localhost/Leader");
            List<Document> initialFiles = leader.getFileList();

            // Save initial files
            for (Document file : initialFiles) {
                fileManager.saveFile(file);
            }

            // Process pending updates
            processPendingUpdates();

            isInitialSyncComplete = true;
            System.out.println("Initial synchronization complete");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processPendingUpdates() {
        for (Message pendingMessage : pendingUpdates) {
            processMessage(pendingMessage);
        }
        pendingUpdates.clear();
    }

    private void processMessage(Message receivedMessage) {
        try {
            if (receivedMessage.getType().equals("FILE")) {
                // Store the file at the class level
                tempFile = receivedMessage.getFile();
                String uuid = java.util.UUID.randomUUID().toString();

                LeaderCommunication leader = (LeaderCommunication) Naming.lookup("rmi://localhost/Leader");
                leader.sendAck(uuid, "COMMIT");
                System.out.println("ACK enviado via RMI: " + uuid);
            }

            if (receivedMessage.getType().equals("HEARTBEAT")) {
                System.out.println("Recebi um heartbeat!");
                LeaderCommunication leader = (LeaderCommunication) Naming.lookup("rmi://localhost/Leader");
                leader.sendAck(node.getUUID(), "HEARTBEAT");
            }

            if (receivedMessage.getType().equals("COMMIT")) {
                // Now tempFile is accessible here
                System.out.println("Commit recebido: A atualizar versão do documento.");
                fileManager.saveFile(tempFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Message deserializeMessage(byte[] data, int length) {
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(data, 0, length);
             ObjectInputStream objectStream = new ObjectInputStream(byteStream)) {
            return (Message) objectStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}