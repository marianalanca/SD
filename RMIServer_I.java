
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
      public boolean createVoter(String username, String department, String contact, String address, String cc_number, Calendar cc_expiring, String password,Type type)  throws RemoteException;
      public boolean voterVotes(String username,String title, String candidateName, String voteLocal)  throws RemoteException;
      public boolean voterVotesAdmin(String username,String title, String candidateName, String voteLocal)  throws RemoteException;
      public void setElections(List<Election> elections) throws RemoteException;
      public boolean createElection(String title, String description, Calendar beggDate,Calendar endDate,String department, List<Type> allowedVoters)throws RemoteException;
      public List<Election> getElections() throws RemoteException;
      public void setVoterList(List<Voter> voterList) throws RemoteException;
      public List<Voter> getVoterList() throws RemoteException;
      public boolean createCandidate(List<Voter> members, String name,String title,Type type) throws RemoteException;
      public boolean removeCandidate(String title, String candidateName) throws RemoteException;
      public boolean addCandidate(String title,Candidates candidate) throws RemoteException;
      public boolean switchElection(Election oriElection, Election newInfo) throws RemoteException;
      public boolean switchUser(Voter oriVoter, Voter newInfo) throws RemoteException;
      public boolean addTableElection(MulticastServer table, Election election)throws RemoteException;
      public boolean removeTableElection(MulticastServer table, Election election)throws RemoteException;
      public boolean removeVoterTable(MulticastServer table, Voter member) throws RemoteException;
      public boolean addVoterTable(MulticastServer table, Voter member)  throws RemoteException;
      public MulticastServer searchTable(String id) throws RemoteException;
      public boolean addMembroToLista(Election election, String nome,Voter member) throws RemoteException;
      public boolean removeMembroToLista(Election election, String nome,Voter member) throws RemoteException;
      public void logoutMulticastServer(MulticastServer multicastServer) throws RemoteException;
      public List<Election> stateElections(State state, Type type) throws RemoteException;
      public List<Election> tablesElections(MulticastServer table) throws RemoteException;
}