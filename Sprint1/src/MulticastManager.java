public class MulticastManager {
    private HeartbeatSender heartbeatSender;
    private HeartbeatReceiver heartbeatReceiver;
    private SyncRequester syncRequester;

    public MulticastManager() {
        // Instancia as threads
        heartbeatSender = new HeartbeatSender();
        heartbeatReceiver = new HeartbeatReceiver();
        syncRequester = new SyncRequester();

        // Inicia as threads
        heartbeatSender.start();
        heartbeatReceiver.start();
        syncRequester.start();

        System.out.println("Threads iniciadas.");
    }

    public static void main(String[] args) {
        // Cria a instância do elemento principal, que lança as threads automaticamente
        new MulticastManager();
    }
}
