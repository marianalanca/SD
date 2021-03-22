//package calculator;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;


public interface Table_I extends Remote{
    
    public String getTableID() throws RemoteException;

    public void setTableID(String tableID) throws RemoteException;

    public List<Voter> getTableMembers() throws RemoteException;

    public void setTableMembers(List<Voter> tableMembers) throws RemoteException;
}
