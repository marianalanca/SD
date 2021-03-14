// São  os  clientes  Multicast  que  estão  associados  a  cada  mesade voto, e que permitem realizar as funcionalidades 7 e 8.  Recomenda-se que oterminal de voto utilize dois grupos de Multicast distintos: um para descobrir e serdescoberto pelo servidor (por exemplo, quando o servidor precisa de um terminallivre) e outro grupo apenas para comunicar a intenção de voto do eleitor.
import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.IOException;

public class VotingTerminal extends Thread {
    private String MULTICAST_ADDRESS = "224.0.224.0";
    private int PORT = 4321;
    private long SLEEP_TIME = 5000; // é para ser 120 segundos!

    public static void main(String[] args) {
        VotingTerminal terminal = new VotingTerminal();
        terminal.start();
    }

    public VotingTerminal() {
        super("Terminal " + (long) (Math.random() * 1000));
    }

    public void run() {
        MulticastSocket socket = null;
        long counter = 0;
        System.out.println(this.getName() + " running...");
        try {
            socket = new MulticastSocket();  // create socket without binding it (only for sending)
            while (true) {
                String message = this.getName() + " packet " + counter++;
                byte[] buffer = message.getBytes();

                InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                socket.send(packet);

                try { sleep((long) (Math.random() * SLEEP_TIME)); } catch (InterruptedException e) { }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }
}
