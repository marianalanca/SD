import java.io.*;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerData implements Serializable{
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String MULTICAST_ADDRESS = "224.0.224.0";
    private int PORT = 4321;
    private int RESULT_PORT = 4322;  // RESULT Port
    private String department;
    private List<Voter> requests = new CopyOnWriteArrayList<Voter>(); // stores all voting members in case a terminal fails
    private int TIMEOUT = 120000;
    private RMIServer_I RMI;
    private List<Long> registeredAcks = new CopyOnWriteArrayList<Long>();
    private List<TerminalVoter> voting = new CopyOnWriteArrayList<TerminalVoter>();
    private Voter searchingTerminal;

    public ServerData(String department) {
        this.department = department;
        if (!connect(0)) {
            System.exit(0);
        }
    }

    public TerminalVoter votingContains(Voter voter) {
        for (TerminalVoter terminalVoter: voting) {
            if (terminalVoter.getData().getUsername().equals(voter.getUsername())) {
                return terminalVoter;
            }
        }
        return null;
    }

    public Voter requestContains(Voter voter) {
        for (Voter request: requests) {
            if (request.getUsername().equals(voter.getUsername())) {
                return request;
            }
        }
        return null;
    }

    public void setRequests(List<Voter> requests) {
        this.requests = requests;
    }

    public void setVoting(List<TerminalVoter> voting) {
        this.voting = voting;
    }

    public void setRegisteredAcks(List<Long> registeredAcks) {
        this.registeredAcks = registeredAcks;
    }

    public Voter getSearchingTerminal() {
        return searchingTerminal;
    }

    public void setSearchingTerminal(Voter searchingTerminal) {
        this.searchingTerminal = searchingTerminal;
    }

    public boolean connect (int count) {
        try {
            // connection with RMI
            BufferedReader br = new BufferedReader(new FileReader("configRMI.txt"));
            String address, port;
            if ((address = br.readLine())!=null && (port = br.readLine())!=null) {
                RMI = (RMIServer_I) LocateRegistry.getRegistry(address,Integer.parseInt(port)).lookup("RMIServer");
            } else {
                System.exit(0);
            }
            br.close();
            return true;
        } catch (ConnectException e) {
            if (count>2)
                connect(++count);
            else {
                System.out.println("The RMI Server is unreachable");
                return false;
            }
        } catch (RemoteException e) {
            System.out.println("The RMI Server is not connected. Connect it before starting the Multicast Server");
        } catch (NotBoundException e) {
            System.out.println("The RMI Server registry name is not correct. Correct it before starting the Multicast Server");
        } catch (FileNotFoundException e) {
            System.out.println("Could not find the config data file. Correct it before starting the Multicast Server");
        } catch (Exception e) {
			System.out.println("The RMI Server data is not correct. Correct it before starting the Multicast Server");
        }
        return false;
    }
    /** 
     * @return int with the value of the client Port
     */
    public int getPORT() {
        return PORT;
    }
    
    /** 
     * @return int with the value of the port where the vote result is received
     */
    public int getRESULT_PORT() {
        return RESULT_PORT;
    }
    
    /** 
     * @return String with the department of the table
     */
    public String getDepartment() {
        return department;
    }
    
    /** 
     * @return List with the id of all messages received
     */
    public List<Long> getRegisteredAcks() {
        return registeredAcks;
    }
    
    /** 
     * @param ackID it is the id of the message received
     * @return boolean
     */
    public boolean addACK(Long ackID) {
        if (!registeredAcks.contains(ackID)) {
            registeredAcks.add(ackID);
            return true;
        }
        return false;
    }

    public void printAcks(){
        for (Long ack: registeredAcks) {
            System.out.println(ack);
        }
    }

    public void addRequestFront (Voter voter) {
        requests.add(0, voter);
    }

    public void addRequestBack (Voter voter) {
        requests.add(voter);
    }

    /**
     * @return String containg the multicast address
     */
    public String getMULTICAST_ADDRESS() {
        return MULTICAST_ADDRESS;
    }
    
    /** 
     * @return RMIServer_I with an object of the RMI Server type
     */
    public RMIServer_I getRMI() {
        return RMI;
    }
    
    /** 
     * @param rMI  of the new RMI Server
     */
    public void setRMI(RMIServer_I rMI) {
        RMI = rMI;
    }
    
    /** 
     * @return int with the value of the messages timeout
     */
    public int getTIMEOUT() {
        return TIMEOUT;
    }
    
    /** 
     * @return List of all the users requesting to access the terminal
     */
    public List<Voter> getRequests() {
        return requests;
    }
    
    /** 
     * @return List of al the users currently voting
     */
    public List<TerminalVoter> getVoting() {
        return voting;
    }
    
    /** 
     * @param id of the user to add to the list
     * @param voter of the user to add to the list
     */
    public void addVoter(String id, Voter voter) {
        voting.add(new TerminalVoter(id, voter));
    }
    
    /** 
     * @param id of the voter to be found
     * @return TerminalVoter with the given id
     */
    public TerminalVoter searchVoter(String id) {
        for (TerminalVoter voter: voting) {
            if (voter.getID().equals(id)){
                return voter;
            }
        }
        return null;
    }
    
    /** 
     * @param voter object to remove from the list
     */
    public void removeVoter(TerminalVoter voter){
        voting.remove(voter);
    }

    public void removeVoter(String id){
        for (TerminalVoter voter: voting) {
            if (voter.getID().equals(id)){
                voting.remove(voter);
                return;
            }
        }
    }
}