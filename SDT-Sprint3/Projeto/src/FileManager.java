import java.util.ArrayList;
import java.util.List;

public class FileManager {
    private final List<File> fileStorage = new ArrayList<>();

    //Verificar se já existe
    public synchronized boolean fileExists(File file) {
        for (File storedFile : fileStorage) {
            if (storedFile.getTitle().equals(file.getTitle())) {
                return true; // Arquivo com o mesmo nome encontrado
            }
        }
        return false; // Arquivo não encontrado
    }

    // Guardar
    public synchronized void saveFile(File file) {
        if (fileExists(file))
        {
            updateFile(file);
            return;
        }
        fileStorage.add(file);
    }

    private void updateFile(File file) {
        for (int i = 0; i < fileStorage.size(); i++) {
            File storedFile = fileStorage.get(i);
            if (storedFile.getTitle().equals(file.getTitle())) {
                //Código apenas de teste!
                if(storedFile.getVersion() >= file.getVersion())
                {
                    System.out.println("A versão do arquivo " + storedFile.getTitle() + " é a mesma ou superior V" + storedFile.getVersion());
                    return;
                }
                System.out.println("Arquivo " + storedFile.getTitle() + " foi atualizado da versão " + storedFile.getVersion() + " para a versão " + file.getVersion());
                fileStorage.set(i, file);
                return;
            }
        }
    }

    // Listar
    public synchronized List<File> listFiles() {
        return new ArrayList<>(fileStorage); // Retorna uma cópia
    }

    public File getFile(String title) {
        return null;
    }
}
