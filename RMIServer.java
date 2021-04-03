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
import java.io.InputStream;
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
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;


/*Por os restantes objects que podem ser passados */ 
public class RMIServer extends UnicastRemoteObject implements RMIServer_I{
      /**
       * Const to use
       */
      private static final String FILE_EMPTY = "File Empty";
      /**
       * Const to use
       */
      private static final String AN_ERROR_OCCURRED = "An error occurred.";
      /**
       * Const to use
       */
      private static final String FILE_ALREADY_EXISTS = "File already exists.";
      /**
       * Const to use
       */
      private static final String FILE_CREATED = "File created: ";
      /**
       * Const to help on serialize
       */
      private static final long serialVersionUID = -7161055300561474003L;
      
      /**
       * List of voters
       */
      private List<Voter> voterList = new CopyOnWriteArrayList<>();
      /**
       * List of elections
       */
      private List<Election> elections = new CopyOnWriteArrayList<>();
      /**
       * List of adminsConsolo logged in
       */
      private List<AdminConsole_I> admins = new CopyOnWriteArrayList<>();
      /**
       * List of all the servers
       */
      private List<MulticastServer> servers = new CopyOnWriteArrayList<>();
      /**
       * List of all the servers logged in
       */
      private List<MulticastServer> onServers = new CopyOnWriteArrayList<>();
      /**
       * Connection port
       */
      private static int port = 6789;
      /**
       * File to write
       */
      private static String electionFile;
      /**
       * File to write
       */
      private static String voterFile;
      /**
       * File to write
       */
      private static String tableFile;
      /**
       * IP the rmi uses
       */
      private static String ipDoServer;
     


      @Override
      public synchronized Voter searchVoter(String username)throws RemoteException{
            
            for (Voter voter : voterList) {
                  if(voter.getUsername().equals(username)){
                        return voter;
                  }
                  
            }
            return null;
      }
      
      
      @Override
      public synchronized boolean updateServerData(String department, ServerData update) throws RemoteException{
            for (MulticastServer server : onServers) {
                  if(server.getQ().getDepartment().equals(department)){
                        server.setQ(update);
                        return true;
                  }
            }
            return false;
      }
      
      @Override
      public synchronized List<MulticastServer> getOnServers() throws RemoteException {
            return  onServers;
      }

      
      @Override
      public synchronized List<MulticastServer> getServers() throws RemoteException {
            return  servers;
      }

      
      @Override
      public synchronized void loginAdmin(AdminConsole_I admin) throws RemoteException{
            System.out.println("Admin Console logged in");
            admins.add(admin);
      }

      
      @Override
      public synchronized void logoutAdmin(AdminConsole_I admin) throws RemoteException{
            
            System.out.println("Admin Console logged out");
            admins.remove(admin);
      }

      
      @Override
      public synchronized MulticastServer loginMulticastServer(MulticastServer multicastServer) throws RemoteException{

            System.out.println("Multicast Server logged in");

            if(!onServers.contains(multicastServer)){
                  List<MulticastServer> servers2 = getOnServers();
                  if(!onServers.isEmpty()){
                  for (MulticastServer server: servers2) {
                              if (server.getQ().getDepartment().equals(multicastServer.getQ().getDepartment())){
                                    return server;
                              }
                        }
                  }

                  onServers.add(multicastServer);
                  String notif = "Mesa de Voto "+ multicastServer.getQ().getDepartment() + " ON"; 
                        for (AdminConsole_I admin : admins) {
                              try{
                                    admin.notify_state(notif);
                              }catch(Exception e){
                                    admins.remove(admin);
                              }
                        }
            }
            
            if(!servers.contains(multicastServer)){
                  MulticastServer serverAux=  searchTableDeptServer(multicastServer.getQ().getDepartment());
                  
                  if(serverAux == null){
                        
                        servers.add(multicastServer);
                        writeMulticastServerFile();
                        
                        return null;
                  }else{
                        return serverAux;
                  }
            }
            return multicastServer;




      }
      @Override
      public synchronized void logoutMulticastServer(MulticastServer multicastServer) throws RemoteException{
            for (MulticastServer server : onServers) {
                  if(server.getQ().getDepartment().equals(multicastServer.getQ().getDepartment())){
                        onServers.remove(server);
                        break;
                  }
                  
            }
            String notif = "Mesa de Voto "+ multicastServer.getQ().getDepartment() + " OFF"; 
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
                  for (MulticastServer table: election.getTables()) {
                        if (table.getQ().getDepartment().equals(department)  && election.getAllowedVoters().contains(role) && election.getState()==State.OPEN) {
                              res.add(election);
                        }
                  }
            }
            return res;
      }

      
      @Override
      public synchronized boolean addTableElection(MulticastServer table, Election election)throws RemoteException{
            boolean flag = false;
            for(MulticastServer m: servers){
                  if(m.getQ().getDepartment().equals(table.getQ().getDepartment())){
                        for(Election e: elections){
                              if(e.getTitle().equals(election.getTitle())){
                                    int index1 = elections.indexOf(e);
                                    if(elections.get(index1).getDepartment().equals(table.getQ().getDepartment())|| elections.get(index1).getDepartment().isEmpty()){
                                          
                                          for (MulticastServer server : elections.get(index1).getTables()) {
                                                if(!server.getQ().getDepartment().equals(table.getQ().getDepartment())){
                                                    flag = true;  
                                                }
                                                
                                          }
                                          if(flag || elections.get(index1).getTables().isEmpty()){
                                                elections.get(index1).addTable(table);
                                                writeElectionFile();
                                                System.out.println("Added table with sucess");
                                                return true;
                                          }
                                    }
                              }
                        }
                  }
            }
            return false;
      }

      
      @Override
      public synchronized boolean removeTableElection(MulticastServer table, Election election)throws RemoteException{
            for(MulticastServer m: servers){
                  if(m.getQ().getDepartment().equals(table.getQ().getDepartment())){
                        for(Election e: elections){
                              if(e.getTitle().equals(election.getTitle())){
                                    int index1 = elections.indexOf(e);
                                    if(elections.get(index1).getDepartment().equals(table.getQ().getDepartment())|| elections.get(index1).getDepartment().isEmpty()){
                                          elections.get(index1).removeTable(table);
                                          writeElectionFile();
                                          System.out.println("Removed table with sucess");
                                          return true;
                                    }
                              }
                        }
                  }
            }
            return false;
      }
      
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

      public synchronized MulticastServer searchTableDeptServer(String department) throws RemoteException{
            
            List<MulticastServer> servers2 = getServers();
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
            boolean flag = true;
            MulticastServer server = searchTableDept(table.getQ().getDepartment());
            if(server!=null){
                  for (Voter voter : server.getTableMembers()) {
                        if(voter.getCc_number().equals(member.getCc_number())){
                              flag = false;
                        }
                  }
                  if(flag){
                        server.addTableMembers(member);
                        System.out.println("Added member to table with sucess");
                        writeMulticastServerFile();
                        return true;
                  }else{
                        return false;
                  }

            }/*
            for(MulticastServer m: servers){
                  if(m.getQ().getDepartment().equals(table.getQ().getDepartment())){
                        int index = servers.indexOf(m);
                        try{
                              
                              
                        }catch(Exception e){
                              servers.remove(index);
                        }
                        
                  }
            }  */
            return false;
      }
      
      @Override
      public synchronized boolean removeVoterTable(MulticastServer table, Voter member) throws RemoteException{
            for(MulticastServer m: servers){
                  if(m.getQ().getDepartment().equals(table.getQ().getDepartment())){
                        int index = servers.indexOf(m);
                        try{
                              servers.get(index).removeTableMembers(member);
                              System.out.println("Removed member to table with sucess");
                        }catch(Exception e){
                              servers.remove(index);
                        }
                        writeElectionFile();
                        return true;
                  }
            }  
            return false;
      }

      
      @Override
      public synchronized Voter searchVoterCc(String cc_number)  throws RemoteException{
            
            for (Voter voter : voterList) {
                  if(voter.getCc_number().equals(cc_number)){
                        return voter;
                  }
                  
            }
            return null;
      }

      
      @Override
      public synchronized Voter searchUser(String username, String password) throws RemoteException{
            for (Voter voter : voterList) {
                  if(voter.getUsername().equals(username) && voter.getPassword().equals(password)){
                        return voter;
                  }
                  
            }
            return null;
      }

      
      @Override
      public synchronized boolean addCandidate(String title,Candidates candidate) throws RemoteException{
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

      /**
       * Writes an object file of the election 
       */
      public synchronized void writeElectionFile(){
            
            try(FileOutputStream fos = new FileOutputStream(electionFile); ObjectOutputStream oos = new ObjectOutputStream(fos)){
                  oos.writeObject(elections);
                  
            }catch(Exception ex){
                  ex.printStackTrace();
            }

      }

      /**
       * Writes an object file about the elections
       */
      public synchronized void writeVoterFile(){
            
            try(FileOutputStream fos = new FileOutputStream(voterFile); ObjectOutputStream oos = new ObjectOutputStream(fos)){
                  oos.writeObject(voterList);
            }catch(Exception ex){
                  ex.printStackTrace();
            }

      }
      /**
       * Writes an object file about the tables
       */
      public synchronized void writeMulticastServerFile(){
            try(FileOutputStream fos = new FileOutputStream(tableFile); ObjectOutputStream oos = new ObjectOutputStream(fos)){
                  oos.writeObject(servers);
            }catch(Exception ex){
                  ex.printStackTrace();
            }

      }

      /**
       * Reads an object file. If it doesnt exist it will create one
      */
      public void readMulticastFile(){
            
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

      /**
       * Reads an object file. If it doesnt exist it will create one
      */
      public void readElectionFile(){
            
            try(FileInputStream fis = new FileInputStream(electionFile); ObjectInputStream ois = new ObjectInputStream(fis)){
                  elections = (CopyOnWriteArrayList<Election>) ois.readObject();
                  for (Election election : elections) {
                        election.runThread();
                  }
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
      /**
       * Reads an object file. If it doesnt exist it will create one
      */
      public void readVoterFile(){
           
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
      public synchronized List<Voter> getRMITableMembers(String department) throws RemoteException{
            MulticastServer table = searchTableDept(department);
            if(table != null){
                  return table.getTableMembers();
            }else{
                  return new CopyOnWriteArrayList<>();
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
            
            for(Election e: elections){
                  if(e.getTitle().equals(election.getTitle())){
                        int index = elections.indexOf(e);
                        for(Voter v: voterList){
                              if(v.getUsername().equals(member.getUsername())){      
                                    boolean flag = elections.get(index).addMemberToLista(nome, v);
                                    writeElectionFile();
      
                                    System.out.println("Added member successfully");
                                    return flag;
                              }
                        }
                  }
            }
            return false;
      }
      
      @Override
      public synchronized boolean removeMembroToLista(Election election, String nome,Voter member) throws RemoteException{
            for(Election e: elections){
                  if(e.getTitle().equals(election.getTitle())){
                        int index = elections.indexOf(e);
                        for(Voter v: voterList){
                              if(v.getUsername().equals(member.getUsername())){     
                                    boolean flag = elections.get(index).removeMemberToLista(nome, v);
                                    writeElectionFile();
      
                                    System.out.println("Removed member successfully");
                                    return flag;
                              }
                        }
                  }
            }
            return false;
      }
      
      @Override
      public synchronized boolean createVoter(String username, String department, String contact, String address, String cc_number, Calendar cc_expiring, String password,Type type)  throws RemoteException{
            
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
            
            Voter voter = searchVoter(username);
            boolean flag = false;

            for(Election election: elections){
                  if(election.getTitle().equals(title)){
                        
                        if(voter != null && election.getState() == State.OPEN){
                              flag = election.vote(voter, candidateName, voteLocal);   
                              writeElectionFile();
                              System.out.println("Voter voted sucessfully");
                              return flag;
                        }
                  }
            }

            return false;
      }

      
      @Override
      public synchronized boolean voterVotesAdmin(String username,String title, String candidateName, String voteLocal)  throws RemoteException{
            
            Voter voter = searchVoter(username);
            boolean flag;

            for(Election election: elections){
                  if(election.getTitle().equals(title)){
                        
                        if(voter != null && election.getState() == State.WAITING){
                              flag = election.vote(voter, candidateName, voteLocal);
                              if(flag){
                                    writeElectionFile();
                                    System.out.println("Voter voted sucessfully");
                              }
                              else{
                                    System.out.println("Error:impossible to vote");
                              }
                              return flag;
                        }
                  }
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

      
      @Override
      public synchronized boolean createCandidate(String name,String title,Type type) throws RemoteException{
            Candidates candidates = new Candidates(name,type);
            return addCandidate(title, candidates);
      }


      
      @Override
      public synchronized boolean switchElection(String name, Election newInfo) throws RemoteException{
            
            for(Election e: elections){
                  if(e.getTitle().equals(name)){
                        int i = elections.indexOf(e);
                        if(e.getState() == State.WAITING){
                              elections.set(i, newInfo);
                              writeElectionFile();
                              System.out.println("Switched info successfully");
                              return true;
                        }else{
                              return false;
                        }
                  }
            }
            return false;
      }

      
      @Override
      public synchronized boolean switchUser(String cc_number, Voter newInfo) throws RemoteException{
            for(Voter v: voterList){
                  if(v.getCc_number().equals(cc_number)){
                        int i = voterList.indexOf(v);
                        voterList.set(i, newInfo);
                        writeElectionFile();
                        System.out.println("Switched voter's info successfully");
                        return true;
                  }
            }
            return false;
      }

      public RMIServer() throws RemoteException{
            super();

      }

      /**
       * Read the config file if it doesnt exist it will go to the default values
       * 
       */
      public static void readConfig(){
            try{
                  Properties prop = new Properties();
                  String fileName = "config.properties";
                  
                  prop.load(new FileInputStream(fileName));
                  port = Integer.parseInt(prop.getProperty("port"));
                  voterFile = prop.getProperty("voterFile");
                  electionFile = prop.getProperty("electionFile");
                  tableFile = prop.getProperty("tableFile");
                  ipDoServer = prop.getProperty("ip");
            }catch(Exception e){
                  System.out.println("Problem");
                  port = 5001;
                  voterFile = "voterInformation";
                  electionFile = "electionInformation";
                  tableFile = "multicastInformation";

            }
            /*
            try {
                  File myObj = new File("config.txt");
                  Scanner myRScanner = new Scanner(myObj);
                  port = Integer.parseInt(myRScanner.nextLine());
                  voterFile = myRScanner.nextLine();
                  electionFile = myRScanner.nextLine();
                  tableFile = myRScanner.nextLine();
                  ipDoServer = myRScanner.nextLine();
                  myRScanner.close();
            } catch (Exception e) {
                  port = 5001;
                  voterFile = "voterInformation";
                  electionFile = "electionInformation";
                  tableFile = "multicastInformation";

            }*/
      }



      public static void main(String[] args) {
            RMIServer rmiServer = null;
            boolean flag = true;
            DatagramSocket aSocket = null;
            readConfig();
            try{
                  System.getProperties().put("java.security.policy", "policy.all");
		      System.setSecurityManager(new RMISecurityManager());
                  System.setProperty("java.rmi.server.hostname", ipDoServer);
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
