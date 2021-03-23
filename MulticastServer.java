// Existe um Multicast Server por cada mesa de voto que gerelocalmente os terminais de voto que lhe estão associados.  Permite aos membros da mesa realizar a funcionalidade 6 e realiza automaticamente a funcionalidade 5
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.rmi.*;

//public interface CalculatorInterface extends Remote {

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
        super("Server eVoting number " + (long) (Math.random() * 1000));
        this.q = q;
    }

    synchronized public void run() {
        MulticastSocket socket = null;
        System.out.println(this.getName() + " running...");
        try {
            // Conection voting terminal
            socket = new MulticastSocket(q.getPORT());  // create socket for communication with voting terminal
            InetAddress group = InetAddress.getByName(q.getMULTICAST_ADDRESS());
            socket.joinGroup(group);

            //RMIServer_I RMI = (RMIServer_I) Naming.lookup("rmi://localhost:5001/RMIServer");
            //RMI.loginMulticastServer(this);

            Scanner keyboardScanner = new Scanner(System.in);

            while (true) {

                // receber por informações de novos eleitores a quererem autenticar-se
                System.out.print("Insert your CC: ");
                String readKeyboard = keyboardScanner.nextLine();

                //Voter voter = RMI.searchVoterCc(readKeyboard);
                // DEBUG
                Voter voter = new Voter("test", "Info", "987654123","morada", "123456789",null,"pass", null);
                if((voter!=null && voter.department.equals(q.getDepartment()))){
                    // add to voting list
                    q.addVoting(voter);

                    // Send request for threads
                    q.requestTerminal(socket);
                    if (!q.full) {
                        // send voter data to terminal
                        byte[] buffer = new Protocol().login(q.ID, voter.username, voter.getPassword()).getBytes();
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, q.getPORT());
                        socket.send(packet);

                        // receives logged in confirmation
                        boolean flag = true;
                        do {
                            packet = new DatagramPacket(buffer, buffer.length);
                            socket.receive(packet);
                            Protocol protocol = new Protocol().parse(new String(packet.getData(), 0, packet.getLength()));
                            if (protocol!=null && protocol.id!=null && protocol.id.equals(q.ID) && protocol.type.equals("status") && protocol.logged.equals("on")) {
                                flag=false;
                            }
                        } while (flag);

                        // sends candidates list
                        // fazer verificação do role com o candidate para só apresentar o que se quer
                        //RMI.searchElection("title");
                        buffer = new Protocol().item_list(q.ID, 0, new CopyOnWriteArrayList<String>()).getBytes();
                        packet = new DatagramPacket(buffer, buffer.length, group, q.getPORT());
                        socket.send(packet);
                        q.availableTerminal = false;
                    }

                    // available for another request
                }
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
    private String MULTICAST_ADDRESS = "224.0.224.0";
    private int PORT = 4321;  // Client Port
    private int RESULT_PORT = 4322;  // RESULT Port
    private String department;
    private List<Voter> voting = new CopyOnWriteArrayList<Voter>(); // stores all voting members in case a terminal fails
    public String ID;
    public boolean availableTerminal = false; //tests whether a terminal has been found for the request
    public boolean full = false;

    public Q_ok(String department) {
        this.department = department;
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

    public void addVoting(Voter voter) {
        voting.add(voter);
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

    synchronized void requestTerminal(MulticastSocket socket) {
        try {
            //System.out.println("BREAKPOINT 1.1");
            notify();
            while(!availableTerminal) {
                wait();
            }
            //System.out.println("BREAKPOINT 1.2");
        } catch(InterruptedException e) {
            System.out.println("interruptedException caught");
        } catch (Exception e) {
            System.out.println("Exception in main: " + e);
            e.printStackTrace();
        }
    }

    synchronized void findTerminal(MulticastSocket socket, InetAddress group) {
        try {
            //System.out.println("BREAKPOINT 2.1");
            wait();

            byte[] buffer = (new Protocol().request(department)).getBytes();  // TO DO PROTOCOL
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            socket.send(packet);
            boolean flag = true;

            // receives answer from thread
            socket.setSoTimeout(150);
            do {
                buffer = new byte[256];
                packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                Protocol protocol = new Protocol().parse(new String(packet.getData(), 0, packet.getLength()));
                System.out.println(protocol.type);
                if (protocol!=null && protocol.type!=null && protocol.type.equals("response") && protocol.id!=null) {
                    flag=false;
                    ID = protocol.id;
                }
            } while (flag);
            // sendes confirmation with id
            buffer = (new Protocol().accepted(ID)).getBytes();
            packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            socket.send(packet);
            //System.out.println("BREAKPOINT 2.6");

            availableTerminal = true;
            notify();
            //System.out.println("BREAKPOINT 2.7");

        } catch (SocketTimeoutException e) {
            System.out.println("There are no available terminals");
            full =true;
        } catch(InterruptedException e) {
            System.out.println("interruptedException caught");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Exception in main: " + e);
            e.printStackTrace();
        }
    }
}

// receives vote info
class Multicast extends Thread implements Serializable {
    Q_ok q;

    public Multicast(Q_ok q) {
        this.q = q;
    }

    synchronized public void run() {
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
                System.out.println(protocol.candidate);
                q.removeVoting(protocol.username);

                System.out.println(protocol.id);

                buffer = new Protocol().status(protocol.id, "off").getBytes();
                packet = new DatagramPacket(buffer, buffer.length, groupACK, q.getPORT());
                socketACK.send(packet);

                /*buffer = new Protocol().status(protocol.id, "off").getBytes();
                packet = new DatagramPacket(buffer, buffer.length, groupACK, q.getPORT());
                socketACK.send(packet);
                System.out.println("Sent");*/
                //q.full = false;
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
// waits for new threads and contacts with them
class MulticastPool extends Thread implements Serializable {
    Q_ok q;

    public MulticastPool(Q_ok q) {
        super();
        this.q = q;
    }

    // requests new terminals
    synchronized public void run() {
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