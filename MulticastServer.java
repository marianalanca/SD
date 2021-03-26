// Existe um Multicast Server por cada mesa de voto que gerelocalmente os terminais de voto que lhe estão associados.  Permite aos membros da mesa realizar a funcionalidade 6 e realiza automaticamente a funcionalidade 5
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.rmi.*;
import java.rmi.ConnectException;

public class MulticastServer extends Thread implements Serializable {
    private static final long serialVersionUID = 1L;
    Q_ok q;
    private String tableID;
    private List<Voter> tableMembers = new CopyOnWriteArrayList<Voter>();

    public static void main(String[] args) {
        // recebe departamento da consola?
        if (args.length == 0) {
            System.out.println("java MulticastServer department");
            System.exit(0); // termina
        }
        //Table table = new Table(this);
        Q_ok q = new Q_ok(args[0]);
        MulticastServer server = new MulticastServer(q);
        server.start();
        Multicast multicast = new Multicast(q);
        multicast.start();
        MulticastPool pool = new MulticastPool(q);
        pool.start();
        MulticastCrash crash = new MulticastCrash(q);
        crash.start();
    }
    public MulticastServer(Q_ok q) {
        super();
        this.q = q;
    }
    public void run() {
        MulticastSocket socket = null;
        System.out.println("VOTING TABLE eVOTING "+q.getDepartment());
        Scanner keyboardScanner = new Scanner(System.in);
        try {
            login();
            System.out.println(getId());

            q.test(this);
            while (true) {
                // Receives from the console requests for new connections
                int option = 0;
                    do {
                    System.out.println("1) Check members of list\n2) Autenticate\n3) Exit");
                    try {
                        option = Integer.valueOf(keyboardScanner.nextLine());
                    } catch (NumberFormatException e) {
                        option = 0; // out of range
                    }
                } while (option < 1 || option > 3);
                if (option == 1) {
                    System.out.println("List of table members");
                    for (Voter member: tableMembers) {
                        System.out.println('\t'+member.getUsername());
                    }
                }else if (option == 3) {
                    // wait for all of the threads to finish voting
                    // disconnect all the threads
                    // logout from server
                    q.RMI.logoutMulticastServer(this);
                    System.exit(0);
                } else {
                    option = 0;
                    Voter voter;
                    do {
                        System.out.println("Autenticate by:\n1) Citizen Card\n2) Username ");
                        try {
                            option = Integer.valueOf(keyboardScanner.nextLine());
                        } catch (NumberFormatException e) {
                            option = 0; // out of range
                        }
                    } while (option !=1 && option != 2);

                    // Autentication
                    if (option==1) { // CC
                        System.out.print("Insert CC: ");
                        String readKeyboard = keyboardScanner.nextLine();
                        voter = q.RMI.searchVoterCc(readKeyboard);
                    } else { // Username
                        System.out.print("Insert Username: ");
                        String readKeyboard = keyboardScanner.nextLine();
                        voter = q.RMI.searchVoter(readKeyboard);
                    }
                    // test if voter exists
                    if((voter!=null && voter.department.equals(q.getDepartment()))){
                        q.addLogin(voter);// add to voting list
                    } else {
                        System.out.println("Data inserted not valid");
                    }
                }
            }
        } catch (RemoteException e){
            try{
                reconnect();
                this.start();
            } catch(Exception excp){
                System.out.println("MultiCast Server: connecting failed " + excp);
            }
        } catch (Exception e) {
			System.out.println("Exception in main: " + e);
			e.printStackTrace();
        } finally {
            keyboardScanner.close();
            if (socket!=null)
                socket.close();
        }
    }
    public String getTableID() throws RemoteException{
        return tableID;
    }
    public void setTableID(String tableID) throws RemoteException{
        this.tableID = tableID;
    }
    public List<Voter> getTableMembers() throws RemoteException{
        return tableMembers;
    }
    public Q_ok getQ() {
        return q;
    }
    public void addTableMembers(Voter voter) throws RemoteException{
        tableMembers.add(voter);
    }
    public void removeTableMembers(Voter voter) throws RemoteException{
        tableMembers.remove(voter);
    }
    public void setTableMembers(List<Voter> tableMembers) throws RemoteException{
        this.tableMembers = tableMembers;
    }
    public void reconnect(){
        try{
            q.RMI = (RMIServer_I) Naming.lookup("rmi://localhost:5001/RMIServer");
            q.RMI.loginMulticastServer(this);
        }
        catch(ConnectException e){
            reconnect();
        }
        catch(Exception e){
            System.out.println("reconnect and I are not friends :) " + e);
        }
    }
    public MulticastServer searchServer() throws RemoteException{
        List<MulticastServer> servers = q.RMI.getOnServers();
            for (MulticastServer server: servers) {
                if (server.q.getDepartment().equals(q.getDepartment())){
                    return server;
                }
            }
            return null;
    }
    public void login() throws RemoteException{
        MulticastServer server = searchServer();
        if (server==null) { // it does not exist
            q.RMI.loginMulticastServer(this);
        } else {
            q = server.q;
            setTableID(server.getTableID());
            setTableMembers(server.getTableMembers());
        }
    }
}
class Q_ok implements Serializable{
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String electionTitle;
    private String MULTICAST_ADDRESS = "224.0.224.0";
    private int PORT = 4321;  // Client Port
    private int RESULT_PORT = 4322;  // RESULT Port
    private String department;
    private List<Voter> voting = new CopyOnWriteArrayList<Voter>(); // stores all voting members in case a terminal fails
    private List<Voter> login = new CopyOnWriteArrayList<Voter>(); // stores all voting members in case a terminal fails
    public String ID;
    public boolean availableTerminal = false; //tests whether a terminal has been found for the request
    private int TIMEOUT = 120000;
    RMIServer_I RMI;

    public Q_ok(String department) {
        this.department = department;
        try {
            // connection with RMI
            RMI = (RMIServer_I) Naming.lookup("rmi://localhost:5001/RMIServer");
            //RMI.searchTableDept(department);
            //chamar exists
        } catch (Exception e) {
            System.out.println("Exception in main: " + e);
			e.printStackTrace();
        }
    }
    public void test(MulticastServer ola) throws RemoteException{
        RMI.createVoter("Maria", "Info", "123456789", "morada", "1234", null, "pass", Type.STUDENT);
        RMI.createVoter("Pedro", "Info", "123456789", "morada", "4321", null, "pass", Type.STUDENT);
        RMI.createVoter("Carlos", "Info", "123456789", "morada", "1357", null, "pass", Type.DOCENTE);
        RMI.createVoter("Joao", "Info", "123456789", "morada", "2468", null, "pass", Type.FUNCIONARIO);

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
    public int getPORT() {
        return PORT;
    }
    public int getRESULT_PORT() {
        return RESULT_PORT;
    }
    public String getDepartment() {
        return department;
    }
    public String getElectionTitle() {
        return electionTitle;
    }
    public void addVoting(Voter voter) {
        voting.add(voter);
    }
    public void addLogin(Voter voter) {
        login.add(voter);
    }
    public void removeVoting(String username) {
        // search for cc
        // remove from voting
        for (Voter voter: voting) {
            if (voter.getUsername().equals(username)) {
                voting.remove(voter);
                return;
            }
        }
        return;
    }
    public String getMULTICAST_ADDRESS() {
        return MULTICAST_ADDRESS;
    }
    public List<Voter> getLogin() {
        return login;
    }
    void findTerminal(MulticastSocket socket, InetAddress group) {
        if (login.size()!=0) {
            try {
                socket.setSoTimeout(1000); // in case it didn't receive a response in a second
                Voter voter = login.get(0);
                byte[] buffer = (new Protocol().request(department)).getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                socket.send(packet);
                Protocol protocol;

                // receives answer from thread
                do {
                    buffer = new byte[256];
                    packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    protocol = new Protocol().parse(new String(packet.getData(), 0, packet.getLength()));

                } while (protocol==null || !(protocol!=null && protocol.type!=null && (protocol.type.equals("response") || protocol.type.equals("crashed"))  && protocol.department.equals(department) && protocol.id!=null));
                ID = protocol.id;

                if (protocol.type.equals("crashed")) {
                    login.remove(0);
                    return;
                }

                // sends accepted
                buffer = (new Protocol().accepted(ID)).getBytes();
                packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                socket.send(packet);

                socket.setSoTimeout(TIMEOUT);

                // send voter data to terminal
                buffer = new Protocol().login(ID, voter.username, voter.getPassword()).getBytes();
                packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                socket.send(packet);

                // receives logged in confirmation
                do {
                    packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    protocol = new Protocol().parse(new String(packet.getData(), 0, packet.getLength()));
                } while (protocol==null || !(protocol!=null && protocol.id!=null && protocol.id.equals(ID) && (protocol.type.equals("status") || protocol.type.equals("crashed")) && protocol.department.equals(department) && protocol.logged.equals("on")));

                if (protocol.type.equals("crashed")) {
                    login.remove(0);
                    return;
                }

                // sends candidates list
                // fazer verificação do role com o candidate para só apresentar o que se quer
                List<Election> elections = RMI.searchElectionbyDepRole(department, voter.getType());
                List<String> electionsNames = new CopyOnWriteArrayList<String>();

                // adds elections names in a list to send to client
                for (Election election: elections) {
                    electionsNames.add(election.getTitle());
                }

                // send candidates information
                buffer = new Protocol().item_list(ID, electionsNames.size(), electionsNames).getBytes();
                packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                socket.send(packet);

                // receives request
                do {
                    packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    protocol = new Protocol().parse(new String(packet.getData(), 0, packet.getLength()));
                } while (protocol==null || !(protocol!=null && protocol.id!=null && protocol.id.equals(ID) && (protocol.type.equals("election") || protocol.type.equals("crashed")) && protocol.department.equals(department)));

                if (protocol.type.equals("crashed")) {
                    login.remove(0);
                    return;
                }

                // sends candidates list
                // fazer verificação do role com o candidate para só apresentar o que se quer
                List<String> candidates = new CopyOnWriteArrayList<String>();
                for (Candidates candidate: RMI.searchElection(protocol.election).getCandidatesList()) {
                    //if (candidate.getType().equals(voter.getType()))
                        candidates.add(candidate.getName());
                }
                // send candidates information
                buffer = new Protocol().item_list(ID, candidates.size(), candidates).getBytes();
                packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                socket.send(packet);

                login.remove(0);

            } catch (SocketTimeoutException e) {
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("Exception in main: " + e);
                e.printStackTrace();
            }
        }
    }
}
// receives vote info
class Multicast extends Thread implements Serializable {
    Q_ok q;

    public Multicast(Q_ok q) {
        this.q = q;
    }
    public void run() {
        MulticastSocket socket = null;
        MulticastSocket socketACK = null;
        try {
            socket = new MulticastSocket(q.getRESULT_PORT());  // create socket and bind it
            InetAddress group = InetAddress.getByName(q.getMULTICAST_ADDRESS());
            socket.joinGroup(group);

            socketACK = new MulticastSocket(q.getPORT());  // create socket and bind it
            InetAddress groupACK = InetAddress.getByName(q.getMULTICAST_ADDRESS());
            byte[] buffer;
            DatagramPacket packet;
            Protocol protocol;

            // receives vote information
            while (true) {
                do {
                    buffer = new byte[256];
                    packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    protocol = new Protocol().parse(new String(packet.getData(), 0, packet.getLength()));
                } while (protocol==null || !(protocol!=null && protocol.type!=null && protocol.type.equals("vote") && protocol.department.equals(q.getDepartment())));

                if (protocol.candidate.equals("white")) {
                    protocol.candidate = "";
                }
                if (q.RMI.voterVotes(protocol.username, protocol.election, protocol.candidate, q.getDepartment())) { // não encontra eleição?
                    buffer = new Protocol().status(protocol.id, q.getDepartment(), "off", "Vote submitted successfully").getBytes();
                } else {
                    buffer = new Protocol().status(protocol.id, q.getDepartment(), "off", "The vote can not be submitted").getBytes();
                }
                packet = new DatagramPacket(buffer, buffer.length, groupACK, q.getPORT());
                socketACK.send(packet);
            }
        } catch (RemoteException e) {
            this.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
			System.out.println("Exception in main: " + e);
			e.printStackTrace();
        } finally {
            socket.close();
        }
    }

}
// contacts with threads when needed
class MulticastPool extends Thread implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    Q_ok q;

    public MulticastPool(Q_ok q) {
        super();
        this.q = q;
    }
    // requests new terminals
    public void run() {
        MulticastSocket socket = null;
        try {
            socket = new MulticastSocket(q.getPORT());  // create socket and bind it
            InetAddress group = InetAddress.getByName(q.getMULTICAST_ADDRESS());
            socket.joinGroup(group);

            while (true) {
                q.findTerminal(socket, group); // pôr aqui a função!
            }
        } catch (RemoteException e) {
            this.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
			System.out.println("Exception in main: " + e);
			e.printStackTrace();
        } finally {
            socket.close();
        }
    }

}

// contacts with threads when needed
class MulticastCrash extends Thread implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    Q_ok q;

    public MulticastCrash(Q_ok q) {
        super();
        this.q = q;
    }
    // requests new terminals
    public void run() {
        MulticastSocket socket = null;
        try {
            socket = new MulticastSocket(q.getPORT());  // create socket and bind it
            InetAddress group = InetAddress.getByName(q.getMULTICAST_ADDRESS());
            socket.joinGroup(group);

            Protocol protocol;

            while (true) {
                do {
                    byte[] buffer = new byte[256];;
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    protocol = new Protocol().parse(new String(packet.getData(), 0, packet.getLength()));
                } while (protocol==null || !(protocol!=null && protocol.id!=null && protocol.username!=null && protocol.type.equals("crashed") && protocol.department.equals(q.getDepartment())));

                System.out.println("crashou :(");
                if (q.getLogin().size()==0) {
                    q.addLogin(q.RMI.searchVoter(protocol.username));
                } else
                    q.getLogin().add(0, q.RMI.searchVoter(protocol.username));
            }
        } catch (RemoteException e) {
            this.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
			System.out.println("Exception in main: " + e);
			e.printStackTrace();
        } finally {
            socket.close();
        }
    }

}
