import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class ElementLider extends Element implements LeaderInterface, Serializable {
    private final Set<String> receivedAcks = new HashSet<>();
    //TODO SETUP REGISTAR ELEMENTOS NOVOS
    private final Set<String> elementos = new HashSet<>();
    private final MessageList messageList = new MessageList();
    private FileManager fileManager = new FileManager();

    public ElementLider() throws RemoteException {
        super( true); // Define isLeader como true
        UnicastRemoteObject.exportObject(this, 0);
        try {
            LocateRegistry.createRegistry(1099);
            Naming.rebind("Leader", this);
            System.out.println("Líder registrado no RMI com o nome 'Leader'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        new Thread(() -> {
            // Cria a mensagem de heartbeat
            Message heartbeatMessage = new Message("HEARTBEAT");

            // Envia o heartbeat diretamente sem armazenar na lista
            while (true) {
                try {
                    // Cria uma nova lista temporária de mensagens a cada iteração
                    MessageList tempMessageList = new MessageList();
                    tempMessageList.addMessage(heartbeatMessage);

                    // Envia o heartbeat por multicast
                    SendTransmitter transmitter = new SendTransmitter(MULTICAST_ADDRESS, PORT, tempMessageList);
                    transmitter.start();

                    System.out.println("Heartbeat enviado para os membros.");

                    // Aguarda 5 segundos antes de enviar o próximo heartbeat
                    Thread.sleep(5000); // Espera 5 segundos
                } catch (InterruptedException e) {
                    System.out.println("Erro ao enviar heartbeat: " + e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }


    @Override
    public void sendAck(String uuid) throws RemoteException {
        System.out.println("ACK recebido via RMI: " + uuid);
        receivedAcks.add(uuid);
        if (receivedAcks.size() > elementos.size() / 2) {
            sendCommit();
            receivedAcks.clear();
        }
    }

    public void sendSetup(String uuid) throws RemoteException{
        elementos.add(uuid);
        System.out.println("A enviar ficheiros de setup para o elemento - " + randomUUIDString);
        SendTransmitter transmitter = new SendTransmitter(MULTICAST_ADDRESS, PORT, messageList);
        transmitter.start();
    }

    @Override
    public void uploadFile(File file) throws RemoteException {
        System.out.println("Ficheiro enviado pelo cliente: " + file);
        this.fileManager.saveFile(file);
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

}
