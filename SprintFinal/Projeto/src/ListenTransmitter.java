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

            byte[] buffer = new byte[256];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                Message receivedMessage = deserializeMessage(packet.getData(), packet.getLength());
                System.out.println("Mensagem recebida: " + receivedMessage.getType());

                processMessage(receivedMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendAckToLeader(String documentTitle) {
        try {
            LeaderCommunication leader = (LeaderCommunication) Naming.lookup("rmi://localhost/Leader");
            leader.sendAck(node.getUUID(), "FILE_ACK:" + documentTitle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processMessage(Message receivedMessage) {
        try {
            switch (receivedMessage.getType()) {
                case "COMMIT":
                    Document committedFile = (Document) receivedMessage.getFile();
                    if (committedFile != null) {
                        fileManager.saveFile(committedFile);
                        System.out.println("Commit recebido e arquivo permanente: " + committedFile.getTitle());
                    }
                    break;
                case "HEARTBEAT":
                    System.out.println("Heartbeat recebido.");
                    node.updateLastHeartbeat();
                    break;
                case "FILE":
                    Document receivedFile = (Document) receivedMessage.getFile();
                    if (receivedFile != null) {
                        fileManager.saveFile(receivedFile);
                        System.out.println("Arquivo sincronizado: " + receivedFile.getTitle());
                        sendAckToLeader(receivedFile.getTitle()); // Enviar ACK para o líder
                    }
                    break;
                case "VOTE_REQUEST":
                    System.out.println("Pedido de voto recebido.");
                    LeaderCommunication leader = (LeaderCommunication) Naming.lookup("rmi://localhost/Leader");
                    leader.sendAck(node.getUUID(), "VOTE");
                    break;
                case "VOTE":
                    System.out.println("Voto recebido.");
                    break;
                default:
                    System.out.println("Mensagem desconhecida: " + receivedMessage.getType());
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