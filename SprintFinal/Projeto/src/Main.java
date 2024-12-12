import java.rmi.registry.LocateRegistry;

public class Main {
    public static void main(String[] args) throws Exception {

        try {
            LocateRegistry.createRegistry(1099);
        } catch (Exception e) {
        }
        // Inicializa o líder
        System.out.println("Inicializando o nó líder...");
        LeaderNode leaderNode = new LeaderNode();
        leaderNode.start();

        // Aguardar para adicionar nós seguidores
        Thread.sleep(5000);

        // Adicionar nós seguidores
        System.out.println("Adicionando nós seguidores...");
        Node memberNode1 = new Node(false);
        memberNode1.start();

        Node memberNode2 = new Node(false);
        memberNode2.start();

        Thread.sleep(10000);

        Node memberNode3 = new Node(false);
        memberNode3.start();

    }
}
