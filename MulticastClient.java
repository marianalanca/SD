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
public class MulticastClient extends Thread {
    private String MULTICAST_ADDRESS = "224.0.224.0";
    private int PORT = 4321;
    private String ID;

    public static void main(String[] args) {
        MulticastClient client = new MulticastClient();
        client.start();
        MulticastUser user = new MulticastUser();
        user.start();
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

                System.out.println("Received packet from " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " with message:");

                // fazer parse da mensagem
                // se do tipo join, guardar thread ID
                String message = new String(packet.getData(), 0, packet.getLength());

                String[] tokens = message.split(";");
                int operation = 0;
                // if operation = 1 -> JOIN

                for (String string : tokens) {
                    String[] token = string.split("\\|");

                    if (operation != 0) {
                        if (operation == 1) { // join
                            if (token[0].equals("ThreadID")) {
                                ID = token[1];
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

// envia
class MulticastUser extends Thread {
    private String MULTICAST_ADDRESS = "224.0.224.0";
    private int PORT = 4321;
    private String ID;

    public MulticastUser() {
        super();
        ID = Long.toString((long) (Math.random() * 1000));
    }

    public void run() {
        MulticastSocket socket = null;
        try {
            socket = new MulticastSocket();  // create socket without binding it (only for sending)

            byte[] buffer = ("type|Join;ThreadID|"+ID).getBytes();

            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            socket.send(packet);

            Scanner keyboardScanner = new Scanner(System.in);
            while (true) {
                String readKeyboard = keyboardScanner.nextLine();
                buffer = readKeyboard.getBytes();

                group = InetAddress.getByName(MULTICAST_ADDRESS);
                packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                socket.send(packet);
            }
            //keyboardScanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }
}
