import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.UUID;

public class Node {

    protected static final String MULTICAST_ADDRESS = "230.0.0.0"; // Endereço Multicast
    protected static final int PORT = 8888; // Porta Multicast

    UUID uuid = UUID.randomUUID();
    String randomUUIDString = uuid.toString();

    private final boolean isLeader;

    public Node(boolean isLeader) throws RemoteException {
        this.isLeader = isLeader;
    }

    public void start() {
        if (isLeader) {
            System.out.println("Este nó é o líder. Inicie classe LeaderNode.");
        } else {
            ListenTransmitter listener = new ListenTransmitter(MULTICAST_ADDRESS, PORT, this);
            listener.start();
            try {
                LeaderCommunication leader = (LeaderCommunication) Naming.lookup("rmi://localhost/Leader");
                leader.sendSetup(randomUUIDString);
            } catch (MalformedURLException | NotBoundException | RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public String getUUID() {
        return randomUUIDString;
    }

    private void sendMessage(Message message) {
        try (MulticastSocket socket = new MulticastSocket()) {
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            byte[] buffer = serializeMessage(message);

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            socket.send(packet);
            System.out.println("Mensagem enviada: " + message.getType());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] serializeMessage(Message message) {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) {
            objectStream.writeObject(message);
            return byteStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
}