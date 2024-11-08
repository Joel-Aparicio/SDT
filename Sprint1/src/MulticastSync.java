
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastSync {

    // Endereço Multicast e Porta
    private static final String MULTICAST_GROUP = "224.0.0.1";
    private static final int PORT = 5000;

    // Envia Heartbeats
    static class HeartbeatSender extends Thread {
        @Override
        public void run() {
            try (MulticastSocket socket = new MulticastSocket()) {
                InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
                while (true) {
                    String message = "HEARTBEAT";
                    byte[] buffer = message.getBytes();
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                    socket.send(packet);
                    System.out.println("Heartbeat enviado para " + MULTICAST_GROUP);
                    Thread.sleep(5000); // Intervalo de 5 segundos
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Recebe Heartbeats e processa pedidos de sincronização
    static class HeartbeatReceiver extends Thread {
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

    // Envia pedidos de sincronização
    static class SyncRequester extends Thread {
        @Override
        public void run() {
            try (MulticastSocket socket = new MulticastSocket()) {
                InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
                while (true) {
                    String syncRequest = "SYNC_REQUEST";
                    byte[] buffer = syncRequest.getBytes();
                    DatagramPacket syncPacket = new DatagramPacket(buffer, buffer.length, group, PORT);
                    socket.send(syncPacket);
                    System.out.println("Pedido de sincronização enviado.");
                    Thread.sleep(10000); // Intervalo de 10 segundos
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        // Inicia as threads
        new HeartbeatSender().start();
        new HeartbeatReceiver().start();
        new SyncRequester().start();
    }
}
