import java.rmi.*;

public interface AdminConsole_I extends Remote{

    /**{@inheritDoc}
     * prints a notification about the state (on or off) of a table
     * @param notification a string with a notification
     * @throws RemoteException Remote Problem
     */
    public void notify_state(String notification) throws RemoteException;
    
}
