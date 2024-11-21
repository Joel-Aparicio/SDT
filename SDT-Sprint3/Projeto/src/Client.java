import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        try {
            // Localiza o objeto remoto
            LeaderInterface leader = (LeaderInterface) Naming.lookup("rmi://localhost/Leader");

            Scanner scanner = new Scanner(System.in);

            System.out.println("Enviar um novo ficheiro:");
            System.out.print("Título do ficheiro: ");
            String title = scanner.nextLine();
            System.out.print("Versão do ficheiro: ");
            int version = Integer.parseInt(scanner.nextLine());
            System.out.print("Conteúdo do ficheiro: ");
            String content = scanner.nextLine();

            // Cria um objeto File
            File newFile = new File(version, title, content);

            // Envia o ficheiro para o líder
            leader.uploadFile(newFile);

            System.out.println("Ficheiro enviado com sucesso!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
