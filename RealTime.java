import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.*;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;

public class RealTime extends UnicastRemoteObject{

    RMIServer_I rmi;  
    String address;
    int port;

    private RealTime() throws RemoteException {

    }

    public static void clearConsole() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            }
            else {
                System.out.print("\033\143");
            }
        } catch (IOException | InterruptedException ex) {
            
        }
    }

    public void read(){
        try{
            Properties prop = new Properties();
            String fileName = "config.properties";
            
            prop.load(new FileInputStream(fileName));
            port = Integer.parseInt(prop.getProperty("port"));
            address = prop.getProperty("ip");
            rmi = (RMIServer_I) LocateRegistry.getRegistry(address, port).lookup("RMIServer");

        }catch(Exception e){
            System.out.println("Error in the file config.properties: " + e);
            System.exit(-1);   
        }
    }

    public void reconnect(){
        try{
            rmi = (RMIServer_I) LocateRegistry.getRegistry(address, port).lookup("RMIServer");
        }
        catch(ConnectException e){
            reconnect();
        }
        catch(Exception e){
            System.out.println("reconnect and I are not friends :) " + e);
        }
    }

    public void real_time(){

        try{

            Scanner myObj = new Scanner(System.in);
            List <Election> elections;
            Election election;
            int option = -1, size;
            String aux;

            elections = rmi.stateElections(State.OPEN, null);

            size = elections.size();
            if( size == 0){ 
                System.out.println("List of open elections is empty.");
                myObj.close();
                return; 
            }

            System.out.println("\nPick a election:");
            for(int i =0; i < size ; i++){
                System.out.println(i + ". " + elections.get(i).getTitle());
            }

            do{
                aux = myObj.nextLine();
                try{
                    option = Integer.parseInt(aux);
                    break;
                }
                catch(Exception e){
                    System.out.print("Error in parse.\n Try again: ");
                }
            }while(option < 0 || option >= elections.size());
    
            election = elections.get(option);

            myObj.close();

            (new Thread() {
                public void run() {
                    try {
                        while(true){
                            try {
                                List<Candidates> candidates = election.getCandidatesList();

                                clearConsole();

                                System.out.println(election.getTitle() + ":\n");

                                for(int i = 0; i < candidates.size(); i++){
                                    Candidates cand = candidates.get(i);
                                    System.out.println(cand.getName() + ":" + cand.getNumberOfVotes());
                                }

                                System.out.println("Null votes: " + election.getNullVote());
                                System.out.println("White votes: " + election.getWhiteVote());

                                if(election.getEndDate().before(Calendar.getInstance())){
                                    Thread.currentThread().interrupt();
                                }

                                Thread.sleep(2000);
                                
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                    catch (Exception e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }).start();

        }
        catch(ConnectException e){
            reconnect();
            real_time();
        }
        catch (Exception e){
            System.out.println("Voters_real_time: " + e);
        }
    }

    public static void main(String args[]) {

        try {
            RealTime real = new RealTime();
            real.read();
            real.real_time();
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(0);
        }

    }
}