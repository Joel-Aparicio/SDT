import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.List;

public class SendTransmitter extends Thread {
    private final String multicastAddress;
    private final int port;
    private final MessageList messages;

    public SendTransmitter(String multicastAddress, int port, MessageList messageList) {
        this.multicastAddress = multicastAddress;
        this.port = port;
        this.messages = messageList;
    }


    public void run() {
        int messageIndex = 0;
        List<Message> lista = messages.getMessages();
        while (!lista.isEmpty()) {
            sendMessage(lista.get(messageIndex));
            lista.remove(messageIndex);
            if(lista.size() != 0) {
                messageIndex = (messageIndex + 1) % lista.size();
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessage(Message message) {
        try (MulticastSocket socket = new MulticastSocket()) {
            InetAddress group = InetAddress.getByName(multicastAddress);
            byte[] buffer = serializeMessage(message);

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
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
