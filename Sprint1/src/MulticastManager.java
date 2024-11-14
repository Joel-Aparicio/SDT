public class MulticastManager {
    private boolean isLeader;
    private HeartbeatSender heartbeatSender;
    private HeartbeatReceiver heartbeatReceiver;
    private SyncRequester syncRequester;

    public MulticastManager(boolean isLeader) {
        this.isLeader = isLeader;

        if (isLeader) {
            heartbeatSender = new HeartbeatSender();
            heartbeatSender.start();
            System.out.println("Líder: enviando heartbeats.");
            heartbeatReceiver = new HeartbeatReceiver();
            syncRequester = new SyncRequester();
            heartbeatReceiver.start();
            syncRequester.start();
        } else {
            heartbeatReceiver = new HeartbeatReceiver();
            syncRequester = new SyncRequester();
            heartbeatReceiver.start();
            syncRequester.start();
            System.out.println("Elemento não-líder: recebendo heartbeats e enviando pedidos de sincronização.");
        }
    }

    public static void main(String[] args) {
        boolean isLeader = true;
        new MulticastManager(isLeader);
    }
}
