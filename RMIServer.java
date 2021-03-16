// É o servidor central (replicado) que armazena todos os dados da aplicação, suportando por essa razão todas as operações necessárias através demétodos remotos usando Java RMI
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
/*Por os restantes objects que podem ser passados */ 
public class RMIServer extends UnicastRemoteObject implements RMIServer_I {
      /**
       *
       */
      private static final long serialVersionUID = -7161055300561474003L;
      
      private List<Voter> voterList = new ArrayList<>();
      private List<Election> elections;
      static List<AdminConsole> admins;
      static List<MulticastServer> servers;

      @Override
      public Voter searchVoter(String username)throws RemoteException{
            for (Voter voter : voterList) {
                  if(voter.getUsername().equals(username)){
                        return voter;
                  }
                  
            }
            return null;
      }


      @Override
      public void loginAdmin(AdminConsole admin) throws RemoteException{
            System.out.println("Admin Console logged in");
            admins.add(admin);
      }

      @Override
      public void loginMulticastServer(MulticastServer multicastServer) throws RemoteException{
            System.out.println("Multicast Server logged in");
            servers.add(multicastServer);
      }

      @Override
      public Election searchElection(String title)  throws RemoteException{
            for(Election election: elections){
                  if(election.getTitle().equals(title)){
                        return election;
                  }
            }
            return null;
      }

      @Override
      public Voter searchVoterCc(String cc_number)  throws RemoteException{
            for (Voter voter : voterList) {
                  if(voter.getCc_number().equals(cc_number)){
                        return voter;
                  }
                  
            }
            return null;
      }

      @Override
      public String login(String message)  throws RemoteException{
            String username= null, password=null;
            Voter voter = null;
            String[] tokens = message.split(";");
            for (String string : tokens) {
                  
                  String[] token = string.split("\\|");
                  if(token[0].equals("username")){
                        username = token[1];
                  }else if(token[0].equals("password")){
                        password = token[1];
                  }
                  
            }
            
            if(username != null && password != null){
                  voter = searchVoter(username);
            }else{
                  return "type|status;logged|on;msg|Fail-username or password wrong";
            }
            if(voter != null && voter.getPassword().equals(password) ){
                  return "type|status;logged|on;msg|Welcome to eVoting";
            }else{
                  return "type|status;logged|on;msg|Fail-username or password wrong";
            }
      }

      @Override
      public boolean addElection(Election election)  throws RemoteException{
            if(searchElection(election.getTitle())==null){
                  elections.add(election);
                  return true;
            }else{
                  return false;
            }
      }

      @Override
      public void addVoter(Voter voter)  throws RemoteException{
            voterList.add(voter);

      }

      @Override
      public boolean createVoter(String username, String role, String department, String contact, String address, String cc_number, Calendar cc_expiring, String password)  throws RemoteException{
            if(searchVoter(username) == null && searchVoterCc(cc_number)==null){
                  Voter voter = new Voter(username, role, department, contact, address, cc_number, cc_expiring, password);
                  addVoter(voter);
                  return true;
            }
            return false;
      }

      @Override
      public boolean voterVotes(String username,String title, String candidateName)  throws RemoteException{
            /**
             * It receives the voter username, the title of the election and the candidate that is going to vote for
             * returns if there was a problem in the voting
             */
            Voter voter = searchVoter(username);
            Election election = searchElection(title);
            if(voter != null || election != null){
                  return election.vote(voter, candidateName);
                  
            }
            return false;
      }

      @Override
      public boolean createElection(String title,Calendar beggDate,Calendar endDate,String department, List<Type> allowedVoters)  throws RemoteException{
            if(searchElection(title) == null){
                  Election election = new Election(title, beggDate, endDate, department, allowedVoters);
                  addElection(election);
                  return true;
            }
            return false;
      }

      public RMIServer() throws RemoteException{
            super();
            voterList = new ArrayList<>();

      }



      public static void main(String[] args) {
            /*
            Calendar cc_expiring = Calendar.getInstance();
            cc_expiring.set(Calendar.YEAR, 2020);
            cc_expiring.set(Calendar.MONTH, 2);
            cc_expiring.set(Calendar.DAY_OF_MONTH, 5);
            Voter voter = new Voter("goncalo", "role", "department", "913802608", "address", "123", cc_expiring, "coelho");

            try{
            RMIServer rmiServer = new RMIServer();
            rmiServer.addVoter(voter);
            System.out.println( rmiServer.login("username|goncalo;password|coelho"));
            System.out.println( rmiServer.searchVoter("1234"));
            }catch(Exception e){
                  System.out.println("HI");
            }
            */
            
            
            try{
                  RMIServer rmiServer = new RMIServer();
                  LocateRegistry.createRegistry(5001).rebind("RMIServer", rmiServer);
                  System.out.println("RMIServer is on");
            } catch (Exception e) {
                  //TODO: handle exception
                  System.out.println("Exception in RMIServer.java(main) " + e);
            }
      }

      


}
