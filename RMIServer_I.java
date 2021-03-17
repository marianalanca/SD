
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public interface RMIServer_I extends Remote {

      public Voter searchVoter(String username) throws RemoteException;

      public void loginAdmin(AdminConsole admin) throws RemoteException;

      public void loginMulticastServer(MulticastServer multicastServer) throws RemoteException;

      public Election searchElection(String title) throws RemoteException;

      public Voter searchVoterCc(String cc_number)throws RemoteException;
      public Voter searchUser(String username, String password) throws RemoteException;
      //public String login(String message)throws RemoteException;
      public boolean addElection(Election election)throws RemoteException;
      public void addVoter(Voter voter)throws RemoteException;
      public boolean createVoter(String username, String role, String department, String contact, String address, String cc_number, Calendar cc_expiring, String password)throws RemoteException;
      public boolean voterVotes(String username,String title, String candidateName, String voteLocal)  throws RemoteException;

      public boolean createElection(String title,Calendar beggDate,Calendar endDate,String department, List<Type> allowedVoters)throws RemoteException;
      
}