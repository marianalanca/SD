import java.rmi.*;

public interface AdminConsole_I extends Remote{

    /**
     * prints a notification about the state (on or off) of a table
     * @param notification a string with a notification
     * @throws RemoteException
     */
    public void notify_state(String notification) throws RemoteException;
    
}
