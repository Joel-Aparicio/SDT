public class Main {
    public static void main(String[] args) throws Exception {

        // Lider
        ElementLider leaderNode = new ElementLider();
        //Esperar 1 segundo para os elementos começarem antes do lider

        //Não Líder
        leaderNode.start();
        Thread.sleep(10000); // Delay para elementos entrarem de depois, apenas de teste
        Element memberNode1 = new Element(false);
        memberNode1.start();
        Element memberNode2 = new Element(false);
        memberNode2.start();
    }
}
