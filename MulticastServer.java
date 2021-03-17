// Existe um Multicast Server por cada mesa de voto que gerelocalmente os terminais de voto que lhe estão associados.  Permite aos membros da mesa realizar a funcionalidade 6 e realiza automaticamente a funcionalidade 5
import java.util.List;
import java.util.ArrayList;
import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.rmi.*;

public class MulticastServer extends Thread {
    private String MULTICAST_ADDRESS = "224.0.224.0";
    private int PORT = 4321;  // Client Port
    private long SLEEP_TIME = 1200;

    public static void main(String[] args) {
        MulticastServer server = new MulticastServer();
        server.start();
        MulticastPool pool = new MulticastPool();
        pool.run();
    }

    public MulticastServer() {
        super("Server " + (long) (Math.random() * 1000));
    }

    public void run() {

        MulticastSocket socket = null;
        System.out.println(this.getName() + " running...");
        try {
            // Conection voting terminal
            socket = new MulticastSocket(PORT);  // create socket for communication with voting terminal

            RMIServer_I RMI = (RMIServer_I) Naming.lookup("rmi://localhost:5001/RMIServer");

            Scanner keyboardScanner = new Scanner(System.in);

            while (true) {

                String readKeyboard = keyboardScanner.nextLine();

                String[] tokens = readKeyboard.split(";");

                for (String string : tokens) {
                    String[] token = string.split("\\|");

                    if (token[0].equals("cc")) {
                        // chamar RMI
                        if(RMI.searchVoterCc(token[1])!=null){
                            byte[] buffer = "request".getBytes();  // TO DO PROTOCOL
                            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                            socket.send(packet);

                            // receives answer from a thread
                            buffer = new byte[256];
                            packet = new DatagramPacket(buffer, buffer.length);
                            socket.receive(packet);

                            // sendes confirmation with id
                            String message = new String(packet.getData(), 0, packet.getLength());
                            buffer = message.getBytes();
                            packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                            socket.send(packet);
                        } else {
                            System.out.println("Wrong format");
                        }
                    }
                }
                // no multicast server server chamar search user
                // chamar pool

                // verificar se login ou não

                // search for available thread
                // ask for id




                //try { sleep((long) (Math.random() * SLEEP_TIME)); } catch (InterruptedException e) { }
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

class MulticastPool extends Thread {
    private String MULTICAST_ADDRESS = "224.0.224.0";
    private int PORT = 4321;

    public MulticastPool() {
        super();
    }

    // recebe
    public void run() {
        MulticastSocket socket = null;
        try {
            socket = new MulticastSocket(PORT);  // create socket and bind it
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            socket.joinGroup(group);
            while (true) {
                byte[] buffer = new byte[256];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String newThreadID = new String(packet.getData(), 0, packet.getLength());

                String[] tokens = newThreadID.split(";");
                int operation = 0;
                // if operation = 1 -> JOIN

                for (String string : tokens) {
                    String[] token = string.split("\\|");

                    if (operation != 0) {
                        if (operation == 1) { // join
                            if (token[0].equals("ThreadID")) {
                                System.out.println("Thread " + token[1] + " has joined.");
                            }
                        }
                    } else {
                        if(token[0].equals("type") && token[1].equals("Join")){
                            operation = 1;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }
}