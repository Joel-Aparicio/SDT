public class Main {
    public static void main(String[] args) throws Exception {

        // Lider
        LeaderNode leaderNode = new LeaderNode();
        //Esperar 1 segundo para os elementos começarem antes do lider

        //Não Líder
        leaderNode.start();
        Thread.sleep(10000); // Delay para elementos entrarem de depois, apenas de teste
        Node memberNode1 = new Node(false);
        memberNode1.start();
        Node memberNode2 = new Node(false);
        memberNode2.start();


        Thread.sleep(20000);
        Node memberNode3 = new Node(false);
        memberNode3.start();
    }
}
