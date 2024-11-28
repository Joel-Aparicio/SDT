import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessageList {
    private final List<Message> messages = Collections.synchronizedList(new ArrayList<>());

    public synchronized void addMessage(Message message) {
            messages.add(message);
        notify();
    }

    public synchronized void removeMessage(Message message) {
        messages.remove(message);

    }

    public synchronized Message getMessage(int index) {
        return messages.get(index);
    }

    public synchronized int size() {
        return messages.size();
    }

    public synchronized boolean isEmpty() {
        return messages.isEmpty();
    }

    public List<Message> getMessages() {
            return new ArrayList<>(messages);

    }

}
