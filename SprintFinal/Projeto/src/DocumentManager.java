import java.util.ArrayList;
import java.util.List;

public class DocumentManager {
    private final List<Document> fileStorage = new ArrayList<>();

    //Verificar se já existe
    public synchronized boolean fileExists(Document file) {
        for (Document storedFile : fileStorage) {
            if (storedFile.getTitle().equals(file.getTitle())) {
                return true; // Arquivo com o mesmo nome encontrado
            }
        }
        return false; // Arquivo não encontrado
    }

    // Guardar
    public synchronized void saveFile(Document file) {
        if (fileExists(file))
        {
            updateFile(file);
            return;
        }
        fileStorage.add(file);
    }


    private void updateFile(Document file) {
        for (int i = 0; i < fileStorage.size(); i++) {
            Document storedFile = fileStorage.get(i);
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
    public synchronized List<Document> listFiles() {
        return new ArrayList<>(fileStorage); // Retorna uma cópia
    }

    public synchronized Document getFile(String title) {
        for (Document storedFile : fileStorage) {
            if (storedFile.getTitle().equals(title)) {
                return storedFile;
            }
        }
        return null; // Retorna null se não encontrar
    }
}

