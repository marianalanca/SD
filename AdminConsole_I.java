import java.rmi.*;

public interface AdminConsole_I extends Remote{
    
    public void notify_state(String notification) throws RemoteException;
    
}
