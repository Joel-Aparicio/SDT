
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class HeartbeatReceiver extends Thread {
    private static final String MULTICAST_GROUP = "224.0.0.1";
    private static final int PORT = 5000;

    @Override
    public void run() {
        try (MulticastSocket socket = new MulticastSocket(PORT)) {
            InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
            socket.joinGroup(group);

            System.out.println("A receber heartbeats e pedidos de sincronização...");
            while (true) {
                byte[] buffer = new byte[256];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Mensagem recebida: " + message);

                // Responde ao pedido de sincronização
                if (message.equals("SYNC_REQUEST")) {
                    respondToSync(packet.getAddress());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void respondToSync(InetAddress leaderAddress) {
        try (MulticastSocket socket = new MulticastSocket()) {
            String nodeData = "NODE_DATA: {id: 1, state: updated}";
            DatagramPacket responsePacket = new DatagramPacket(nodeData.getBytes(), nodeData.length(),
                    leaderAddress, PORT);
            socket.send(responsePacket);
            System.out.println("Dados enviados para o líder: " + leaderAddress.getHostAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
