import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashSet;
import java.util.Set;

public class PacketReceiverVerifier extends Thread {
    private static final String MULTICAST_GROUP = "224.0.0.1";
    private static final int PORT = 5000;
    private final Set<Integer> receivedPackets = new HashSet<>();
    private final int totalExpectedPackets;

    public PacketReceiverVerifier(int totalExpectedPackets) {
        this.totalExpectedPackets = totalExpectedPackets;
    }

    @Override
    public void run() {
        System.out.println("PacketReceiverVerifier iniciado...");
        try (MulticastSocket socket = new MulticastSocket(PORT)) {
            InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
            socket.joinGroup(group);

            while (receivedPackets.size() < totalExpectedPackets) {
                byte[] buffer = new byte[256];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Pacote recebido: " + message);

                // Supondo que a mensagem contenha um identificador de pacote
                if (message.startsWith("PACKET")) {
                    int packetId = extractPacketId(message);
                    receivedPackets.add(packetId);
                    System.out.println("Pacote " + packetId + " registrado.");
                }
            }

            System.out.println("Todos os pacotes esperados foram recebidos!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int extractPacketId(String message) {
        // Extrai o ID do pacote da mensagem (ajuste conforme o formato da mensagem)
        try {
            return Integer.parseInt(message.split(":")[1].trim());
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.err.println("Erro ao extrair o ID do pacote: " + message);
            return -1; // ID inválido
        }
    }
}
