
import java.rmi.*;
import java.util.Calendar;
import java.util.List;


public interface RMIServer_I extends Remote {
     
      /** {@inheritDoc}
       * @param username the username you want to search 
       *  @return a boolean if the operation was sucessfull
       * @throws RemoteException
       */
      public Voter searchVoter(String username) throws RemoteException;

      /** {@inheritDoc}
       * @param admin the admin that is going to be added and is operational
       * 
       * @throws RemoteException
       */
      public void loginAdmin(AdminConsole_I admin) throws RemoteException;

      /** {@inheritDoc}
       * @param multicastServer the multicastServer that is on and in the network 
       * Will notify all admin consoles
       * @return null if it was added sucessfully or the "supposed MulticastServer value" it should have
       * @throws RemoteException
       */
      public MulticastServer loginMulticastServer(MulticastServer multicastServer) throws RemoteException;
      
      /** {@inheritDoc}
       * @param admin that is not going to be operational
       * 
       * @throws RemoteException
       */
      public void logoutAdmin(AdminConsole_I admin) throws RemoteException;
      
      /** {@inheritDoc}
       * @param title the title of the election you want to find
       * @return the election if it was successfull or null if the opposite
       * @throws RemoteException
       */
      public Election searchElection(String title) throws RemoteException;
      
      /** {@inheritDoc}
       * @param department the department where the election is happenning
       * @param role the type of voter that are allowed in the election 
       * @return the election if it was successfull or null if the opposite 
       * @throws RemoteException
       */
      public List<Election> searchElectionbyDepRole(String department, Type role) throws RemoteException;

      /** {@inheritDoc}
       * @param cc_number the cc_number of the user
       * 
       * @return null if it hasnt been found one with that value
       * @throws RemoteException
       */
      public Voter searchVoterCc(String cc_number)throws RemoteException;
      
      /** {@inheritDoc}
       * @param username the username of the voter
       * @param password the password of the voter with that username
       * 
       * @return null if it hasnt been found one with that value
       * @throws RemoteException
       */
      public Voter searchUser(String username, String password) throws RemoteException;
      //public String login(String message)throws RemoteException;
      
      /** {@inheritDoc}
       * Adds the election to the List
       * @param election
       * @return boolean depending if it was successful
       * @throws RemoteException
       */
      public boolean addElection(Election election)throws RemoteException;
      
      /** {@inheritDoc}
       * Adds the voter to the List voters
       * @param voter
       * @throws RemoteException
       */
      public void addVoter(Voter voter)throws RemoteException;
      
      
      /** {@inheritDoc}
       * It will create a voter (based on the Voter constructor)
       * @param username
       * @param department
       * @param contact
       * @param address
       * @param cc_number
       * @param cc_expiring
       * @param password
       * @param type
       * @return if it was successful
       * @throws RemoteException
       */
      public boolean createVoter(String username, String department, String contact, String address, String cc_number, Calendar cc_expiring, String password,Type type)  throws RemoteException;
      
      /** {@inheritDoc}
       * 
       * It receives the voter username, the title of the election and the candidate that is going to vote for
       * returns if there was a problem in the voting
       * Server e admins
       * @param username
       * @param title
       * @param candidateName
       * @param voteLocal
       * @return if successful true else false
       * @throws RemoteException
       */
      public boolean voterVotes(String username,String title, String candidateName, String voteLocal)  throws RemoteException;
      
      /** {@inheritDoc}
       *
       * It receives the voter username, the title of the election and the candidate that is going to vote for
       * returns if there was a problem in the voting
       * Server e admins
       * @param username
       * @param title
       * @param candidateName
       * @param voteLocal
       * @return
       * @throws RemoteException
       */
      public boolean voterVotesAdmin(String username,String title, String candidateName, String voteLocal)  throws RemoteException;
      public void setElections(List<Election> elections) throws RemoteException;
      
      /** {@inheritDoc}
       * Election constructor
       * Creates Election
       * @param title
       * @param description
       * @param beggDate
       * @param endDate
       * @param department
       * @param allowedVoters
       * @return if successfull returns true else false
       * @throws RemoteException
       */
      public boolean createElection(String title, String description, Calendar beggDate,Calendar endDate,String department, List<Type> allowedVoters)throws RemoteException;
      
      /** {@inheritDoc}
       * Gets the Election List
       * @throws RemoteException
       */
      public List<Election> getElections() throws RemoteException;
      public void setVoterList(List<Voter> voterList) throws RemoteException;
      
      /** {@inheritDoc}
       * Gets the Voter List
       * @throws RemoteException
       */
      public List<Voter> getVoterList() throws RemoteException;
      
      /** {@inheritDoc}
       * Creates Candidate and adds it to the proper election
       * @param members
       * @param name
       * @param title
       * @param type
       * @return
       * @throws RemoteException
       */
      public boolean createCandidate(List<Voter> members, String name,String title,Type type) throws RemoteException;
      
      /** {@inheritDoc}
       * @param title the title of the election
       * @param candidate the candidate to add to the election
       * @return true if successful
       * @throws RemoteException
       */
      public boolean removeCandidate(String title, String candidateName) throws RemoteException;
      
      /** {@inheritDoc}
       * @param title the title of the election
       * @param candidate the candidate to add to the election
       * @return true if successful
       * @throws RemoteException
       */
      public boolean addCandidate(String title,Candidates candidate) throws RemoteException;
      
      /** {@inheritDoc}
       * @param original info Election
       * @param newInfo Election
       * 
       * switches to the new election info
       * 
       * @return boolean if it was possible to switch
       * @throws RemoteException
       */
      public boolean switchElection(Election oriElection, Election newInfo) throws RemoteException;
      
      /** {@inheritDoc}
       * @param original info Voter
       * @param newInfo Voter
       * 
       * Switches to the new User info
       * 
       * @return boolean if it was possible to switch
       * @throws RemoteException
       */
      public boolean switchUser(Voter oriVoter, Voter newInfo) throws RemoteException;
      
      /** {@inheritDoc}
       * @param table the table that you wish to add 
       * @param election the election that the table is added
       * 
       * @return boolean depending if it was successful or not
       * @throws RemoteException
       */
      public boolean addTableElection(MulticastServer table, Election election)throws RemoteException;
      
      /** {@inheritDoc}
       * @param table the table that you wish to remove
       * @param election the election that the table is removed
       * 
       * @return boolean depending if it was successful or not
       * 
       * @throws RemoteException
       */
      public boolean removeTableElection(MulticastServer table, Election election)throws RemoteException;
      
      /** {@inheritDoc}@param table the table to callback and remove the member
       * @param member the voter to be removed as a member
       * @return false if something wrong happened
       * @throws RemoteException
       */
      public boolean removeVoterTable(MulticastServer table, Voter member) throws RemoteException;
      
      /** {@inheritDoc}
       * @param table the table to callback and add the member
       * @param member the voter to be added as a member
       * @return false if something wrong happened
       * @throws RemoteException
       */
      public boolean addVoterTable(MulticastServer table, Voter member)  throws RemoteException;
      
      /** {@inheritDoc}
       * @param id the id of the table 
       * @returns the table if it was successful or null if it wasn't
       * @throws RemoteException
       */
      public MulticastServer searchTable(String id) throws RemoteException;
      
      /** {@inheritDoc}
       * @param department the name of the department
       * It searches the table by its department that are on the OnServers
       * @return null if nothing has been found or the table if successfull
       * @throws RemoteException
       */
      public MulticastServer searchTableDept(String department) throws RemoteException;
      
      /** {@inheritDoc}
       * Adds a member from an election list
       * @param election
       * @param nome
       * @param member
       * @return false if there was an error finding the member or the list
       * @throws RemoteException
       */
      public boolean addMembroToLista(Election election, String nome,Voter member) throws RemoteException;
      
      /** {@inheritDoc}
       * Removes a member from an election list
       * @return false if there was an error finding the member or the list
       * @throws RemoteException
       */
      public boolean removeMembroToLista(Election election, String nome,Voter member) throws RemoteException;
      
      /** {@inheritDoc}
       * @param multicastServer the multicastServer is removed from the list of onServers
       * Will notify all adminConsoles it left
       * @throws RemoteException
       */
      public void logoutMulticastServer(MulticastServer multicastServer) throws RemoteException;
      
      /** {@inheritDoc}
       * @param state the state wish to obtain
       * @param type the type of the Election 
       * @return a List of all the election that has the state and the type
       * @throws RemoteException
       */
      public List<Election> stateElections(State state, Type type) throws RemoteException;
      
      /** {@inheritDoc}
       * @param table the table that you want to find the possible elections you can vote
       * @return the list of possible Elections that the Table can vote
       * @throws RemoteException
       */
      public List<Election> tablesElections(MulticastServer table) throws RemoteException;
      
      /** {@inheritDoc}
       * @return onServers - list of all On Tables
       * @throws RemoteException
       */
       public List<MulticastServer> getOnServers() throws RemoteException;
      
       /** {@inheritDoc}
       * @return servers - list of all servers that existed
       * @throws RemoteException
       */
       public List<MulticastServer> getServers() throws RemoteException;
}