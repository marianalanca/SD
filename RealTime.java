import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.*;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;

public class RealTime extends UnicastRemoteObject{

    RMIServer_I rmi;  
    String address;
    int port;

    private RealTime() throws RemoteException {
        super();
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

            Scanner myObj;
            List <Election> elections;
            Election election;
            int option;

            elections = rmi.stateElections(State.OPEN, null);

            if(elections.size() == 0){ 
                System.out.println("List of open elections is empty.");
                return; 
            }

            (new Thread() {
                public void run() {
                    try {
                        while(true){
                            try {
                                List<Candidates> candidates = election.getCandidatesList();

                                for(int i = 0; i < candidates.size(); i++){
                                    Candidates cand = candidates.get(i);
                                    System.out.println(cand.getName() + ": " + cand.getNumberOfVotes());
                                }
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }

                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }).start();

            /*
            candidates = election.getCandidatesList();

            for(int i = 0; i < candidates.size(); i++){
                cand = candidates.get(i);
                System.out.println(cand.getName() + ": " + cand.getNumberOfVotes());
            }*/
        }
        catch(ConnectException e){
            reconnect();
            voters_real_time();
        }
        catch (Exception e){
            System.out.println("Voters_real_time: " + e);
        }
    }

    public static void main(String args[]) {

        RealTime real = new RealTime();
        real.read();
        real.real_time();

    }
}