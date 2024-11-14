import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HeartbeatSender extends Thread {
    private static final String MULTICAST_GROUP = "224.0.0.1";
    private static final int PORT = 5000;
    private final Map<InetAddress, Boolean> ackResponses = new ConcurrentHashMap<>();
    private int totalNodes = 3; // Exemplo de número de nós na rede

    @Override
    public void run() {
        try (MulticastSocket socket = new MulticastSocket()) {
            InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
            while (true) {
                // Gerar uma nova versão de documento
                List<String> documents = getDocumentList();
                String documentData = "NEW_VERSION: [" + String.join(", ", documents) + "]";
                byte[] buffer = documentData.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                socket.send(packet);
                System.out.println("Nova versão de documento enviada para " + MULTICAST_GROUP);

                // Limpar respostas anteriores e aguardar ACKs
                ackResponses.clear();
                Thread.sleep(5000); // Intervalo para receber ACKs

                if (ackResponses.size() > totalNodes / 2) {
                    // Se a maioria enviou ACKs, envia commit
                    String commitMessage = "COMMIT";
                    byte[] commitBuffer = commitMessage.getBytes();
                    packet = new DatagramPacket(commitBuffer, commitBuffer.length, group, PORT);
                    socket.send(packet);
                    System.out.println("Commit enviado após receber ACKs da maioria.");
                }

                Thread.sleep(10000); // Intervalo de 10 segundos antes do próximo envio
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void receiveAck(InetAddress address) {
        ackResponses.put(address, true);
    }

    private List<String> getDocumentList() {
        // Gerar uma nova versão de documentos, pode incluir um contador ou versão
        List<String> documents = new ArrayList<>();
        long timestamp = System.currentTimeMillis();
        documents.add("{id: 1, title: 'Doc1', content: 'Updated Content of Doc1 - Version " + timestamp + "'}");
        documents.add("{id: 2, title: 'Doc2', content: 'Updated Content of Doc2 - Version " + timestamp + "'}");
        return documents;
    }

}


