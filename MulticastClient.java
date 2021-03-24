import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Scanner;

class Data extends Thread{
    private String MULTICAST_ADDRESS = "224.0.224.0";
    private int PORT = 4321;  // Client Port
    private int RESULT_PORT = 4322;  // RESULT Port
    private String department, username, password;
    public String ID;
    private int TIMEOUT = 120000;
    public boolean available = true;

    public Data(String department) {
        ID = Long.toString(new Date().getTime());
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
    }

    // recebe
    public void run() {
        MulticastSocket socket = null;
        try {
            socket = new MulticastSocket(data.getPORT());  // create socket and bind it
            InetAddress group = InetAddress.getByName(data.getMULTICAST_ADDRESS());
            socket.joinGroup(group);

            MulticastSocket socketResult = new MulticastSocket(data.getRESULT_PORT());  // create socket and bind it
            InetAddress groupResult = InetAddress.getByName(data.getMULTICAST_ADDRESS());
            socketResult.joinGroup(groupResult);

            Scanner keyboardScanner = new Scanner(System.in);
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

            byte[] buffer;
            DatagramPacket packet;
            Protocol protocol;
            while (true) {
                try {
                    //if (data.available) {
                        // receives request
                        do {
                            buffer = new byte[256];
                            packet = new DatagramPacket(buffer, buffer.length);
                            socket.receive(packet);
                            protocol = new Protocol().parse(new String(packet.getData(), 0, packet.getLength()));
                        } while (protocol==null || (protocol!=null && !protocol.type.equals("request")));

                        if (protocol.department.equals(data.getDepartment())) {
                            // sends confirmation
                            buffer = (new Protocol().response(data.ID)).getBytes();
                            packet = new DatagramPacket(buffer, buffer.length, group, data.getPORT());
                            socket.send(packet);

                            // receives ack
                            do {
                                buffer = new byte[256];
                                packet = new DatagramPacket(buffer, buffer.length);
                                socket.receive(packet);
                                protocol = new Protocol().parse(new String(packet.getData(), 0, packet.getLength()));
                            } while (protocol==null || (protocol!=null && !protocol.type.equals("accepted")));
                            data.available = false;

                            if (protocol.id.equals(data.ID)){

                                // timeout = 120 seconds
                                socket.setSoTimeout(data.getTIMEOUT());

                                // receives login data
                                do {
                                    buffer = new byte[256];
                                    packet = new DatagramPacket(buffer, buffer.length);
                                    socket.receive(packet);
                                    protocol = new Protocol().parse(new String(packet.getData(), 0, packet.getLength()));
                                } while (protocol==null || protocol.id==null || (protocol!=null && !protocol.type.equals("login")));

                                // autentication
                                data.setUsername(protocol.username);
                                data.setPassword(protocol.password);

                                System.out.println("Welcome to eVoting");

                                // enumeração das listas
                                boolean usernameFlag = true, passwordFlag = true;
                                String username;
                                String password;
                                do {
                                    long startTime = System.currentTimeMillis();
                                    System.out.print("Insert username: ");

                                    while ((System.currentTimeMillis() - startTime) < 60000 && !in.ready()) {}
                                    if (in.ready()) {
                                        username = in.readLine();
                                        if (username.equals(data.getUsername())) {
                                            do {
                                                usernameFlag = false;
                                                System.out.print("Insert Password: ");
                                                while ((System.currentTimeMillis() - startTime) < 60000 && !in.ready()) {}
                                                if (in.ready()) {
                                                    password = in.readLine();
                                                    if (password.equals(data.getPassword())) {
                                                        passwordFlag = false;
                                                        // send confirmation
                                                        buffer = (new Protocol().status(data.ID, "on")).getBytes();
                                                        packet = new DatagramPacket(buffer, buffer.length, group, data.getPORT());
                                                        socket.send(packet);
        
                                                        // wait for list
                                                        do {
                                                            buffer = new byte[256];
                                                            packet = new DatagramPacket(buffer, buffer.length);
                                                            socket.receive(packet);
                                                            protocol = new Protocol().parse(new String(packet.getData(), 0, packet.getLength()));
                                                        } while (protocol==null || (protocol!=null && protocol.id!=null && protocol.item_name!=null && !protocol.type.equals("item_list") && protocol.id.equals(data.ID)));
        
                                                        System.out.println("ELECTION NAME");
                                                        System.out.println("LIST OF CANDIDATES");
                                                        for (String candidate: protocol.item_name){
                                                            System.out.println(candidate);
                                                        }
                                                        //System.out.println("White");
        
                                                        boolean flag = true;
                                                        int selection = protocol.item_count; // white
                                                        do {
                                                            try {
                                                                System.out.println("Select one candidate from the list");
                                                                selection = Integer.valueOf(keyboardScanner.nextLine());
                                                                if (selection <= protocol.item_count && selection >= 0) // change to > 0
                                                                    flag = false;
                                                            } catch (NoSuchElementException e) {}
                                                        } while(flag);
        
                                                        // send vote to MultiCast Server
                                                        buffer = (new Protocol().vote(data.ID, username, "election"/*protocol.item_name.get(selection-1)*/)).getBytes();
                                                        packet = new DatagramPacket(buffer, buffer.length, groupResult, data.getRESULT_PORT());
                                                        socketResult.send(packet);
        
                                                        do {
                                                            buffer = new byte[256];
                                                            packet = new DatagramPacket(buffer, buffer.length);
                                                            socket.receive(packet);
                                                            protocol = new Protocol().parse(new String(packet.getData(), 0, packet.getLength()));
                                                        } while (protocol==null || (protocol!=null && protocol.id!=null && !protocol.type.equals("status") && !protocol.logged.equals("off") && protocol.id.equals(data.ID)));
                                                        System.out.println("ACKNOWLEDGED");
        
                                                    } else {
                                                        System.out.println("Wrong password");
                                                    }
                                                } else {
                                                    System.out.println("\nThe terminal has been block for inaction");
                                                    usernameFlag = false;
                                                    passwordFlag = false;
                                                }
                                            } while (passwordFlag);
                                        } else if (username.equals("\n")) {}
                                        else {
                                            System.out.println("Wrong username");
                                        }
                                    } else {
                                        System.out.println("\nThe terminal has been block for inaction");
                                        usernameFlag = false;
                                        passwordFlag = false;
                                    }
                                } while (usernameFlag);
                                // enviar mensagem a dizer que login feito e que precisa da lista

                            } else {
                                continue;
                            }
                        } else {
                            continue;
                        }
                   // }
                } catch (SocketTimeoutException e) {
                    System.out.println("The terminal has been idle for too long");
                }
            }
        }  catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }
}
