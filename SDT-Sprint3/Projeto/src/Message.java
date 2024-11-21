import java.io.Serializable;

public class Message implements Serializable {
    private String type; // Tipo da mensagem, por exemplo, "file" ou "commit"
    private Object file; // Objeto File, se a mensagem for do tipo "file"

    // Construtor para uma mensagem do tipo commit (sem arquivo)
    public Message(String type) {
        this.type = type;
        this.file = null;
    }

    // Construtor para uma mensagem do tipo file (com arquivo)
    public Message(String type, Object file) {
        this.type = type;
        this.file = file;
    }

    // Getters e setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public File getFile() {
        return (File) file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public boolean isFileMessage() {
        return "FILE".equals(this.type);
    }

    public boolean isCommitMessage() {
        return "COMMIT".equals(this.type);
    }
}
