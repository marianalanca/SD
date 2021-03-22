// Existe um Multicast Server por cada mesa de voto que gerelocalmente os terminais de voto que lhe estão associados.  Permite aos membros da mesa realizar a funcionalidade 6 e realiza automaticamente a funcionalidade 5
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.rmi.*;


public class MulticastServer extends Thread{
    Q_ok q;
    public static void main(String[] args) {
        // recebe departamento da consola?
        if (args.length == 0) {
            System.out.println("java MulticastServer department");
            System.exit(0); // termina
        }
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

            RMIServer_I RMI = (RMIServer_I) Naming.lookup("rmi://localhost:5001/RMIServer");

            Scanner keyboardScanner = new Scanner(System.in);

            while (true) {

                // receber por informações de novos eleitores a quererem autenticar-se
                System.out.print("Insert your CC: ");
                String readKeyboard = keyboardScanner.nextLine();

                //Voter voter = RMI.searchVoterCc(readKeyboard);
                // TEST
                Voter voter = new Voter("test", "inform", "987654123","morada", "123456789",null,"pass", null);
                if((voter!=null && voter.department.equals(q.getDepartment()))){

                    // Send request for threads
                    System.out.println("#DEBUG notify");
                    q.requestTerminal(socket);

                    byte[] buffer = ("type|login;id|"+q.ID+";username|"+voter.username+";password|"+voter.getPassword()).getBytes();  // TO DO PROTOCOL
                    InetAddress group = InetAddress.getByName(q.getMULTICAST_ADDRESS());
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, q.getPORT());
                    socket.send(packet);

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

}

class Q_ok extends Thread{
    private String MULTICAST_ADDRESS = "224.0.224.0";
    private int PORT = 4321;  // Client Port
    private String department;
    private List<Voter> members = new CopyOnWriteArrayList<Voter>();
    public String ID;
    public boolean availableTerminal = false;

    public Q_ok(String department) {
        this.department = department;
    }

    public int getPORT() {
        return PORT;
    }

    public String getDepartment() {
        return department;
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

class Multicast extends Thread implements Runnable {
    Q_ok q;

    public Multicast(Q_ok q) {
        this.q = q;
    }

    synchronized public void run() {
        // recebe resultados dos votos!
        // multicast próprio!
    }

}

// waits for new threads and contacts with them
class MulticastPool extends Thread {
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