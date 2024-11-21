import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.List;

public class ListenTransmitter extends Thread {
    private final String multicastAddress;
    private final int port;
    private final Element node;
    private FileManager fileManager;
    private File tempFile;

    public ListenTransmitter(String multicastAddress, int port, Element node) {
        this.multicastAddress = multicastAddress;
        this.port = port;
        this.fileManager =  new FileManager();
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

                if(receivedMessage.getType().equals("FILE")) {
                    tempFile = receivedMessage.getFile();
                    String uuid = java.util.UUID.randomUUID().toString();
                    try {
                        LeaderInterface leader = (LeaderInterface) Naming.lookup("rmi://localhost/Leader");
                        leader.sendAck(uuid);
                        System.out.println("ACK enviado via RMI: " + uuid);
                    } catch (NotBoundException | RemoteException e) {
                        e.printStackTrace();
                    }

                }

                if(receivedMessage.getType().equals("HEARTBEAT")) {
                    System.out.println("Recebi um heartbeat!");
                }

                if (receivedMessage.getType().equals("COMMIT")) {
                    System.out.println("Commit recebido: A atualizar versão do documento.");
                    fileManager.saveFile(tempFile);
                }


            }
        } catch (IOException e) {
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
