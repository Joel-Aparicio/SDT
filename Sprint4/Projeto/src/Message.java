import java.io.Serializable;

public class Message implements Serializable {
    private String type;
    private Object file;

    public Message(String type) {
        this.type = type;
        this.file = null;
    }

    public Message(String type, Object file) {
        this.type = type;
        this.file = file;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public Document getFile() {
        return (Document) file;
    }
    public void setFile(Document file) {
        this.file = file;
    }
    public boolean isFileMessage() {
        return "FILE".equals(this.type);
    }
    public boolean isCommitMessage() {
        return "COMMIT".equals(this.type);
    }
}
