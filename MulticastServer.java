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

            RMIServer_I RMI = (RMIServer_I) Naming.lookup("rmi://localhost:5001/RMIServer");
            RMI.loginMulticastServer(this);

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

                    byte[] buffer = new Protocol().login(q.ID, voter.username, voter.getPassword()).getBytes();
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, q.getPORT());
                    socket.send(packet);

                    boolean flag = true;
                    do {
                        packet = new DatagramPacket(buffer, buffer.length);
                        socket.receive(packet);
                        Protocol protocol = new Protocol().parse(new String(packet.getData(), 0, packet.getLength()));
                        System.out.print(protocol.id+'\t'+q.ID);
                        if (protocol!=null && protocol.id!=null && protocol.id.equals(q.ID)) {
                            flag=false;
                            System.out.println("oi");
                        }
                        // if for o certo, vai
                        //System.out.println(message);

                    } while (flag);


                    // receives message saying that
                    // waits for login info
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
    private List<Voter> voting = new CopyOnWriteArrayList<Voter>();
    public String ID;
    public boolean availableTerminal = false;

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
            notify();
            while(!availableTerminal)
                wait();
        } catch(InterruptedException e) {
            System.out.println("interruptedException caught");
        } catch (Exception e) {
            System.out.println("Exception in main: " + e);
            e.printStackTrace();
        }
    }

    synchronized void findTerminal(MulticastSocket socket, InetAddress group) {
        try {
            wait();

            byte[] buffer = ("request|"+department).getBytes();  // TO DO PROTOCOL
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            socket.send(packet);

            // receives answer from thread
            buffer = new byte[256];
            packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            // sendes confirmation with id
            String message = new String(packet.getData(), 0, packet.getLength());
            buffer = message.getBytes();
            packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            socket.send(packet);
            System.out.println("MESSAGE: "+message);
            ID = message;
            availableTerminal = true;
            notify();

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

class Multicast extends Thread implements Serializable {
    Q_ok q;

    public Multicast(Q_ok q) {
        this.q = q;
    }

    synchronized public void run() {
        MulticastSocket socket = null;
        try {
            socket = new MulticastSocket(q.getRESULT_PORT());  // create socket and bind it
            InetAddress group = InetAddress.getByName(q.getMULTICAST_ADDRESS());
            socket.joinGroup(group);

            while (true) {
                // waits for results of the voting
                // q.removeVoting(username);
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

    // recebe
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