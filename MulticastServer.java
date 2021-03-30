// Existe um Multicast Server por cada mesa de voto que gerelocalmente os terminais de voto que lhe estão associados.  Permite aos membros da mesa realizar a funcionalidade 6 e realiza automaticamente a funcionalidade 5
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.rmi.*;
import java.util.Date;

public class MulticastServer extends Thread implements Serializable {
    private static final long serialVersionUID = 1L;
    private ServerData q;
    private String tableID;
    private List<Voter> tableMembers = new CopyOnWriteArrayList<Voter>();


    /**
     * @param args
     */
    public static void main(String[] args) {

        // recebe departamento da consola?
        if (args.length == 0 || args.length > 3) {
            System.out.println("java MulticastServer department");
            System.exit(0); // termina
        }

        ServerData q = new ServerData(args[0]);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    Thread.sleep(200);
                    System.out.println("Shutting down ...");
                    if (q.getSearchingTerminal()!=null) {
                        q.addRequestFront(q.getSearchingTerminal());
                    }
                    q.getRMI().updateServerData(q.getDepartment(), q);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                } catch (Exception e) {
                }
            }
        });

        MulticastServer server = new MulticastServer(q);
        server.start();
        MulticastVote vote = new MulticastVote(q);
        vote.start();
        MulticastPool pool = new MulticastPool(q);
        pool.start();
        MulticastRequest request = new MulticastRequest(q);
        request.start();

    }
    public MulticastServer(ServerData q) {
        super();
        this.q = q;
    }
    public void run() {
        System.out.println("VOTING TABLE eVOTING "+q.getDepartment());
        Scanner keyboardScanner = new Scanner(System.in);
        try {
            login();

            q.test(this); // DELETE
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
                }else if (option == 3) {  // "Exit"
                    closeTerminals();
                    //q.RMI.logoutMulticastServer(this);
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
                        voter = q.getRMI().searchVoterCc(readKeyboard);
                    } else { // Username
                        System.out.print("Insert Username: ");
                        String readKeyboard = keyboardScanner.nextLine();
                        voter = q.getRMI().searchVoter(readKeyboard);
                    }
                    // test if voter exists
                    if((voter!=null && voter.department.equals(q.getDepartment()))){
                        if (q.votingContains(voter)==null && q.requestContains(voter)==null && (q.getSearchingTerminal()==null || (q.getSearchingTerminal()!=null && !q.getSearchingTerminal().getUsername().equals(voter.getUsername())))) // MELHORAR!
                            q.addRequestBack(voter); // add to voting list
                        else System.out.println("The user has already been selected");
                    } else {
                        System.out.println("Data inserted is not valid");
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
        }catch(NoSuchElementException e){
            System.out.print("Invalid operation. ");
        } catch (Exception e) {
			System.out.println("Exception in Multicast: " + e);
			e.printStackTrace();
        } finally {
            keyboardScanner.close();
            System.exit(0);
        }
    }

    /** 
     * @return String
     * @throws RemoteException
     */
    public String getTableID() throws RemoteException{
        return tableID;
    }

    /** 
     * @param tableID
     * @throws RemoteException
     */
    public void setTableID(String tableID) throws RemoteException{
        this.tableID = tableID;
    }

    /** 
     * @return List<Voter>
     * @throws RemoteException
     */
    public List<Voter> getTableMembers() throws RemoteException{
        return tableMembers;
    }

    /** 
     * @return ServerData
     */
    public ServerData getQ() {
        return q;
    }

    /** 
     * @param voter
     * @throws RemoteException
     */
    public void addTableMembers(Voter voter) throws RemoteException{
        tableMembers.add(voter);
    }

    /** 
     * @param voter
     * @throws RemoteException
     */
    public void removeTableMembers(Voter voter) throws RemoteException{
        tableMembers.remove(voter);
    }
    
    /** 
     * @param tableMembers
     * @throws RemoteException
     */
    public void setTableMembers(List<Voter> tableMembers) throws RemoteException{
        this.tableMembers = tableMembers;
    }
    
    /** 
     * @param q
     */
    public void setQ(ServerData q) {
        this.q = q;
    }

    public void reconnect(){
        try{
            q.connect(0);
            q.getRMI().loginMulticastServer(this);
        }
        catch(Exception e){
            System.out.println("reconnect and I are not friends :) " + e);
        }
    }
    
    /** 
     * @throws RemoteException
     */
    public void login() throws RemoteException{
        MulticastServer server = q.getRMI().loginMulticastServer(this);
        if (server!=null) {
            q.setRequests(server.getQ().getRequests());
            q.setVoting(server.getQ().getVoting());
            q.setRegisteredAcks(server.getQ().getRegisteredAcks());
            setTableID(server.getTableID());
            setTableMembers(server.getTableMembers());
        }
    }

    /**
     * @throws IOException
     */
    public void closeTerminals() throws IOException{
        MulticastSocket socket = new MulticastSocket(q.getPORT());  // create socket and bind it
        InetAddress group = InetAddress.getByName(q.getMULTICAST_ADDRESS());
        socket.joinGroup(group);

        // send message to finish
        byte[] buffer = (new Protocol().turnoff(new Date().getTime(), q.getDepartment())).getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, q.getPORT());
        socket.send(packet);
    }
}
// receives vote info
class MulticastVote extends Thread implements Serializable {
    ServerData q;

    public MulticastVote(ServerData q) {
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

                if (q.getRMI().voterVotes(protocol.username, protocol.election, protocol.candidate, q.getDepartment())) { // não encontra eleição?
                    buffer = new Protocol().status(new Date().getTime(), protocol.id, q.getDepartment(), "off", "Vote submitted successfully").getBytes();
                } else {
                    buffer = new Protocol().status(new Date().getTime(), protocol.id, q.getDepartment(), "off", "The vote can not be submitted").getBytes();
                }
                packet = new DatagramPacket(buffer, buffer.length, groupACK, q.getPORT());
                socketACK.send(packet);

                q.removeVoter(protocol.id); 
            }
        } catch (RemoteException e) {
            this.start();
        } catch (IOException e) {
            System.out.println("Couldn't contact with terminal"); // TO DO
        } catch (Exception e) {
			System.out.println("Exception in Multicast: " + e);
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
    ServerData q;

    public MulticastPool(ServerData q) {
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
                if (q.getRequests().size()!=0){
                    Voter voter = q.getRequests().get(0);
                    q.getRequests().remove(0);
                    q.setSearchingTerminal(voter);
                    findTerminal(voter, socket, group);
                }
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

    void findTerminal(Voter voter, MulticastSocket socket, InetAddress group) throws IOException{
        try {
            Protocol protocol;

            socket.setSoTimeout(2000); // in case it didn't receive a response in 2 seconds

            // send request for terminal
            byte[] buffer = (new Protocol().request(new Date().getTime(),  q.getDepartment())).getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, q.getPORT());
            socket.send(packet);

            // receives answer
            do {
                buffer = new byte[256];
                packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                protocol = new Protocol().parse(new String(packet.getData(), 0, packet.getLength()));
            } while (protocol==null || !(protocol!=null && protocol.type!=null && (protocol.type.equals("response"))  && protocol.department.equals(q.getDepartment()) && protocol.id!=null));
            String id = protocol.id;

            // test if packet has already been received;  this is necessary since sometimes the client receives some packet that has already been received and starts unnecessarily
            if (q.getRegisteredAcks().contains(protocol.msgId)) {
                return;
            } else {
                q.getRegisteredAcks().add(protocol.msgId);
            }

            // sends accepted
            buffer = (new Protocol().accepted(new Date().getTime(), id)).getBytes();
            packet = new DatagramPacket(buffer, buffer.length, group, q.getPORT());
            socket.send(packet);

            // has received the terminal
            q.addVoter(id, voter);
            q.setSearchingTerminal(null);

            // send voter data to terminal
            buffer = new Protocol().login(new Date().getTime(),  id, voter.username, voter.getPassword()).getBytes();
            packet = new DatagramPacket(buffer, buffer.length, group, q.getPORT());
            socket.send(packet);

            // receives ack in case the terminal has received the data
            do {
                buffer = new byte[256];
                packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                protocol = new Protocol().parse(new String(packet.getData(), 0, packet.getLength()));
            } while (protocol==null || !(protocol!=null && protocol.type!=null && (protocol.type.equals("ack"))  && protocol.department.equals(q.getDepartment()) && protocol.id!=null && protocol.id.equals(id)));

            if (protocol.type.equals("crashed")) {
                q.addRequestFront(voter);
            }

        } catch (SocketTimeoutException e) {q.addRequestFront(voter);}
    }

}
// receives packets from terminals
class MulticastRequest extends Thread implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    ServerData q;

    public MulticastRequest(ServerData q) {
        super();
        this.q = q;
    }

    public void run() {
        MulticastSocket socket = null;
        TerminalVoter voter;
        try {
            socket = new MulticastSocket(q.getPORT());  // create socket and bind it
            InetAddress group = InetAddress.getByName(q.getMULTICAST_ADDRESS());
            socket.joinGroup(group);

            Protocol protocol;
            socket.setSoTimeout(q.getTIMEOUT());

            while (true) {
                // recebe info!
                byte[] buffer = new byte[256];;
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                protocol = new Protocol().parse(new String(packet.getData(), 0, packet.getLength()));

                if (protocol!=null) {
                    if (protocol.department!=null && protocol.department.equals(q.getDepartment())) {
                        if (protocol.id!=null) {
                            voter = q.searchVoter(protocol.id);
                            if (voter!=null){
                                if (protocol.logged!=null) {
                                    if (protocol.type.equals("status")) {
                                        status(protocol.logged, voter, socket, group);
                                    }
                                } else if(protocol.election!=null) {
                                    if (protocol.type.equals("election")) {
                                        electionList(protocol.election, voter, socket, group);
                                    }
                                }else {
                                    if (protocol.type.equals("crashed")) {
                                        crash(voter.getData());
                                        q.removeVoter(voter);
                                    } else if (protocol.type.equals("timeout")) {
                                        q.removeVoter(voter);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (SocketTimeoutException e) {
        } catch (RemoteException e) {
            this.start();
        }catch (NullPointerException e) {
            System.out.println("Ups. Guess something went wrong. Try again");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Couldn't contact with terminal");
        } catch (Exception e) {
			System.out.println("Exception in main: " + e);
			e.printStackTrace();
        } finally {
            socket.close();
        }
    }

    public void crash(Voter voter) throws RemoteException{
        if (voter!=null){
            if (q.getVoting().size()==0) {
                q.addRequestBack(voter);
            } else{
                q.addRequestFront(voter);
            }
        }
    }

    public void status(String logged, TerminalVoter voter, MulticastSocket socket, InetAddress group) throws IOException{
        if (logged.equals("on")){ // receives logged in confirmation
            // sends candidates list
            // fazer verificação do role com o candidate para só apresentar o que se quer
            List<Election> elections = q.getRMI().searchElectionbyDepRole(q.getDepartment(), voter.getData().getType());
            List<String> electionsNames = new CopyOnWriteArrayList<String>();

            // adds elections names in a list to send to client
            for (Election election: elections) {
                electionsNames.add(election.getTitle());
            }

            // send candidates information
            byte[] buffer = new Protocol().item_list(new Date().getTime(),  voter.getID(), electionsNames.size(), electionsNames).getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, q.getPORT());
            socket.send(packet);

        }
    }

    public void electionList(String election, TerminalVoter voter, MulticastSocket socket, InetAddress group) throws IOException{
        // sends candidates list
        List<String> candidates = new CopyOnWriteArrayList<String>();
        for (Candidates candidate: q.getRMI().searchElection(election).getCandidatesList()) {
            if (candidate.getType().equals(voter.getData().getType()))
                candidates.add(candidate.getName());
        }
        // send candidates information
        byte[] buffer = new Protocol().item_list(new Date().getTime(), voter.getID(), candidates.size(), candidates).getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, q.getPORT());
        socket.send(packet);
    }

}
