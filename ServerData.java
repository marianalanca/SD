import java.io.*;
import java.net.MalformedURLException;
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

    public ServerData(String department) {
        this.department = department;
        try {
            // connection with RMI
            BufferedReader br = new BufferedReader(new FileReader("configRMI.txt"));
            String address, port;
            if ((address = br.readLine())!=null && (port = br.readLine())!=null) {
                RMI = (RMIServer_I) LocateRegistry.getRegistry(address,Integer.parseInt(port)).lookup("RMIServer");
            } else {
                System.exit(0);
            }
            //RMI = (RMIServer_I) Naming.lookup("rmi://localhost:5001/RMIServer");
        } catch (RemoteException e) {
            System.out.println("The RMI Server is not connected. Connect it before starting the Multicast Server");
        } catch (NotBoundException e) {
            System.out.println("The RMI Server registry name is not correct. Correct it before starting the Multicast Server");
        } catch (FileNotFoundException e) {
            System.out.println("Could not find the config data file. Correct it before starting the Multicast Server");
        } catch (Exception e) {
			System.out.println("Exception in Multicast: " + e);
			e.printStackTrace();
        }
    }

    /**
     * @param ola
     * @throws RemoteException
     */
    public void test(MulticastServer ola) throws RemoteException{
        RMI.createVoter("Maria", "Info", "123456789", "morada", "1", null, "pass", Type.STUDENT);
        RMI.createVoter("Pedro", "Info", "123456789", "morada", "2", null, "pass", Type.STUDENT);
        RMI.createVoter("Carlos", "Info", "123456789", "morada", "3", null, "pass", Type.DOCENTE);
        RMI.createVoter("Joao", "Info", "123456789", "morada", "4", null, "pass", Type.FUNCIONARIO);

        List<Voter> members = new CopyOnWriteArrayList<Voter>();

        members.add(new Voter("Mauel", "Info", "123", "address", "2587", null, "pass", Type.STUDENT));

        List<Type> electionType = new CopyOnWriteArrayList<>();
        electionType.add(Type.STUDENT);
        electionType.add(Type.DOCENTE);
        RMI.createElection("STUDENTS","", null, null, "Info", electionType);
        RMI.createCandidate(null, "Tino", "STUDENTS", Type.STUDENT);
        RMI.createCandidate(null, "Octavio", "STUDENTS", Type.STUDENT);
        Election ele = RMI.searchElection("STUDENTS");
        RMI.addTableElection(ola, ele);

        electionType = new CopyOnWriteArrayList<>();
        electionType.add(Type.DOCENTE);
        RMI.createElection("DOCENTES","", null, null, "Info", electionType);
        RMI.createCandidate(null, "Antonio", "DOCENTES", Type.DOCENTE);
        ele = RMI.searchElection("DOCENTES");
        RMI.addTableElection(ola, ele);

        electionType = new CopyOnWriteArrayList<>();
        electionType.add(Type.FUNCIONARIO);
        RMI.createElection("FUNCIONARIOS","", null, null, "Info", electionType);
        ele = RMI.searchElection("FUNCIONARIOS");
        RMI.addTableElection(ola, ele);
        //RMI.createCandidate(null, "Vio", "FUNCIONARIOS", Type.FUNCIONARIO);




        RMI.createVoter("M", "I", "123456789", "morada", "123", null, "pass", Type.STUDENT);
        RMI.createVoter("P", "I", "123456789", "morada", "432", null, "pass", Type.STUDENT);
        RMI.createVoter("C", "I", "123456789", "morada", "135", null, "pass", Type.DOCENTE);
        RMI.createVoter("K", "I", "123456789", "morada", "136", null, "pass", Type.DOCENTE);
        RMI.createVoter("J", "I", "123456789", "morada", "246", null, "pass", Type.FUNCIONARIO);
        RMI.createVoter("L", "I", "123456789", "morada", "241", null, "pass", Type.FUNCIONARIO);

        List<Voter> membersI = new CopyOnWriteArrayList<Voter>();

        membersI.add(new Voter("M", "I", "123", "address", "258", null, "pass", Type.DOCENTE));

        List<Type> electionTypeI = new CopyOnWriteArrayList<>();
        electionTypeI.add(Type.STUDENT);
        electionTypeI.add(Type.DOCENTE);
        RMI.createElection("STUDENTSI","", null, null, "I", electionTypeI);
        RMI.createCandidate(null, "Tino1", "STUDENTSI", Type.STUDENT);
        RMI.createCandidate(null, "Octavio1", "STUDENTSI", Type.STUDENT);
        RMI.createCandidate(null, "Octavio1", "STUDENTSI", Type.DOCENTE);
        ele = RMI.searchElection("STUDENTSI");
        RMI.addTableElection(ola, ele);

        electionTypeI = new CopyOnWriteArrayList<>();
        electionTypeI.add(Type.DOCENTE);
        RMI.createElection("DOCENTESI","", null, null, "I", electionType);
        RMI.createCandidate(null, "Antonio1", "DOCENTESI", Type.DOCENTE);
        ele = RMI.searchElection("DOCENTESI");
        RMI.addTableElection(ola, ele);

        electionType = new CopyOnWriteArrayList<>();
        electionType.add(Type.FUNCIONARIO);
        RMI.createElection("FUNCIONARIOSI","", null, null, "I", electionType);
        ele = RMI.searchElection("FUNCIONARIOSI");
        RMI.addTableElection(ola, ele);
        //RMI.createCandidate(null, "Vio", "FUNCIONARIOS", Type.FUNCIONARIO);


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
     * @return List<Long> with the id of all messages received
     */
    public List<Long> getRegisteredAcks() {
        return registeredAcks;
    }
    
    // TODO
    /** 
     * @param ackID 
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
     * @return List<Voter> of all the users requesting to access the terminal
     */
    public List<Voter> getRequests() {
        return requests;
    }
    
    /** 
     * @return List<TerminalVoter> of al the users currently voting
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