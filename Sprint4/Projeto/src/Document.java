import java.io.Serializable;
import java.util.Arrays;

public class Document implements Serializable {
    private final int version;
    private final String title;
    private final String content;

    public Document(int version, String title, String content) {
        this.version = version;
        this.title = title;
        this.content = content;
    }

    public Document(String title, int version, byte[] fileContent) {
        this.title = title;
        this.version = version;
        this.content = Arrays.toString(fileContent);
    }

    public int getVersion() {
        return version;
    }
    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "File{" +
                "version=" + version +
                ", content='" + content + '\'' +
                '}';
    }
}
