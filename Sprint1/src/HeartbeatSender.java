import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class HeartbeatSender extends Thread {
    private static final String MULTICAST_GROUP = "224.0.0.1";
    private static final int PORT = 5000;

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
