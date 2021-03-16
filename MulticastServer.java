// Existe um Multicast Server por cada mesa de voto que gerelocalmente os terminais de voto que lhe estão associados.  Permite aos membros da mesa realizar a funcionalidade 6 e realiza automaticamente a funcionalidade 5
import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.IOException;
import java.util.ArrayList;

public class MulticastServer extends Thread {
    private String MULTICAST_ADDRESS = "224.0.224.0";
    private int PORT = 4321;
    private long SLEEP_TIME = 5000; // é para ser 120 segundos!
    static public ArrayList<VotingTerminal> terminals = new ArrayList<VotingTerminal>(); // quando necessário vai-se buscar

    public static void main(String[] args) {

        // cria terminais
        for (int i=0;i<10;i++) {
            terminals.add(new VotingTerminal());
            terminals.get(i).run();
            System.out.println(terminals.get(i).isAlive());
        }

        //System.out.println("oi");

        MulticastServer server = new MulticastServer();
        server.start();
        // adicionar ao array 10 terminais
    }

    public boolean findVoter (String id) {
        // CONSULTAR RMI SERVER
        return false;
    }

    public MulticastServer() {
        super("Server " + (long) (Math.random() * 1000));
    }

    public void run() {
        MulticastSocket socket = null;
        long counter = 0;
        System.out.println(this.getName() + " running...");
        try {
            socket = new MulticastSocket(PORT);  // create socket without binding it (only for sending)
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            socket.joinGroup(group);
            while (true) {
                byte[] buffer = new byte[256]; // recebe CC

                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength());

                // pedir ao RMI para procurar
                // type | find ; what | cc_number ; value | + message

                // se encontrar, procura na lista
                for (VotingTerminal terminal: terminals) {
                    if (!terminal.isAlive()) {
                        terminal.run();
                        break;
                    }
                }

                /*System.out.println("Received packet from " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " with message:");
                String message1 = new String(packet.getData(), 0, packet.getLength());
                System.out.println(message1);*/

                try { sleep((long) (Math.random() * SLEEP_TIME)); } catch (InterruptedException e) { }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }
}
