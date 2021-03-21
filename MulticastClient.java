import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.IOException;
import java.util.Scanner;

/**
 * The MulticastClient class joins a multicast group and loops receiving
 * messages from that group. The client also runs a MulticastUser thread that
 * loops reading a string from the keyboard and multicasting it to the group.
 * <p>
 * The example IPv4 address chosen may require you to use a VM option to
 * prefer IPv4 (if your operating system uses IPv6 sockets by default).
 * <p>
 * Usage: java -Djava.net.preferIPv4Stack=true MulticastClient
 *
 * @author Raul Barbosa
 * @version 1.0
 */

class Data extends Thread{
    private String MULTICAST_ADDRESS = "224.0.224.0";
    private int PORT = 4321;  // Client Port
    private String department;
    public String ID;
    private int TIMEOUT = 120000;

    public Data(String department) {
        ID = Long.toString((long) (Math.random() * 1000));
        this.department = department;
    }

    public int getPORT() {
        return PORT;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public int getTIMEOUT() {
        return TIMEOUT;
    }

    public String getDepartment() {
        return department;
    }

    public String getMULTICAST_ADDRESS() {
        return MULTICAST_ADDRESS;
    }

    public String receivedMessage(Protocol protocol) {
        // test if id is the correct; if so -> return
        return null;
    }
}

public class MulticastClient extends Thread {
    private static Data data;

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("java MulticastClient department");
            System.exit(0); // termina
        }
        data = new Data(args[0]);

        MulticastClient client = new MulticastClient();
        client.start();
        MulticastUser user = new MulticastUser(data);
        user.start();
    }

    // recebe
    public void run() {
        MulticastSocket socket = null;
        try {
            socket = new MulticastSocket(data.getPORT());  // create socket and bind it
            InetAddress group = InetAddress.getByName(data.getMULTICAST_ADDRESS());
            socket.joinGroup(group);

            //System.out.println("#DEBUG MULTICASTCLIENT ID = "+ data.ID);
            byte[] buffer = new byte[256];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            while (true) {

                buffer = new byte[256];
                packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                // fazer parse da mensagem
                // se do tipo join, guardar thread ID
                String message = new String(packet.getData(), 0, packet.getLength());

                if (message.equals("request|"+data.getDepartment())) {
                    buffer = data.ID.getBytes();
                    packet = new DatagramPacket(buffer, buffer.length, group, data.getPORT());
                    socket.send(packet);

                    buffer = new byte[256];
                    packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    message = new String(packet.getData(), 0, packet.getLength());

                    if (message.equals(data.ID)) {
                        // timeout = 120 seconds
                        socket.setSoTimeout(data.getTIMEOUT());

                        // receives login data
                        buffer = new byte[256];
                        packet = new DatagramPacket(buffer, buffer.length);
                        socket.receive(packet);
                        message = new String(packet.getData(), 0, packet.getLength());
                        System.out.println(message);


                        // autentication
                        String username, password;

                        // enumeração das listas
                        System.out.println("Insert username: ");

                        // enviar mensagem a dizer que login feito e que precisa da lista

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

// envia
class MulticastUser extends Thread {
    private Data data;

    public MulticastUser(Data data) {
        super();
        this.data = data;
    }

    public void run() {
        MulticastSocket socket = null;
        try {
            socket = new MulticastSocket();  // create socket without binding it (only for sending)

            //System.out.println("#DEBUG MULTICASTUSER ID = "+ data.ID);

            Scanner keyboardScanner = new Scanner(System.in);
            while (true) {
                String readKeyboard = keyboardScanner.nextLine();
                byte[] buffer = readKeyboard.getBytes();

                InetAddress group = InetAddress.getByName(data.getMULTICAST_ADDRESS());
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, data.getPORT());
                socket.send(packet);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }
}
