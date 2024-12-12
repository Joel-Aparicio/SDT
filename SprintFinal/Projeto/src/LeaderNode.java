import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class LeaderNode extends Node implements LeaderCommunication, Serializable {
    private final Set<String> receivedAcks = new HashSet<>();
    private final Map<String, Set<String>> fileAcks = Collections.synchronizedMap(new HashMap<>());
    private final Set<String> elementos = new HashSet<>();
    private final MessageList messageList = new MessageList();
    private final DocumentManager file = new DocumentManager();
    private final Map<String, Long> lastResponseTime = new HashMap<>();
    private static final long TIMEOUT = 15000; // 15 seconds
    private final List<String> logEntries = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) throws Exception {
        LeaderNode leaderNode = new LeaderNode();
        leaderNode.start();
    }

    private enum NodeState {
        FOLLOWER, CANDIDATE, LEADER
    }

    private NodeState state = NodeState.FOLLOWER;
    private int currentTerm = 0;
    private String votedFor = null;
    private final Map<String, Integer> votesReceived = new HashMap<>();

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
        for (String nodeId : removedElements) {
            handleNodeExit(nodeId);
        }

        elementos.removeAll(removedElements);
    }

    @Override
    public void start() {
        new Thread(() -> {
            while (true) {
                if (System.currentTimeMillis() - lastHeartbeatReceived > TIMEOUT) {
                    System.out.println("Líder não responde. Iniciando eleição de líder.");
                    performLeaderElection();
                }
                try {
                    Thread.sleep(1000);
                    if (state == NodeState.LEADER) {
                        Message heartbeatMessage = new Message("HEARTBEAT");
                        MessageList tempMessageList = new MessageList();
                        tempMessageList.addMessage(heartbeatMessage);

                        // Adicione commits pendentes ao heartbeat
                        synchronized (fileAcks) {
                            for (String documentTitle : fileAcks.keySet()) {
                                if (fileAcks.get(documentTitle).size() > elementos.size() / 2) {
                                    Document committedFile = file.getFile(documentTitle);
                                    if (committedFile != null) {
                                        tempMessageList.addMessage(new Message("COMMIT", committedFile));
                                        System.out.println("Commit incluído no heartbeat: " + documentTitle);
                                    }
                                }
                            }
                        }

                        SendTransmitter transmitter = new SendTransmitter(MULTICAST_ADDRESS, PORT, tempMessageList);
                        transmitter.start();

                        System.out.println("Heartbeat enviado com commits.");
                    }

                    checkTimeouts();
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    public void handleNodeExit(String nodeId) {
        elementos.remove(nodeId);
        System.out.println("Nó removido: " + nodeId);

        // Redistribute documents
        List<Document> allDocuments = file.listFiles();
        int nodeCount = elementos.size();
        if (nodeCount > 0) {
            int docsPerNode = allDocuments.size() / nodeCount;
            int extraDocs = allDocuments.size() % nodeCount;

            Iterator<String> nodeIterator = elementos.iterator();
            for (int i = 0; i < allDocuments.size(); ) {
                String targetNode = nodeIterator.next();

                int docsToAssign = docsPerNode + (extraDocs > 0 ? 1 : 0);
                extraDocs--;

                for (int j = 0; j < docsToAssign && i < allDocuments.size(); j++, i++) {
                    Document doc = allDocuments.get(i);
                    System.out.println("Redistribuindo documento: " + doc.getTitle() + " para nó: " + targetNode);

                    // Enviar mensagem FILE para o nó
                    Message fileMessage = new Message("FILE", doc);
                    MessageList tempMessageList = new MessageList();
                    tempMessageList.addMessage(fileMessage);

                    SendTransmitter transmitter = new SendTransmitter(MULTICAST_ADDRESS, PORT, tempMessageList);
                    transmitter.start();
                }

                if (!nodeIterator.hasNext()) {
                    nodeIterator = elementos.iterator();
                }
            }
        } else {
            System.out.println("Nenhum nó disponível para redistribuir documentos.");
        }
    }

    @Override
    public void sendAck(String uuid, String messageType) throws RemoteException {
        System.out.println("ACK recebido de " + uuid + " para mensagem do tipo " + messageType);
        lastResponseTime.put(uuid, System.currentTimeMillis());

        if (messageType.startsWith("FILE_ACK:")) {
            String documentTitle = messageType.split(":")[1];
            fileAcks.putIfAbsent(documentTitle, new HashSet<>());
            fileAcks.get(documentTitle).add(uuid);

            System.out.println("ACK recebido de " + uuid + " para o documento " + documentTitle);

            if (fileAcks.get(documentTitle).size() > elementos.size() / 2) {
                sendCommit(documentTitle); // Enviar commit
            }

            if (messageType.startsWith("VOTE_REQUEST")) {
                int term = extractTermFromMessage(messageType); // Implemente este método para obter o termo
                if (term > currentTerm) {
                    currentTerm = term;
                    votedFor = uuid;
                    System.out.println("Voto concedido a " + uuid);

                    Message voteMessage = new Message("VOTE", uuid);
                    SendTransmitter transmitter = new SendTransmitter(MULTICAST_ADDRESS, PORT, new MessageList());
                    transmitter.run();
                }
            } else if (messageType.equals("VOTE")) {
                votesReceived.merge(uuid, 1, Integer::sum);
                System.out.println("Voto recebido de " + uuid + ". Total: " + votesReceived.get(uuid));

                if (votesReceived.get(uuid) > elementos.size() / 2) {
                    state = NodeState.LEADER;
                    System.out.println("Novo líder eleito: " + uuid);
                }
            }
        }
    }

        @Override
        public void uploadFile (Document file) throws RemoteException {
            synchronized (logEntries) {
                logEntries.add("Upload: " + file.getTitle() + ", Version: " + file.getVersion());
            }
            System.out.println("Ficheiro enviado pelo cliente: " + file);
            this.file.saveFile(file);
            this.messageList.addMessage(new Message("FILE", file));
            SendTransmitter transmitter = new SendTransmitter(MULTICAST_ADDRESS, PORT, messageList);
            transmitter.start();
            System.out.println("Ficheiro enviado por multicast para os membros.");
        }

        @Override
        public List<Document> getFileList () throws RemoteException {
            return file.listFiles();
        }

        public void sendSetup (String uuid) throws RemoteException {
            elementos.add(uuid);
            lastResponseTime.put(uuid, System.currentTimeMillis());
            System.out.println("A enviar ficheiros de setup para o elemento - " + uuid);

            MessageList setupMessageList = new MessageList();
            List<Document> currentFiles = file.listFiles();
            for (Document file : currentFiles) {
                setupMessageList.addMessage(new Message("FILE", file));
            }

            SendTransmitter transmitter = new SendTransmitter(MULTICAST_ADDRESS, PORT, setupMessageList);
            transmitter.start();
        }

    public synchronized void performLeaderElection() {
        state = NodeState.CANDIDATE;
        currentTerm++;
        votedFor = getUUID(); // Votar em si mesmo
        votesReceived.clear();
        votesReceived.put(getUUID(), 1); // Contabilizar o próprio voto

        System.out.println("Iniciando eleição de líder no termo " + currentTerm);

        Message voteRequest = new Message("VOTE_REQUEST", getUUID());
        SendTransmitter transmitter = new SendTransmitter(MULTICAST_ADDRESS, PORT, new MessageList());
        transmitter.run();
    }

        private void sendCommit (String documentTitle){
            try {
                System.out.println("Enviando COMMIT para o documento: " + documentTitle);
                Document committedFile = file.getFile(documentTitle);

                if (committedFile != null) {
                    Message commitMessage = new Message("COMMIT", committedFile);
                    MessageList commitMessageList = new MessageList();
                    commitMessageList.addMessage(commitMessage);

                    SendTransmitter transmitter = new SendTransmitter(MULTICAST_ADDRESS, PORT, commitMessageList);
                    transmitter.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    private int extractTermFromMessage(String messageType) {
        try {
            if (messageType.startsWith("VOTE_REQUEST:")) {
                String[] parts = messageType.split(":");
                return Integer.parseInt(parts[1]); // Extrai e retorna o termo
            }
        } catch (NumberFormatException e) {
            System.err.println("Erro ao extrair o termo da mensagem: " + messageType);
        }
        return -1; // Retorna -1 em caso de erro
    }
}


