
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class HeartbeatReceiver extends Thread {
    private static final String MULTICAST_GROUP = "224.0.0.1";
    private static final int PORT = 5000;
    private static final int TOTAL_EXPECTED_PACKETS = 5;

    @Override
    public void run() {
        System.out.println("HeartbeatReceiver iniciado...");
        new PacketReceiverVerifier(TOTAL_EXPECTED_PACKETS).start(); // Inicia a verificação de pacotes

        System.out.println("HeartbeatReceiver iniciado...");
        try (MulticastSocket socket = new MulticastSocket(PORT)) {
            InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
            socket.joinGroup(group);

            System.out.println("A receber mensagens...");
            while (true) {
                byte[] buffer = new byte[256];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Mensagem recebida: " + message);

                if (message.startsWith("NEW_VERSION")) {
                    // Envia um ACK para o líder após receber a nova versão do documento
                    sendAck(packet.getAddress());
                    System.out.println("ACK enviado para o líder.");
                } else if (message.equals("SYNC_REQUEST")) {
                    // Responde ao pedido de sincronização com uma mensagem de sincronização
                    handleSyncRequest(packet.getAddress());
                    System.out.println("Pedido de sincronização recebido e respondido.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para enviar ACK após receber a nova versão do documento
    private void sendAck(InetAddress leaderAddress) {
        try (MulticastSocket socket = new MulticastSocket()) {
            String ackMessage = "ACK";
            DatagramPacket ackPacket = new DatagramPacket(ackMessage.getBytes(), ackMessage.length(), leaderAddress, PORT);
            socket.send(ackPacket);
            System.out.println("ACK enviado para: " + leaderAddress.getHostAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para responder ao pedido de sincronização
    private void handleSyncRequest(InetAddress leaderAddress) {
        // Responde com uma mensagem indicando que está pronto para sincronizar
        try (MulticastSocket socket = new MulticastSocket()) {
            String syncResponse = "SYNC_RESPONSE";
            DatagramPacket syncPacket = new DatagramPacket(syncResponse.getBytes(), syncResponse.length(), leaderAddress, PORT);
            socket.send(syncPacket);
            System.out.println("Resposta de sincronização enviada para: " + leaderAddress.getHostAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



