
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastSync {
    public static void main(String[] args) {
        // Inicia as threads
        new HeartbeatSender().start();
        new HeartbeatReceiver().start();
        new SyncRequester().start();
    }
}
