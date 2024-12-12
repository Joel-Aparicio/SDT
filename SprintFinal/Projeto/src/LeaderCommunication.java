import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface LeaderCommunication extends Remote {
    void sendAck(String uuid, String messageType) throws RemoteException;
    void uploadFile(Document file) throws RemoteException;
    void sendSetup(String uuid) throws RemoteException;

    List<Document> getFileList() throws RemoteException;
}