// É o servidor central (replicado) que armazena todos os dados da aplicação, suportando por essa razão todas as operações necessárias através demétodos remotos usando Java RMI
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;


/*Por os restantes objects que podem ser passados */ 
public class RMIServer extends UnicastRemoteObject implements RMIServer_I{
      /**
       *
       */
      private static final String FILE_EMPTY = "File Empty";
      private static final String AN_ERROR_OCCURRED = "An error occurred.";
      private static final String FILE_ALREADY_EXISTS = "File already exists.";
      private static final String FILE_CREATED = "File created: ";

      private static final long serialVersionUID = -7161055300561474003L;
      
      private List<Voter> voterList = new CopyOnWriteArrayList<>();
      private List<Election> elections = new CopyOnWriteArrayList<>();
      private List<AdminConsole_I> admins = new CopyOnWriteArrayList<>();
      private List<MulticastServer> servers = new CopyOnWriteArrayList<>();
      private List<MulticastServer> onServers = new CopyOnWriteArrayList<>();
      private static int port = 6789;
      private static String electionFile;
      private static String voterFile;
      private static String tableFile;
      @Override
      public synchronized Voter searchVoter(String username)throws RemoteException{
            /**
             * Search voter by only it's username
             * return false if there was any problem
             */
            for (Voter voter : voterList) {
                  if(voter.getUsername().equals(username)){
                        return voter;
                  }
                  
            }
            return null;
      }

      @Override
      public synchronized List<MulticastServer> getOnServers() throws RemoteException {
            return onServers;
      }
      @Override
      public synchronized List<MulticastServer> getServers() throws RemoteException {
            return servers;
      }

      /**
       * @param AdminConsole_I that is going to be added
       * Login the Admin Console
       * 
       */
      @Override
      public synchronized void loginAdmin(AdminConsole_I admin) throws RemoteException{
            System.out.println("Admin Console logged in");
            admins.add(admin);
      }

      /**
       * @param AdminConsole_I that is going to be removed
       * Logout the Admin Console
       * 
       */
      @Override
      public synchronized void logoutAdmin(AdminConsole_I admin) throws RemoteException{
            
            System.out.println("Admin Console logged on");
            admins.remove(admin);
      }

      /**
      * @param MulticastServer u want to add 
      * if it already exists one in the table array it will skip to add to the full list
      * Login the MulticastServer(Table)
      * Will notify all admin consoles
      * returns null if it was added sucessfully or the "supposed value" it should have
      */
      @Override
      public synchronized MulticastServer loginMulticastServer(MulticastServer multicastServer) throws RemoteException{
            
            System.out.println("Multicast Server logged in");
            if(!servers.contains(multicastServer)){
                  multicastServer.setTableID(UUID.randomUUID().toString());
                  servers.add(multicastServer);
                  writeMulticastServerFile();
            }
            if(!onServers.contains(multicastServer)){
                  List<MulticastServer> servers2 = getOnServers();
                  if(!onServers.isEmpty()){
                  for (MulticastServer server: servers2) {
                              if (server.getQ().getDepartment().equals(multicastServer.getQ().getDepartment())){
                                    return server;
                              }
                        }
                  }
                  String notif = "Mesa de Voto "+ multicastServer.getTableID() + " ON"; 
                  for (AdminConsole_I admin : admins) {
                        try{
                        admin.notify_state(notif);
                        }catch(Exception e){
                              admins.remove(admin);
                        }
                  }
                  onServers.add(multicastServer);
                  return null;
            }else{
                  return multicastServer;
            }
            
      }
      /**
      * @param MulticastServer it should be removed
      * Logout the MulticastServer(Table)
      * Will notify all adminConsoles it left
      * @return void
      */
      @Override
      public synchronized void logoutMulticastServer(MulticastServer multicastServer) throws RemoteException{
            onServers.remove(multicastServer);
            String notif = "Mesa de Voto "+ multicastServer.getTableID() + " OFF"; 
            for (AdminConsole_I admin : admins) {
                  try{
                        admin.notify_state(notif);
                  }catch(Exception e){
                        admins.remove(admin);
                  }
            }
      }
      @Override
      public synchronized List<Election> stateElections(State state, Type type) throws RemoteException{
            /**
             * Returns a List of all the election that has that state and the same type
             * if the type is null, will not consider the type
             */
            List<Election> res = new CopyOnWriteArrayList<>();
            for (Election election : elections){
                  if(election.getState().equals(state)){
                        if(type == null || election.getAllowedVoters().contains(type)){
                              res.add(election);
                        }
                  }
            }
            return res;
      }

      

      @Override
      public synchronized List<Election> tablesElections(MulticastServer table) throws RemoteException{
            /**
             * Returns the list of possible Elections that the Table can vote
             */
            
            List<Election> res = new CopyOnWriteArrayList<>();
            for (Election election : elections){
                  if(election.getTables().contains(table)){
                        res.add(election);
                  }
            }
            return res;
      }

      @Override
      public synchronized Election searchElection(String title)  throws RemoteException{
            /**
             * Searches the Election by the title it was given
             * returns null if it hasnt been found
             */
            for(Election election: elections){
                  if(election.getTitle().equals(title)){
                        return election;
                  }
            }
            return null;
      }
      @Override
      public synchronized List<Election> searchElectionbyDepRole(String department, Type role) throws RemoteException{
            /**
             * Searches the Election by the Department and Role it was given
             * returns null if it hasn't been found
             */
            List<Election> res = new CopyOnWriteArrayList<>();
            for(Election election: elections){
                  if(election.getDepartment().equals(department) && election.getAllowedVoters().contains(role)){
                        res.add(election);
                  }
            }
            return res;
      }
      @Override
      public synchronized boolean addTableElection(MulticastServer table, Election election)throws RemoteException{
            /**
             * adds the table ability vote in that election
             * returns false if a mistake has happened
             */
            int index1 = elections.indexOf(election);
            if(index1 != -1 && servers.contains(table)){
                  elections.get(index1).addTable(table);
                  writeElectionFile();
                  return true;
            }
            return false;
      }
      @Override
      public synchronized boolean removeTableElection(MulticastServer table, Election election)throws RemoteException{
            /**
             * Removes the table ability vote in that election
             * returns false if a mistake has happened
             */
            int index1 = elections.indexOf(election);
            if(index1 != -1 && servers.contains(table)){
                  boolean flag = elections.get(index1).removeTable(table);
                  writeElectionFile();
                  return flag;
            }
            return false;
      }

      @Override
      public synchronized MulticastServer searchTable(String id) throws RemoteException{
            /**
             * It searches the table by its unique id
             * returns null if nothing has been found
             */
            List<MulticastServer> servers2 = getServers();
            for(MulticastServer server: servers2){
                  try{
                        if(server.getTableID().equals(id)){
                              return server;
                              
                        }
                  }catch(Exception e){
                        onServers.remove(server);
                  }
            }
            return null;
      }
      /**
       * @param String the name of the department
      * It searches the table by its department that are on the OnServers
      * @return null if nothing has been found
      */
      @Override
      public synchronized MulticastServer searchTableDept(String department) throws RemoteException{
            
            List<MulticastServer> servers2 = getOnServers();
            Iterator<MulticastServer> it = servers2.iterator();
            while (it.hasNext()) {
                  MulticastServer server = it.next();
                  try {
                        if(server.getQ().getDepartment().equals(department)){
                              return server;
                        }
                  } catch (Exception e) {
                        onServers.remove(server);
                  }
            }
            /*for(MulticastServer server: servers){
                  if(server.q.getDepartment().equals(department)){
                        return server;
                  }
            }*/
            return null;
      }
      @Override
      public synchronized boolean addVoterTable(MulticastServer table, Voter member)  throws RemoteException{
            /**
             * Does a callback with the table to add a member to it
             * returns false if no such table exist in the array
             */
            int index = servers.indexOf(table);
            if(index != -1){
                  servers.get(index).addTableMembers(member);
                  writeElectionFile();
                  return true;
            }
            return false;
      }
      @Override
      public synchronized boolean removeVoterTable(MulticastServer table, Voter member) throws RemoteException{
            /**
             * Does a callback with the table to remove a member to it
             * returns false if no such table exist in the array
             */
            int index = servers.indexOf(table);
            if(index != -1){
                  servers.get(index).removeTableMembers(member);
                  writeElectionFile();
                  return true;
            }
            return false;
      }

      @Override
      public synchronized Voter searchVoterCc(String cc_number)  throws RemoteException{
            /**
             * Searches an user based by its cc_number which it should be unique
             * returns null if it hasnt been found one with that value
             */
            for (Voter voter : voterList) {
                  if(voter.getCc_number().equals(cc_number)){
                        return voter;
                  }
                  
            }
            return null;
      }

      @Override
      public synchronized Voter searchUser(String username, String password) throws RemoteException{
            /**
             * It will search a user based by its username and password and returns the first one
             * returns null if it hasn't been found
             */


            for (Voter voter : voterList) {
                  if(voter.getUsername().equals(username) && voter.getPassword().equals(password)){
                        return voter;
                  }
                  
            }
            return null;
      }
      @Override
      public synchronized boolean addCandidate(String title,Candidates candidate) throws RemoteException{
            /**
             * Adds a Candidate (List) from an election
             * returns false if it can't find an election or a List
             * 
             */
            List<Election> elections2 = getElections();
            
            for (Election election : elections2) {
                  if(election.getTitle().equals(title)){
                        boolean flag = election.addCandidateList(candidate);
                        writeElectionFile();
                        System.out.println("Candidate created sucessfully");
                        return flag;
                  }
            }
            
            return false;
      }

      @Override
      public synchronized boolean removeCandidate(String title, String candidateName) throws RemoteException{
            /**
             * Removes a Candidate (List) from an election
             * returns false if it can't find an election or a List
             * 
             */
            ListIterator<Election> iterator = elections.listIterator();
            while(iterator.hasNext()){
                  if(iterator.next().getTitle().equals(title)){
                        boolean flag = iterator.next().removeCandidateList(candidateName);
                        writeElectionFile();
                        return flag;
                  }
            }
            return false;
      }

      public void writeElectionFile(){
            /**
             * Writes an object file about the elections
             */
            try(FileOutputStream fos = new FileOutputStream(electionFile); ObjectOutputStream oos = new ObjectOutputStream(fos)){
                  oos.writeObject(elections);
                  
            }catch(Exception ex){
                  ex.printStackTrace();
            }

      }

      public void writeVoterFile(){
            /**
             * Writes an object file about the elections
             */
            try(FileOutputStream fos = new FileOutputStream(voterFile); ObjectOutputStream oos = new ObjectOutputStream(fos)){
                  oos.writeObject(voterList);
            }catch(Exception ex){
                  ex.printStackTrace();
            }

      }

      public void writeMulticastServerFile(){
            /**
             * Writes an object file about the tables
             */
            try(FileOutputStream fos = new FileOutputStream(tableFile); ObjectOutputStream oos = new ObjectOutputStream(fos)){
                  oos.writeObject(voterList);
            }catch(Exception ex){
                  ex.printStackTrace();
            }

      }
      public void readMulticastFile(){
            /**
             * Reads an object file. If it doesnt exist it will create one
             */
            try(FileInputStream fis = new FileInputStream(tableFile); ObjectInputStream ois = new ObjectInputStream(fis)){
                  servers = (CopyOnWriteArrayList<MulticastServer>) ois.readObject();
                  
            }catch(FileNotFoundException e){
                  try {
                        File myObj = new File(tableFile);
                        if (myObj.createNewFile()) {
                          System.out.println(FILE_CREATED + myObj.getName());
                        } else {
                          System.out.println(FILE_ALREADY_EXISTS);
                        }
                      } catch (IOException ex) {
                        System.out.println(AN_ERROR_OCCURRED);
                        e.printStackTrace();
                      }
            }catch(EOFException ex){
                  System.out.println(FILE_EMPTY);
            }catch(Exception ex){
                  ex.printStackTrace();
            }

      }

      public void readElectionFile(){
            /**
             * Reads an object file. If it doesnt exist it will create one
             */
            try(FileInputStream fis = new FileInputStream(electionFile); ObjectInputStream ois = new ObjectInputStream(fis)){
                  elections = (CopyOnWriteArrayList<Election>) ois.readObject();
                  
            }catch(FileNotFoundException e){
                  try {
                        File myObj = new File(electionFile);
                        if (myObj.createNewFile()) {
                          System.out.println(FILE_CREATED + myObj.getName());
                        } else {
                          System.out.println(FILE_ALREADY_EXISTS);
                        }
                      } catch (IOException ex) {
                        System.out.println(AN_ERROR_OCCURRED);
                        e.printStackTrace();
                      }
            }catch(EOFException ex){
                  System.out.println(FILE_EMPTY);
            }catch(Exception ex){
                  ex.printStackTrace();
            }

      }

      public void readVoterFile(){
            /**
             * Reads an object file. If it doesnt exist it will create one
             */
            try(FileInputStream fis = new FileInputStream(voterFile); ObjectInputStream ois = new ObjectInputStream(fis)){
                   
                  voterList = (CopyOnWriteArrayList<Voter>) ois.readObject();
            }catch(FileNotFoundException e){
                  try {
                        File myObj = new File(voterFile);
                        if (myObj.createNewFile()) {
                          System.out.println(FILE_CREATED + myObj.getName());
                        } else {
                          System.out.println(FILE_ALREADY_EXISTS);
                        }
                      } catch (IOException ex) {
                        System.out.println(AN_ERROR_OCCURRED);
                        e.printStackTrace();
                      }
            }catch(EOFException ex){
                  System.out.println(FILE_EMPTY);
            }catch(Exception ex){
                  ex.printStackTrace();
            }

      }
      @Override
      public synchronized List<Voter> getVoterList() throws RemoteException {
            return this.voterList;
      }
      @Override
      public synchronized void setVoterList(List<Voter> voterList) throws RemoteException{
            this.voterList = voterList;
      }
      @Override
      public synchronized List<Election> getElections() throws RemoteException {
            return this.elections;
      }
      @Override
      public synchronized void setElections(List<Election> elections) throws RemoteException{
            this.elections = elections;
      }
      /*
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
      }*/

      @Override
      public  synchronized boolean addElection(Election election)  throws RemoteException{
            if(searchElection(election.getTitle())==null){
                  elections.add(election);
                  
                  return true;
            }else{
                  return false;
            }
      }

      @Override
      public synchronized void addVoter(Voter voter)  throws RemoteException{                  
            voterList.add(voter);
      }
      @Override
      public synchronized boolean addMembroToLista(Election election, String nome,Voter member) throws RemoteException{
            /**
             * Adds a member from an election list
             * return false if there was an error finding the member or the list
             */
            int index = elections.indexOf(election);
            if(index != -1){
                  boolean flag = elections.get(index).addMemberToLista(nome, member);
                  writeElectionFile();

                  
                  System.out.println("Added member successfully");
                  return flag;
            }

            return false;
      }
      @Override
      public synchronized boolean removeMembroToLista(Election election, String nome,Voter member) throws RemoteException{
            /**
             * Removes a member from an election list
             * return false if there was an error finding the member or the list
             */
            int index = elections.indexOf(election);
            if(index != -1){
                  
                  boolean flag = elections.get(index).removeMemberToLista(nome, member);
                  writeElectionFile();
                  
                  System.out.println("Removed member successfully");
                  return flag;
            }

            return false;
      }

      @Override
      public synchronized boolean createVoter(String username, String department, String contact, String address, String cc_number, Calendar cc_expiring, String password,Type type)  throws RemoteException{
            /**
             * It will create a voter
             */
            if(searchVoter(username) == null && searchVoterCc(cc_number)==null){
                  Voter voter = new Voter(username, department, contact, address, cc_number, cc_expiring, password,type);
                  addVoter(voter);
                  writeVoterFile();
                  System.out.println("Voter created sucessfully");
                  return true;
            }
            return false;
      }

      @Override
      public synchronized boolean voterVotes(String username,String title, String candidateName, String voteLocal)  throws RemoteException{
            /**
             * It receives the voter username, the title of the election and the candidate that is going to vote for
             * returns if there was a problem in the voting
             * Server e admins
             */
            Voter voter = searchVoter(username);
            Election election = searchElection(title);
            
            if(voter != null && election != null && election.getState() == State.OPEN){
                  boolean flag =election.vote(voter, candidateName, voteLocal);
                  writeElectionFile();
                  System.out.println("Voter voted sucessfully");
                  return flag;
                  
            }
            return false;
      }

      @Override
      public synchronized boolean voterVotesAdmin(String username,String title, String candidateName, String voteLocal)  throws RemoteException{
            /**
             * It receives the voter username, the title of the election and the candidate that is going to vote for
             * returns if there was a problem in the voting
             * Server e admins
             */
            Voter voter = searchVoter(username);
            Election election = searchElection(title);
            
            if(voter != null && election != null && election.getState() != State.CLOSED){
                  boolean flag =election.vote(voter, candidateName, voteLocal);
                  writeElectionFile();
                  System.out.println("Voter voted sucessfully");
                  return flag;
                  
            }
            return false;
      }

      @Override
      public synchronized boolean createElection(String title, String description, Calendar beggDate,Calendar endDate,String department, List<Type> allowedVoters)  throws RemoteException{
            /**
             * Consola admin
             */
            if(searchElection(title) == null){
                  Election election = new Election(title, description, beggDate, endDate, department, allowedVoters);
                  addElection(election);
                  writeElectionFile();
                  
                  System.out.println("Created election successfully");
                  return true;
            }
            return false;
      }

      /**
       * @params all the infor 
       * 
       */
      @Override
      public synchronized boolean createCandidate(List<Voter> members, String name,String title,Type type) throws RemoteException{
            Candidates candidates = new Candidates(members, name,type);
            return addCandidate(title, candidates);
      }


      /**
       * @param1 original info Election
       * @param2 new info Election
       * 
       * switches to the new election info
       * 
       * @return boolean if it was possible to switch
       */
      @Override
      public synchronized boolean switchElection(Election oriElection, Election newInfo) throws RemoteException{
            if(elections.contains(oriElection)){
                  int i = elections.indexOf(oriElection);
                  if(oriElection.getState() == State.WAITING){
                        elections.set(i, newInfo);
                        writeElectionFile();
                        System.out.println("Switched info successfully");
                  }else{
                        return false;
                  }
            }else{
                  return false;
            }
            return true;
      }

      /**
       * @param1 original info Voter
       * @param2 new info Voter
       * 
       * Switches to the new User info
       * 
       * @return boolean if it was possible to switch
       */
      @Override
      public synchronized boolean switchUser(Voter oriVoter, Voter newInfo) throws RemoteException{
            if(voterList.contains(oriVoter)){
                  int i = voterList.indexOf(oriVoter);
                  voterList.set(i, newInfo);
                  writeElectionFile();
                  System.out.println("Switched info successfully");
            }else{
                  return false;
            }
            return true;
      }

      public RMIServer() throws RemoteException{
            super();

      }

      /**
       * Read the config file if it doesnt exist it will go to the default values
       * 
       */
      public static void readConfig(){
            
            try {
                  File myObj = new File("config.txt");
                  Scanner myRScanner = new Scanner(myObj);
                  port = Integer.parseInt(myRScanner.nextLine());
                  voterFile = myRScanner.nextLine();
                  electionFile = myRScanner.nextLine();
                  tableFile = myRScanner.nextLine();
                  myRScanner.close();
            } catch (Exception e) {
                  port = 5001;
                  voterFile = "voterInformation";
                  electionFile = "electionInformation";
                  tableFile = "multicastInformation";

            }
      }



      public static void main(String[] args) {
            RMIServer rmiServer = null;
            boolean flag = true;
            DatagramSocket aSocket = null;
            readConfig();
            try{

                   
                  rmiServer = new RMIServer();
                  LocateRegistry.createRegistry(port).rebind("RMIServer", rmiServer);
                  System.out.println("RMIServer is on");
                  rmiServer.readMulticastFile();
                  rmiServer.readElectionFile();
                  rmiServer.readVoterFile();
                  aSocket = new DatagramSocket(port+1);
                  while (flag) {
                        byte[] buffer = new byte[1];
                        DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                        aSocket.receive(request);
                        byte[] datas = request.getData();
                        
                        ByteArrayInputStream bis=new ByteArrayInputStream(datas);
                        DataInputStream dis=new DataInputStream(new BufferedInputStream(bis));
                        flag = dis.readBoolean();
                        System.out.println("Recebeu ping");
                        DatagramPacket reply = new DatagramPacket(request.getData(), request.getLength(),request.getAddress(),request.getPort());
                        aSocket.send(reply);
                        dis.close();
                        bis.close();
                  }
                  aSocket.close();
                  
            }catch(ExportException ex){
                  try{
                        aSocket = new DatagramSocket(port+2);
                        aSocket.setSoTimeout(5000);
                        while (flag) {
                              byte[]  m = new byte[]{(byte)(flag?1:0)};
                              InetAddress aHost = InetAddress.getByName("localhost");
                              DatagramPacket request = new DatagramPacket(m, 1, aHost, port+1);
                              aSocket.send(request);
                              byte[] r = new byte[1];
				      DatagramPacket reply = new DatagramPacket(r, r.length);
                              aSocket.receive(reply);
                              byte[] datas = request.getData();
                              
                              ByteArrayInputStream bis=new ByteArrayInputStream(datas);
                              DataInputStream dis=new DataInputStream(new BufferedInputStream(bis));
                              flag = dis.readBoolean();
                              dis.close();
                              bis.close();
                              Thread.sleep(5000);
                        }
                        aSocket.close();
                  }catch (SocketTimeoutException e) {
                        // timeout exception.
                        if(aSocket != null){
                              aSocket.close();
                        }
                        System.out.println("Primary Dead !!!! " + e);
                        main(args);
                  }catch (SocketException e){
                        System.out.println("Socket: " + e.getMessage());
                  }catch (IOException e) {
                        System.out.println("IO: " + e.getMessage());
                  } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        Thread.currentThread().interrupt();
                  }finally{
                        if(aSocket != null){
                              aSocket.close();
                        }
                  }
                 

            }catch (SocketException e){
                  System.out.println("Socket: " + e.getMessage());
            }catch (IOException e) {
                  System.out.println("IO: " + e.getMessage());
            }catch (Exception e) {
                  if(aSocket != null){
                        aSocket.close();
                  }
                  if(rmiServer != null){
                        rmiServer.writeElectionFile();
                        rmiServer.writeVoterFile();
                  }
            }finally{
                  if(aSocket != null){
                        aSocket.close();
                  }
            }
      }

}
