import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class LeaderNode extends Node implements LeaderCommunication, Serializable {
    private final Set<String> receivedAcks = new HashSet<>();
    private final Set<String> elementos = new HashSet<>();
    private final MessageList messageList = new MessageList();
    private DocumentManager file = new DocumentManager();
    private final Map<String, Long> lastResponseTime = new HashMap<>();
    private static final long TIMEOUT = 15000; // 15 seconds

    public LeaderNode() throws RemoteException {
        super(true);
        UnicastRemoteObject.exportObject(this, 0);
        try {
            LocateRegistry.createRegistry(1099);
            Naming.rebind("Leader", this);
            System.out.println("Líder registado");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkTimeouts() {
        long currentTime = System.currentTimeMillis();
        List<String> removedElements = new ArrayList<>();

        for (String uuid : elementos) {
            Long lastResponse = lastResponseTime.get(uuid);
            if (lastResponse == null || (currentTime - lastResponse) > TIMEOUT) {
                System.out.println("Elemento " + uuid + " removido por timeout.");
                removedElements.add(uuid);
            }
        }

        elementos.removeAll(removedElements);
    }

    @Override
    public void start() {
        new Thread(() -> {
            Message heartbeatMessage = new Message("HEARTBEAT");

            while (true) {
                try {
                    MessageList tempMessageList = new MessageList();
                    tempMessageList.addMessage(heartbeatMessage);

                    SendTransmitter transmitter = new SendTransmitter(MULTICAST_ADDRESS, PORT, tempMessageList);
                    transmitter.start();

                    System.out.println("Heartbeat enviado");

                    checkTimeouts();

                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    System.out.println("Erro ao enviar heartbeat: " + e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }


    @Override
    public void sendAck(String uuid, String messageType) throws RemoteException {
        System.out.println("ACK recebido via RMI: " + uuid + " para mensagem do tipo: " + messageType);
        lastResponseTime.put(uuid, System.currentTimeMillis());

        switch (messageType) {
            case "HEARTBEAT":
                // Lógica específica para HEARTBEAT
                System.out.println("Heartbeat ACK recebido de " + uuid);
                break;
            case "COMMIT":
                receivedAcks.add(uuid);
                if (receivedAcks.size() > elementos.size() / 2) {
                    sendCommit();
                    receivedAcks.clear();
                }
                break;
            // Adicione outros casos conforme necessário
            default:
                System.out.println("Tipo de mensagem desconhecido: " + messageType);
        }
    }

    @Override
    public void uploadFile(Document file) throws RemoteException {
        System.out.println("Ficheiro enviado pelo cliente: " + file);
        this.file.saveFile(file);
        this.messageList.addMessage(new Message("FILE", file));
        SendTransmitter transmitter = new SendTransmitter(MULTICAST_ADDRESS, PORT, messageList);
        transmitter.start();
        System.out.println("Ficheiro enviado por multicast para os membros.");
    }

    private void sendCommit() {
        System.out.println("Enviando commit para todos os membros...");
        MessageList messageList = new MessageList();
        messageList.addMessage(new Message("COMMIT"));
        SendTransmitter transmitter = new SendTransmitter(MULTICAST_ADDRESS, PORT, messageList);
        transmitter.start();
    }
    @Override
    public List<Document> getFileList() throws RemoteException {
        return file.listFiles();
    }

    public void sendSetup(String uuid) throws RemoteException {
        elementos.add(uuid);
        lastResponseTime.put(uuid, System.currentTimeMillis());
        System.out.println("A enviar ficheiros de setup para o elemento - " + uuid);

        // Create a new message list with the current file list
        MessageList setupMessageList = new MessageList();
        List<Document> currentFiles = file.listFiles();
        for (Document file : currentFiles) {
            setupMessageList.addMessage(new Message("FILE", file));
        }

        SendTransmitter transmitter = new SendTransmitter(MULTICAST_ADDRESS, PORT, setupMessageList);
        transmitter.start();
    }
}