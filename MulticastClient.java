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
    private int RESULT_PORT = 4322;  // RESULT Port
    private String department, username, password;
    public String ID;
    private int TIMEOUT = 120000;

    public Data(String department) {
        ID = Long.toString((long) (Math.random() * 1000));
        this.department = department;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public int getPORT() {
        return PORT;
    }

    public int getRESULT_PORT() {
        return RESULT_PORT;
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
}

public class MulticastClient extends Thread {
    private static Data data;

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("java MulticastClient department");
            System.exit(0);
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
            Scanner keyboardScanner = new Scanner(System.in);

            //System.out.println("#DEBUG MULTICASTCLIENT ID = "+ data.ID);
            byte[] buffer = new byte[256];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            // fazer while packet != request e id for o errado -> fazer isto em todos os receives
            socket.receive(packet);

            while (true) {

                buffer = new byte[256];
                packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                // fazer parse da mensagem
                // se do tipo join, guardar thread ID
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println(message);

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
                        Protocol login = new Protocol();
                        login.parse(message);
                        // autentication
                        data.setUsername(login.username);
                        data.setPassword(login.password);

                        System.out.println(data.getUsername() + '\t'+ data.getPassword());

                        // enumeração das listas
                        boolean usernameFlag = true, passwordFlag = true;
                        do {
                            System.out.print("Insert username: ");
                            String username = keyboardScanner.nextLine();
                            if (username.equals(data.getUsername())) {
                                do {
                                    usernameFlag = false;
                                    System.out.print("Insert Password: ");
                                    String password = keyboardScanner.nextLine();
                                    if (password.equals(data.getPassword())) {
                                        passwordFlag = false;
                                        buffer = (new Protocol().status(data.ID, "on")).getBytes();
                                        packet = new DatagramPacket(buffer, buffer.length, group, data.getPORT());
                                        socket.send(packet);

                                    } else {
                                        System.out.println("Wrong password");
                                    }

                                } while (passwordFlag);
                            }else {
                                System.out.println("Wrong username");
                            }


                        } while (usernameFlag);
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

// sends result
class MulticastUser extends Thread {
    private Data data;

    public MulticastUser(Data data) {
        super();
        this.data = data;
    }

    public void run() {
        MulticastSocket socket = null;
        try {
            socket = new MulticastSocket(data.getRESULT_PORT());  // create socket and bind it
            InetAddress group = InetAddress.getByName(data.getMULTICAST_ADDRESS());
            socket.joinGroup(group);

            while (true) {
                // when voter has voted, send
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
			System.out.println("Exception in Multicast Client: " + e);
			e.printStackTrace();
        } finally {
            socket.close();
        }
    }
}
