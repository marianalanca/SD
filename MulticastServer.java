// Existe um Multicast Server por cada mesa de voto que gerelocalmente os terminais de voto que lhe estão associados.  Permite aos membros da mesa realizar a funcionalidade 6 e realiza automaticamente a funcionalidade 5
import java.util.List;
import java.util.ArrayList;
import java.net.*;
import java.io.*;

public class MulticastServer extends Thread {
    private String MULTICAST_ADDRESS = "224.0.224.0";
    private int PORT = 4321;  // Client Port
    private int RMIPORT = 5001;  // Client Port
    private long SLEEP_TIME = 1200;
    private int VOTING_TABLES_COUNTER = 0;
    private List<Integer> ID_TABLE = new ArrayList<Integer>();

    public static void main(String[] args) {
        MulticastServer server = new MulticastServer();
        server.start();
        //MulticastPool pool = new MulticastPool();
        //pool.run();
    }

    public MulticastServer() {
        super("Server " + (long) (Math.random() * 1000));
    }

    public void run() {

        DatagramSocket RMISocket = null;

        MulticastSocket VTSocket = null;
        System.out.println(this.getName() + " running...");
        try {
            // fazer conexão com RMI (UDP)
            RMISocket = new DatagramSocket(RMIPORT);

            // Conection voting terminal
            VTSocket = new MulticastSocket(PORT);  // create socket for communication with voting terminal

            while (true) {

                // receive package from RMI -> está a receber do Multicast!
                byte[] buffer = new byte[1000];
                //DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                //RMISocket.receive(reply);

                // verificar se login ou não

                // procurar por thread disponível (ter contador total de threads)
                // ask for id
                buffer = "type|request;value|Thread".getBytes();
                //InetAddress groupVT = InetAddress.getByName(MULTICAST_ADDRESS);
                //DatagramPacket packet = new DatagramPacket(buffer, buffer.length, groupVT, PORT);
                //VTSocket.send(packet);

                // wait for an answer
                // send message to id giving necessary info
                // espera por resposta dos disponíveis


                try { sleep((long) (Math.random() * SLEEP_TIME)); } catch (InterruptedException e) { }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            RMISocket.close();
            VTSocket.close();
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

                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println(message);
                //String message = new String(packet.getData(), 0, packet.getLength());
                //if (message.equals("type|request;value|Thread")); // é o que se espera
                // responde com i'm here
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }
}