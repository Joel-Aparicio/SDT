
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class SyncRequester extends Thread {
    private static final String MULTICAST_GROUP = "224.0.0.1";
    private static final int PORT = 5000;

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
