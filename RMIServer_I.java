
import java.rmi.*;
import java.util.Calendar;
import java.util.List;


public interface RMIServer_I extends Remote {
     




      

      /** {@inheritDoc}
       * @param username the username you want to search 
       * @return a boolean if the operation was sucessfull
       * @throws RemoteException Remote Problem
       */
      public Voter searchVoter(String username) throws RemoteException;

      /**
       * Changes the info needed
       * @param department the department 
       * @param update the data that needs to be updated
       * @return if it was successful
       * @throws RemoteException Remote Problem
       */
      public boolean updateServerData(String department, ServerData update) throws RemoteException;

      /** {@inheritDoc}
       * @param admin the admin that is going to be added and is operational
       * 
       * @throws RemoteException Remote Problem
       */
      public void loginAdmin(AdminConsole_I admin) throws RemoteException;

      /** {@inheritDoc}
       * @param multicastServer the multicastServer that is on and in the network 
       * Will notify all admin consoles
       * @return null if it was added sucessfully or the "supposed MulticastServer value" it should have
       * @throws RemoteException Remote Problem
       */
      public MulticastServer loginMulticastServer(MulticastServer multicastServer) throws RemoteException;
      
      /** {@inheritDoc}
       * @param admin that is not going to be operational
       * 
       * @throws RemoteException Remote Problem
       */
      public void logoutAdmin(AdminConsole_I admin) throws RemoteException;
      
      /** {@inheritDoc}
       * @param title the title of the election you want to find
       * @return the election if it was successfull or null if the opposite
       * @throws RemoteException Remote Problem
       */
      public Election searchElection(String title) throws RemoteException;
      
      /** {@inheritDoc}
       * @param department the department where the election is happenning
       * @param role the type of voter that are allowed in the election 
       * @return the election if it was successfull or null if the opposite 
       * @throws RemoteException Remote Problem
       */
      public List<Election> searchElectionbyDepRole(String department, Type role) throws RemoteException;

      /** {@inheritDoc}
       * @param cc_number the cc_number of the user
       * 
       * @return null if it hasnt been found one with that value
       * @throws RemoteException Remote Problem
       */
      public Voter searchVoterCc(String cc_number)throws RemoteException;
      
      /** {@inheritDoc}
       * @param username the username of the voter
       * @param password the password of the voter with that username
       * 
       * @return null if it hasnt been found one with that value
       * @throws RemoteException Remote Problem
       */
      public Voter searchUser(String username, String password) throws RemoteException;
      //public String login(String message)throws RemoteException;
      
      /** {@inheritDoc}
       * Adds the election to the List
       * @param election the election that needs to be added
       * @return boolean depending if it was successful
       * @throws RemoteException Remote Problem
       */
      public boolean addElection(Election election)throws RemoteException;
      
      /** {@inheritDoc}
       * Adds the voter to the List voters
       * @param voter the voter added to the list
       * @throws RemoteException Remote Problem
       */
      public void addVoter(Voter voter)throws RemoteException;
      
      
      /** {@inheritDoc}
       * It will create a voter (based on the Voter constructor)
       * @param username the username name
       * @param department the department
       * @param contact the contact number
       * @param address the address of the voter
       * @param cc_number the cc_number
       * @param cc_expiring the cc_expiring date
       * @param password the password
       * @param type the type of user he is
       * @return if it was successful
       * @throws RemoteException Remote Problem
       */
      public boolean createVoter(String username, String department, String contact, String address, String cc_number, Calendar cc_expiring, String password,Type type)  throws RemoteException;
      
      /** {@inheritDoc}
       * 
       * It receives the voter username, the title of the election and the candidate that is going to vote for
       * returns if there was a problem in the voting
       * Server e admins
       * @param username the name of the user
       * @param title the title of the election
       * @param candidateName the name of the candidate he wants to vote
       * @param voteLocal the Local where he votes
       * @return if successful true else false
       * @throws RemoteException Remote Problem
       */
      public boolean voterVotes(String username,String title, String candidateName, String voteLocal)  throws RemoteException;
      
      /** {@inheritDoc}
       *
       * It receives the voter username, the title of the election and the candidate that is going to vote for
       * returns if there was a problem in the voting
       * Server e admins
       * @param username the name of the user
       * @param title the title of the election
       * @param candidateName the name of the candidate he wants to vote
       * @param voteLocal the Local where he votes
       * @return true if succesful else false
       * @throws RemoteException Remote Problem
       */
      public boolean voterVotesAdmin(String username,String title, String candidateName, String voteLocal)  throws RemoteException;
      public void setElections(List<Election> elections) throws RemoteException;
      
      /** {@inheritDoc}
       * Election constructor
       * Creates Election
       * @param title the title
       * @param description the description
       * @param beggDate the beggining date of the election
       * @param endDate the end of the election
       * @param department the department that is in
       * @param allowedVoters the voters allowed
       * @return if successfull returns true else false
       * @throws RemoteException Remote Problem
       */
      public boolean createElection(String title, String description, Calendar beggDate,Calendar endDate,String department, List<Type> allowedVoters)throws RemoteException;
      
      /** {@inheritDoc}
       * Gets the Election List
       * @throws RemoteException Remote Problem
       */
      public List<Election> getElections() throws RemoteException;
      public void setVoterList(List<Voter> voterList) throws RemoteException;
      
      /** {@inheritDoc}
       * Gets the Voter List
       * @throws RemoteException Remote Problem
       */
      public List<Voter> getVoterList() throws RemoteException;
      
      /** {@inheritDoc}
       * Creates Candidate and adds it to the proper election
       * @param name Name of the Election Title
       * @param title The title of the List
       * @param type The Type of the List
       * @return if successful returns true else false 
       * @throws RemoteException Remote Problem
       */
      public boolean createCandidate( String name,String title,Type type) throws RemoteException;
      
      /** {@inheritDoc}
       * @param title the title of the election
       * @param candidateName the candidate to add to the election
       * @return true if successful
       * @throws RemoteException Remote Problem
       */
      public boolean removeCandidate(String title, String candidateName) throws RemoteException;
      
      /** {@inheritDoc}
       * @param title the title of the election
       * @param candidate the candidate to add to the election
       * @return true if successful
       * @throws RemoteException Remote Problem
       */
      public boolean addCandidate(String title,Candidates candidate) throws RemoteException;
      
      /** {@inheritDoc}
       * @param name a string with the election's title
       * @param newInfo Election
       * 
       * switches to the new election info
       * 
       * @return boolean if it was possible to switch
       * @throws RemoteException Remote Problem
       */
      public boolean switchElection(String name, Election newInfo) throws RemoteException;
      
      /** {@inheritDoc}
       * @param cc_number a string with the voter's cc_number
       * @param newInfo Voter
       * 
       * Switches to the new User info
       * 
       * @return boolean if it was possible to switch
       * @throws RemoteException Remote Problem
       */
      public boolean switchUser(String cc_number, Voter newInfo) throws RemoteException;
      
      /** {@inheritDoc}
       * @param table the table that you wish to add 
       * @param election the election that the table is added
       * 
       * @return boolean depending if it was successful or not
       * @throws RemoteException Remote Problem
       */
      public boolean addTableElection(MulticastServer table, Election election)throws RemoteException;
      
      /** {@inheritDoc}
       * @param table the table that you wish to remove
       * @param election the election that the table is removed
       * 
       * @return boolean depending if it was successful or not
       * 
       * @throws RemoteException Remote Problem
       */
      public boolean removeTableElection(MulticastServer table, Election election)throws RemoteException;
      
      /** {@inheritDoc}@param table the table to callback and remove the member
       * @param member the voter to be removed as a member
       * @return false if something wrong happened
       * @throws RemoteException Remote Problem
       */
      public boolean removeVoterTable(MulticastServer table, Voter member) throws RemoteException;
      
      /** {@inheritDoc}
       * @param table the table to callback and add the member
       * @param member the voter to be added as a member
       * @return false if something wrong happened
       * @throws RemoteException Remote Problem
       */
      public boolean addVoterTable(MulticastServer table, Voter member)  throws RemoteException;
      
      /** {@inheritDoc}
       * @param department the name of the department
       * It searches the table by its department that are on the OnServers
       * @return null if nothing has been found or the table if successfull
       * @throws RemoteException Remote Problem
       */
      public MulticastServer searchTableDept(String department) throws RemoteException;
      
      /** {@inheritDoc}
       * Adds a member from an election list
       * @param election Election to be added
       * @param nome Name of the List
       * @param member Voter to add
       * @return false if there was an error finding the member or the list
       * @throws RemoteException Remote Problem
       */
      public boolean addMembroToLista(Election election, String nome,Voter member) throws RemoteException;
      
      /** {@inheritDoc}
       * Removes a member from an election list
       * @return false if there was an error finding the member or the list
       * @throws RemoteException Remote Problem
       */
      public boolean removeMembroToLista(Election election, String nome,Voter member) throws RemoteException;
      
      /** {@inheritDoc}
       * @param multicastServer the multicastServer is removed from the list of onServers
       * Will notify all adminConsoles it left
       * @throws RemoteException Remote Problem
       */
      public void logoutMulticastServer(MulticastServer multicastServer) throws RemoteException;
      
      /** {@inheritDoc}
       * @param state the state wish to obtain
       * @param type the type of the Election 
       * @return a List of all the election that has the state and the type
       * @throws RemoteException Remote Problem
       */
      public List<Election> stateElections(State state, Type type) throws RemoteException;
      
      /** {@inheritDoc}
       * @param table the table that you want to find the possible elections you can vote
       * @return the list of possible Elections that the Table can vote
       * @throws RemoteException Remote Problem
       */
      public List<Election> tablesElections(MulticastServer table) throws RemoteException;
      
      /** {@inheritDoc}
       * @return onServers - list of all On Tables
       * @throws RemoteException Remote Problem
       */
       public List<MulticastServer> getOnServers() throws RemoteException;
      
       /** {@inheritDoc}
       * @return servers - list of all servers that existed
       * @throws RemoteException Remote Problem
       */
       public List<MulticastServer> getServers() throws RemoteException;
}