// Existe um Multicast Server por cada mesa de voto que gerelocalmente os terminais de voto que lhe estão associados.  Permite aos membros da mesa realizar a funcionalidade 6 e realiza automaticamente a funcionalidade 5
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.rmi.*;

public class MulticastServer extends Thread implements Serializable {
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
    }

    public MulticastServer(Q_ok q) {
        super();
        this.q = q;
    }

    public void run() {
        MulticastSocket socket = null;
        System.out.println("VOTING TABLE eVOTING "+q.getDepartment());
        try {

            // connection with RMI
            q.RMI.loginMulticastServer(this);

            Scanner keyboardScanner = new Scanner(System.in);

            while (true) {

                // receber por informações de novos eleitores a quererem autenticar-se
                int option;
                Voter voter;
                do {
                    System.out.println("Autenticate by:\n1) Citizen Card\n2) Username ");
                    option = Integer.valueOf(keyboardScanner.nextLine());
                } while (option ==1 && option == 2);

                // Autentication
                if (option==1) { // CC
                    System.out.print("Insert CC: ");
                    String readKeyboard = keyboardScanner.nextLine();
                    voter = q.RMI.searchVoterCc(readKeyboard);
                } else {
                    System.out.print("Insert Username: ");
                    String readKeyboard = keyboardScanner.nextLine();
                    voter = q.RMI.searchVoter(readKeyboard);
                }

                //Voter voter = q.RMI.searchVoterCc(readKeyboard);
                voter = new Voter("test", "Info", "987654123","morada", "123456789",null,"pass", null);
                if((voter!=null && voter.department.equals(q.getDepartment()))){
                    // add to voting list
                    q.addLogin(voter);
                } else {
                    System.out.println("Data inserted not valid");
                }
            }
        } catch (Exception e) {
			System.out.println("Exception in main: " + e);
			e.printStackTrace();
        } finally {
            socket.close();
        }
    }

    public String getTableID() {
        return tableID;
    }

    public void setTableID(String tableID) {
        this.tableID = tableID;
    }

    public List<Voter> getTableMembers() {
        return tableMembers;
    }

    public void addTableMembers(Voter voter) {
        tableMembers.add(voter);
    }

    public void removeTableMembers(Voter voter) {
        tableMembers.remove(voter);
    }

    public void setTableMembers(List<Voter> tableMembers) {
        this.tableMembers = tableMembers;
    }
}
class Q_ok implements Serializable{
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
        } catch (Exception e) {
            System.out.println("Exception in main: " + e);
			e.printStackTrace();
        }
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
        System.out.println("ADDED: "+voting.size());
        for(Voter vote : voting) {
            System.out.println(vote.username);
        }
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
    void findTerminal(MulticastSocket socket, InetAddress group) {
        //
        if (login.size()!=0) {
            System.out.print("LOGIN.SIZE() "+login.size());
            try {
                Voter voter = login.get(0);
                byte[] buffer = (new Protocol().request(department)).getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                socket.send(packet);
                boolean flag = true;

                // receives answer from thread
                do {
                    buffer = new byte[256];
                    packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    Protocol protocol = new Protocol().parse(new String(packet.getData(), 0, packet.getLength()));
                    if (protocol!=null && protocol.type!=null && protocol.type.equals("response") && protocol.id!=null) {
                        flag=false;
                        ID = protocol.id;
                    }
                } while (flag);
                socket.setSoTimeout(TIMEOUT);

                // sends accepted
                buffer = (new Protocol().accepted(ID)).getBytes();
                packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                socket.send(packet);

                // send voter data to terminal
                buffer = new Protocol().login(ID, voter.username, voter.getPassword()).getBytes();
                packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                socket.send(packet);

                // receives logged in confirmation
                flag = true;
                do {
                    packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    Protocol protocol = new Protocol().parse(new String(packet.getData(), 0, packet.getLength()));
                    if (protocol!=null && protocol.id!=null && protocol.id.equals(ID) && protocol.type.equals("status") && protocol.logged.equals("on")) {
                        flag=false;
                    }
                } while (flag);

                // sends candidates list
                // fazer verificação do role com o candidate para só apresentar o que se quer
                //RMI.searchElection("title");
                buffer = new Protocol().item_list(ID, 0, new CopyOnWriteArrayList<String>()).getBytes();
                packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                socket.send(packet);

                login.remove(0);

            } catch (SocketTimeoutException e) {
                System.out.println("Terminal has been idle for too long");
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

            while (true) {
                do {
                    buffer = new byte[256];
                    packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    protocol = new Protocol().parse(new String(packet.getData(), 0, packet.getLength()));
                } while (protocol!=null && protocol.type!=null && !protocol.type.equals("vote"));

                q.removeVoting(protocol.username);
                q.RMI.voterVotes(protocol.username, q.getElectionTitle(), "candidateName", q.getDepartment());
                buffer = new Protocol().status(protocol.id, "off").getBytes();
                packet = new DatagramPacket(buffer, buffer.length, groupACK, q.getPORT());
                socketACK.send(packet);
            }
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
                q.findTerminal(socket, group);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
			System.out.println("Exception in main: " + e);
			e.printStackTrace();
        //}catch (SocketTimeoutException) {}
        } finally {
            socket.close();
        }
    }

}