import java.rmi.Naming;
import java.util.Scanner;

public class ClientNode {

    public static void main(String[] args) {
        try {
            // Localiza o objeto remoto
            LeaderCommunication leader = (LeaderCommunication) Naming.lookup("rmi://localhost/Leader");
            Scanner scanner = new Scanner(System.in);

            System.out.println("Enviar um novo ficheiro:");
            System.out.print("Título do ficheiro: ");
            String title = scanner.nextLine();
            System.out.print("Versão do ficheiro: ");
            int version = Integer.parseInt(scanner.nextLine());
            System.out.print("Conteúdo do ficheiro: ");
            String content = scanner.nextLine();

            // Cria um objeto File
            Document newFile = new Document(version, title, content);

            // Envia o ficheiro para o líder
            leader.uploadFile(newFile);

            System.out.println("Ficheiro enviado com sucesso!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
