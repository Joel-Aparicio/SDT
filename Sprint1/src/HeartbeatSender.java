import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.net.MulticastSocket;

public class HeartbeatSender extends Thread {
    private static final String MULTICAST_GROUP = "224.0.0.1";
    private static final int PORT = 5000;

    @Override
    public void run() {
        try (MulticastSocket socket = new MulticastSocket()) {
            InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
            while (true) {
                List<String> documents = getDocumentList();
                String documentsData = String.join(", ", documents);

                String message = "DOCUMENT_LIST: [" + documentsData + "]";
                byte[] buffer = message.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);

                socket.send(packet);
                System.out.println("Lista de documentos enviada para " + MULTICAST_GROUP);
                Thread.sleep(5000); // Intervalo de 5 segundos
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    private List<String> getDocumentList() {
        List<String> documents = new ArrayList<>();
        documents.add("{id: 1, title: 'Doc1', content: 'Content of Doc1'}");
        documents.add("{id: 2, title: 'Doc2', content: 'Content of Doc2'}");
        documents.add("{id: 3, title: 'Doc3', content: 'Content of Doc3'}");
        return documents;
    }
}
