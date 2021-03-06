import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.Properties;

class Data{
    private String MULTICAST_ADDRESS;
    private int PORT;  // Client Port
    private int RESULT_PORT;  // RESULT Port
    private String department, username, password;
    private String ID;
    private int TIMEOUT = 120;
    MulticastSocket socket = null;
    MulticastSocket socketResult = null;
    InetAddress group, groupResult;
    private List<Long> registeredAcks = new CopyOnWriteArrayList<Long>();

    public Data(String department) {
        ID = Long.toString(Math.abs(new Random(System.currentTimeMillis()).nextLong()));
        this.department = department;
        try{
            Properties prop = new Properties();
            String fileName = "config.properties";
            prop.load(new FileInputStream(fileName));
            MULTICAST_ADDRESS = prop.getProperty("multicast_adress");
            PORT = Integer.parseInt(prop.getProperty("multicast_port"));
            RESULT_PORT = Integer.parseInt(prop.getProperty("results_port"));
      }catch(Exception e){ // standard values
            MULTICAST_ADDRESS = "224.0.224.0";
            PORT = 4321;
            RESULT_PORT = 4322;
      }
    }

    /**
     * @param password of the current user, to be stored
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return String of the current user
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param username of the current user, to be stored
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return String of the current user
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return int with the value of the client Port
     */
    public int getPORT() {
        return PORT;
    }

    /**
     * @return int with the value of the port where the vote result is received
     */
    public int getRESULT_PORT() {
        return RESULT_PORT;
    }

    /**
     * @param ID of the current user, to be stored
     */
    public void setID(String ID) {
        this.ID = ID;
    }

    /**
     * @return int with the value of the messages timeout
     */
    public int getTIMEOUT() {
        return TIMEOUT;
    }

    /**
     * @return String with the department of the terminal
     */
    public String getDepartment() {
        return department;
    }

    /**
     * @return String containg the multicast address
     */
    public String getMULTICAST_ADDRESS() {
        return MULTICAST_ADDRESS;
    }

    /**
     * @return List<Long> with the id of all messages received
     */
    public List<Long> getRegisteredAcks() {
        return registeredAcks;
    }

    /**
     * @return String containing the terminal ID
     */
    public String getID() {
        return ID;
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

    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    Thread.sleep(200);
                    System.out.println("\nThe terminal has crashed. Logging out ...");
                    byte[] buffer = (new Protocol().crashed(data.getID(), data.getDepartment())).getBytes();
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, data.group, data.getPORT());
                    data.socket.send(packet);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (IOException e) {
                    System.out.println("Your request has been lost. Please try again.");
                }
            }
        });


        Scanner keyboardScanner = new Scanner(System.in);
        try {
            data.socket = new MulticastSocket(data.getPORT());  // create socket and bind it
            data.group = InetAddress.getByName(data.getMULTICAST_ADDRESS());
            data.socket.joinGroup(data.group);
            try{
                data.socketResult = new MulticastSocket(data.getRESULT_PORT());  // create socket and bind it
                data.groupResult = InetAddress.getByName(data.getMULTICAST_ADDRESS());
                data.socketResult.joinGroup(data.groupResult);

                while (true) {
                    vote(data.socket, data.group, keyboardScanner, data.socketResult, data.groupResult);
                }
            } catch (Exception e) {
            }
        }  catch (IOException e) {
        } finally {
            data.socket.close();
            data.socketResult.close();
            keyboardScanner.close();
        }
    }

    public void vote(MulticastSocket socket, InetAddress group,Scanner keyboardScanner, MulticastSocket socketResult,InetAddress groupResult) throws IOException, ExecutionException, InterruptedException {
        byte[] buffer;
        DatagramPacket packet;
        Protocol protocol;
        try {
            // receives request
            socket.setSoTimeout(2000);
            do {
                buffer = new byte[256];
                packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                protocol = new Protocol().parse(new String(packet.getData(), 0, packet.getLength()));
            } while (protocol==null || !(protocol!=null && protocol.type.equals("request") && !data.getRegisteredAcks().contains(protocol.msgId)));

            data.getRegisteredAcks().add(protocol.msgId);

            if (protocol.department.equals(data.getDepartment())) {
                // sends confirmation
                buffer = (new Protocol().response(data.getDepartment() ,data.getID())).getBytes();
                packet = new DatagramPacket(buffer, buffer.length, group, data.getPORT());
                socket.send(packet);

                // receives ack
                do {
                    buffer = new byte[256];
                    packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    protocol = new Protocol().parse(new String(packet.getData(), 0, packet.getLength()));
                    if (protocol!=null && protocol.type.equals("turnoff") && protocol.department.equals(data.getDepartment())){
                        System.out.println("\nThe table has exited. ");
                        return;
                    }
                } while (protocol==null || !(protocol!=null && protocol.type.equals("accepted") && !data.getRegisteredAcks().contains(protocol.msgId)));

                System.out.print("");

                data.getRegisteredAcks().add(protocol.msgId);

                if (protocol.id.equals(data.getID())){

                    // receives login data
                    do {
                        buffer = new byte[256];
                        packet = new DatagramPacket(buffer, buffer.length);
                        socket.receive(packet);
                        protocol = new Protocol().parse(new String(packet.getData(), 0, packet.getLength()));
                        if (protocol!=null && protocol.type.equals("turnoff") && protocol.department.equals(data.getDepartment())){
                            System.out.println("\nThe table has exited. ");
                            return;
                        }
                    } while (protocol==null || !(protocol!=null && protocol.id!=null && protocol.type.equals("login")));

                    // send ack telling it has received login
                    buffer = (new Protocol().ack(data.getID(), data.getDepartment())).getBytes();
                    packet = new DatagramPacket(buffer, buffer.length, group, data.getPORT());
                    socket.send(packet);

                    socket.setSoTimeout(10000); // waits for 10 seconds

                    // autentication
                    data.setUsername(protocol.username);
                    data.setPassword(protocol.password);

                    System.out.println("Welcome to eVoting");

                    // enumera????o das listas
                    boolean usernameFlag = true, passwordFlag = true;
                    String username;
                    String password;
                    int counter = 0;

                    try {
                        do {
                            System.out.print("Insert username: ");
                            username =  getTimeConsole(keyboardScanner, data.getTIMEOUT());
                            if (++counter <3) {
                                if (username.equals(data.getUsername())) {
                                    counter = 0;
                                    do {
                                        usernameFlag = false;
                                        System.out.print("Insert Password: ");
                                        password = getTimeConsole(keyboardScanner, data.getTIMEOUT());
                                        if (++counter < 3) {
                                            if (password.equals(data.getPassword())) {
                                                passwordFlag = false;
                                                // send confirmation
                                                buffer = (new Protocol().status(data.getID(), data.getDepartment(), "on")).getBytes();
                                                packet = new DatagramPacket(buffer, buffer.length, group, data.getPORT());
                                                socket.send(packet);
                                                // wait for list of elections
                                                do {
                                                    buffer = new byte[256];
                                                    packet = new DatagramPacket(buffer, buffer.length);
                                                    socket.receive(packet);
                                                    protocol = new Protocol().parse(new String(packet.getData(), 0, packet.getLength()));
                                                    if (protocol!=null && protocol.type.equals("turnoff") && protocol.department.equals(data.getDepartment())){
                                                        System.out.println("\nThe table has exited. ");
                                                        return;
                                                    }
                                                } while (protocol==null || !(protocol!=null && protocol.id!=null && protocol.item_name!=null && protocol.type.equals("item_list") && protocol.key!=null && protocol.key.equals("election") && protocol.id.equals(data.getID())));

                                                if (protocol.item_count==0) {
                                                    System.out.println("There are no elections available at the moment");
                                                    buffer = (new Protocol().leave(data.getID(), data.getDepartment())).getBytes();
                                                    packet = new DatagramPacket(buffer, buffer.length, group, data.getPORT());
                                                    socket.send(packet);
                                                    return;
                                                } else {
                                                    System.out.println("List of Elections");
                                                    for (int i=0;i<protocol.item_count;i++){
                                                        System.out.println("\t"+i+". "+ protocol.item_name.get(i));
                                                    }
                                                    boolean flag = true;
                                                    int selection = 0;
                                                    do {
                                                        System.out.print("Select one election from the list: ");
                                                        selection = getIntTimeConsole(keyboardScanner, data.getTIMEOUT());
                                                        if (selection < protocol.item_count && selection >= 0)
                                                            flag = false;
                                                        else System.out.print("The option you've chosen is not possible. ");
                                                    } while(flag);

                                                    String electionName = protocol.item_name.get(selection);

                                                    // send request for the list of candidates
                                                    buffer = (new Protocol().election(data.getID(), data.getDepartment(), electionName)).getBytes();
                                                    packet = new DatagramPacket(buffer, buffer.length, group, data.getPORT());
                                                    socket.send(packet);
                                                    do {
                                                        buffer = new byte[256];
                                                        packet = new DatagramPacket(buffer, buffer.length);
                                                        socket.receive(packet);
                                                        protocol = new Protocol().parse(new String(packet.getData(), 0, packet.getLength()));
                                                        if (protocol!=null && protocol.type.equals("turnoff") && protocol.department.equals(data.getDepartment())){
                                                            System.out.println("\nThe table has exited. ");
                                                            return;
                                                        }
                                                    } while (protocol==null || !(protocol!=null && protocol.id!=null && protocol.item_name!=null && protocol.type.equals("item_list") && protocol.key!=null && protocol.key.equals("candidate") && protocol.id.equals(data.getID())));

                                                    if (protocol.item_count==0){ 
                                                        System.out.println("There are no candidates available");
                                                        buffer = (new Protocol().leave(data.getID(), data.getDepartment())).getBytes();
                                                        packet = new DatagramPacket(buffer, buffer.length, group, data.getPORT());
                                                        socket.send(packet);
                                                        return;
                                                    } else {
                                                        System.out.println("List of Candidates");
                                                        for (int i=0;i<protocol.item_count;i++){
                                                            System.out.println("\t"+i+". "+ protocol.item_name.get(i));
                                                        }
                                                        System.out.println("\t"+(protocol.item_count)+". White");

                                                        flag = true;
                                                        selection = 0;
                                                        do {
                                                            System.out.print("Select one election from the list: ");
                                                            selection = getIntTimeConsole(keyboardScanner, data.getTIMEOUT());
                                                            if (selection <= protocol.item_count && selection >= 0)
                                                                flag = false;
                                                            else System.out.print("The option you've chosen is not possible. ");
                                                        } while(flag);


                                                        String votedCantidate;
                                                        if ( selection == protocol.item_count)
                                                            votedCantidate = "White";
                                                        else
                                                            votedCantidate = protocol.item_name.get(selection);

                                                        // send vote to MultiCast Server
                                                        buffer = (new Protocol().vote(data.getID(), data.getDepartment(), data.getUsername(), electionName, votedCantidate)).getBytes();
                                                        packet = new DatagramPacket(buffer, buffer.length, groupResult, data.getRESULT_PORT());
                                                        socketResult.send(packet);
                                                        do {
                                                            buffer = new byte[256];
                                                            packet = new DatagramPacket(buffer, buffer.length);
                                                            socket.receive(packet);
                                                            protocol = new Protocol().parse(new String(packet.getData(), 0, packet.getLength()));
                                                        } while (protocol==null || !(protocol!=null && protocol.id!=null && protocol.type.equals("status") && protocol.logged.equals("off") && protocol.id.equals(data.getID())));

                                                        if (protocol.msg!=null) {
                                                            System.out.println(protocol.msg);
                                                        }
                                                    }
                                                }
                                            } else {
                                                System.out.println("Wrong password");
                                            }
                                        } else { // in case failed to insert password
                                            failedLogin(group, socket);
                                            return;
                                        }
                                    } while (passwordFlag);
                                } else {
                                    System.out.println("Wrong username");
                                }
                            } else { // in case failed to insert login
                                failedLogin(group, socket);
                                return;
                            }
                        } while (usernameFlag);
                    } catch (TimeoutException e) {
                        System.out.println("The terminal has been idle for too long");
                        buffer = (new Protocol().leave(data.getID(), data.getDepartment())).getBytes();
                        packet = new DatagramPacket(buffer, buffer.length, group, data.getPORT());
                        socket.send(packet);
                        return;
                    } catch (SocketTimeoutException e) { System.out.println("Something went wrong");
                    } catch (Exception e) {
                        System.exit(0);
                    }
                }
            }
        } catch (SocketTimeoutException e) {}
    }

    public void failedLogin(InetAddress group, MulticastSocket socket) throws IOException {
        System.out.println("You have failed to login");
        byte[] buffer = (new Protocol().leave(data.getID(), data.getDepartment())).getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, data.getPORT());
        socket.send(packet);
    }

    public String getTimeConsole(Scanner scanner, int time) throws NoSuchElementException, ExecutionException, InterruptedException, TimeoutException {
        String result;

        FutureTask<String> task = new FutureTask<>(() -> {
            try {
                return scanner.nextLine();
            } catch (NoSuchElementException e) {
                return "error";
            } catch (Exception e) {
                return "";
            }
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
        result = task.get(time, TimeUnit.SECONDS);
        if (result.equals("error")) {
            throw new NoSuchElementException();
        }

        return result;
    }

    public int getIntTimeConsole(Scanner scanner, int time) throws NoSuchElementException, ExecutionException, InterruptedException, TimeoutException {
        try {
            return Integer.valueOf(getTimeConsole(scanner, time));
        } catch (NumberFormatException e)  {
            System.out.print("The inserted value is not valid. Try again ");
            return getIntTimeConsole(scanner, time);
        }
    }
}
