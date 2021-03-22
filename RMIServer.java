// É o servidor central (replicado) que armazena todos os dados da aplicação, suportando por essa razão todas as operações necessárias através demétodos remotos usando Java RMI
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
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
import java.util.concurrent.CopyOnWriteArrayList;

/*Por os restantes objects que podem ser passados */ 
public class RMIServer extends UnicastRemoteObject implements RMIServer_I{
      /**
       *adicionar remover eleições as mesas de voto 
       *adicionar membros
       */
      private static final long serialVersionUID = -7161055300561474003L;
      
      private List<Voter> voterList = new CopyOnWriteArrayList<>();
      private List<Election> elections = new CopyOnWriteArrayList<>();
      private List<AdminConsole> admins = new CopyOnWriteArrayList<>();
      private static List<MulticastServer> servers = new CopyOnWriteArrayList<>();
      private static int port = 6789;
      private Integer id = 0;
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
            multicastServer.setTableID(Integer.toString(id));
            id++;
            servers.add(multicastServer);
      }

      public void logoutMulticastServer(MulticastServer multicastServer) throws RemoteException{
            servers.remove(multicastServer);
            for (Election election : elections) {
                  if(election.getTables().contains(multicastServer)){
                        election.getTables().remove(multicastServer);
                  }
            }
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
      public boolean addTableElection(MulticastServer table, Election election)throws RemoteException{
            int index1 = elections.indexOf(election);
            if(index1 != -1 && servers.contains(table)){
                  elections.get(index1).addTable(table);
                  return true;
            }
            return false;
      }
      @Override
      public boolean removeTableElection(MulticastServer table, Election election)throws RemoteException{
            int index1 = elections.indexOf(election);
            if(index1 != -1 && servers.contains(table)){
                  elections.get(index1).removeTable(table);
                  return true;
            }
            return false;
      }
      @Override
      public MulticastServer searchTable(String id) throws RemoteException{
            for(MulticastServer server: servers){
                  if(server.getTableID().equals(id)){
                        return server;
                        
                  }
            }
            return null;
      }
      @Override
      public boolean addVoterTable(MulticastServer table, Voter member)  throws RemoteException{
            int index = servers.indexOf(table);
            if(index != -1){
                  servers.get(index).addTableMembers(member);
                  return true;
            }
            return false;
      }
      @Override
      public boolean removeVoterTable(MulticastServer table, Voter member) throws RemoteException{
            int index = servers.indexOf(table);
            if(index != -1){
                  servers.get(index).removeTableMembers(member);
                  return true;
            }
            return false;
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
      public Voter searchUser(String username, String password) throws RemoteException{


            for (Voter voter : voterList) {
                  if(voter.getUsername().equals(username) && voter.getPassword().equals(password)){
                        return voter;
                  }
                  
            }
            return null;
      }

      public boolean addCandidate(String title,Candidates candidate) throws RemoteException{
            ListIterator<Election> iterator = elections.listIterator();
            while(iterator.hasNext()){
                  if(iterator.next().getTitle().equals(title)){
                        boolean flag = iterator.next().addCandidateList(candidate);
                        writeElectionFile();
                        return flag;
                  }
            }
            
            return false;
      }


      public boolean removeCandidate(String title, String candidateName) throws RemoteException{
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
            try(FileOutputStream fos = new FileOutputStream("electionInformation"); ObjectOutputStream oos = new ObjectOutputStream(fos)){
                  oos.writeObject(elections);
                  
            }catch(Exception ex){
                  ex.printStackTrace();
            }

      }

      public void writeVoterFile(){
            try(FileOutputStream fos = new FileOutputStream("voterInformation"); ObjectOutputStream oos = new ObjectOutputStream(fos)){
                  oos.writeObject(voterList);
            }catch(Exception ex){
                  ex.printStackTrace();
            }

      }

      public void readElectionFile(){
            try(FileInputStream fis = new FileInputStream("electionInformation"); ObjectInputStream ois = new ObjectInputStream(fis)){
                  elections = (CopyOnWriteArrayList<Election>) ois.readObject();
                  for (Election election : elections) {
                        election.setTables(new CopyOnWriteArrayList<>());
                  }
            }catch(FileNotFoundException e){
                  try {
                        File myObj = new File("electionInformation");
                        if (myObj.createNewFile()) {
                          System.out.println("File created: " + myObj.getName());
                        } else {
                          System.out.println("File already exists.");
                        }
                      } catch (IOException ex) {
                        System.out.println("An error occurred.");
                        e.printStackTrace();
                      }
            }catch(Exception ex){
                  ex.printStackTrace();
            }

      }

      public void readVoterFile(){
            try(FileInputStream fis = new FileInputStream("voterInformation"); ObjectInputStream ois = new ObjectInputStream(fis)){
                   
                  voterList = (CopyOnWriteArrayList<Voter>) ois.readObject();
            }catch(FileNotFoundException e){
                  try {
                        File myObj = new File("voterInformation");
                        if (myObj.createNewFile()) {
                          System.out.println("File created: " + myObj.getName());
                        } else {
                          System.out.println("File already exists.");
                        }
                      } catch (IOException ex) {
                        System.out.println("An error occurred.");
                        e.printStackTrace();
                      }
            }catch(Exception ex){
                  ex.printStackTrace();
            }

      }
      @Override
      public List<Voter> getVoterList() throws RemoteException {
            return this.voterList;
      }
      @Override
      public void setVoterList(List<Voter> voterList) throws RemoteException{
            this.voterList = voterList;
      }
      @Override
      public List<Election> getElections() throws RemoteException {
            return this.elections;
      }
      @Override
      public void setElections(List<Election> elections) throws RemoteException{
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
      public boolean addElection(Election election)  throws RemoteException{
            if(searchElection(election.getTitle())==null){
                  elections.add(election);
                  writeElectionFile();
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
      public boolean addMembroToLista(Election election, String nome,Voter member) throws RemoteException{
            int index = elections.indexOf(election);
            if(index != -1){
                  return elections.get(index).addMemberToLista(nome, member);
            }

            return false;
      }
      @Override
      public boolean removeMembroToLista(Election election, String nome,Voter member) throws RemoteException{
            int index = elections.indexOf(election);
            if(index != -1){
                  return elections.get(index).removeMemberToLista(nome, member);
            }

            return false;
      }

      @Override
      public boolean createVoter(String username, String department, String contact, String address, String cc_number, Calendar cc_expiring, String password,Type type)  throws RemoteException{
            if(searchVoter(username) == null && searchVoterCc(cc_number)==null){
                  Voter voter = new Voter(username, department, contact, address, cc_number, cc_expiring, password,type);
                  addVoter(voter);
                  writeVoterFile();
                  return true;
            }
            return false;
      }

      @Override
      public boolean voterVotes(String username,String title, String candidateName, String voteLocal)  throws RemoteException{
            /**
             * It receives the voter username, the title of the election and the candidate that is going to vote for
             * returns if there was a problem in the voting
             * Server e admins
             */
            Voter voter = searchVoter(username);
            Election election = searchElection(title);
            
            if(voter != null || election != null){
                  boolean flag =election.vote(voter, candidateName, voteLocal);
                  writeElectionFile();
                  return flag;
                  
            }
            return false;
      }

      @Override
      public boolean createElection(String title,Calendar beggDate,Calendar endDate,String department, List<Type> allowedVoters)  throws RemoteException{
            /**
             * Consola admin
             */
            if(searchElection(title) == null){
                  Election election = new Election(title, beggDate, endDate, department, allowedVoters);
                  addElection(election);
                  writeElectionFile();
                  return true;
            }
            return false;
      }
      @Override
      public boolean createCandidate(List<Voter> members, String name,String title,Type type) throws RemoteException{
            Candidates candidates = new Candidates(members, name,type);
            return addCandidate(title, candidates);
      }

      @Override
      public boolean switchElection(Election oriElection, Election newInfo) throws RemoteException{
            if(elections.contains(oriElection)){
                  int i = elections.indexOf(oriElection);
                  if(oriElection.getState() == State.WAITING){
                        elections.set(i, newInfo);
                  }else{
                        return false;
                  }
            }else{
                  return false;
            }
            return true;
      }

      @Override
      public boolean switchUser(Voter oriVoter, Voter newInfo) throws RemoteException{
            if(voterList.contains(oriVoter)){
                  int i = voterList.indexOf(oriVoter);
                  voterList.set(i, newInfo);
            }else{
                  return false;
            }
            return true;
      }

      public RMIServer() throws RemoteException{
            super();

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
            /*
            Calendar beggDate = Calendar.getInstance();
            beggDate.set(Calendar.YEAR,2021);
            beggDate.set(Calendar.MONTH, Calendar.MARCH);
            beggDate.set(Calendar.DATE,17);
            beggDate.set(Calendar.HOUR_OF_DAY,18);
            beggDate.set(Calendar.MINUTE,41);
            

            Calendar eCalendar = Calendar.getInstance();
            eCalendar.set(Calendar.YEAR,2021);
            eCalendar.set(Calendar.MONTH, Calendar.MARCH);
            eCalendar.set(Calendar.DATE,17);
            eCalendar.set(Calendar.HOUR,18);
            eCalendar.set(Calendar.MINUTE,42);
            List<Type> allowedVoters = new CopyOnWriteArrayList<>();
            allowedVoters.add(Type.STUDENT);
            System.out.println(Calendar.getInstance().getTimeInMillis());
            System.out.println(beggDate.getTimeInMillis() < Calendar.getInstance().getTimeInMillis());
            Election election = new Election("Ola", beggDate, eCalendar, "Date", allowedVoters);
            */
            
            RMIServer rmiServer = null;
            boolean flag = true;
            DatagramSocket aSocket = null;
            try{
                  rmiServer = new RMIServer();
                  LocateRegistry.createRegistry(5001).rebind("RMIServer", rmiServer);
                  System.out.println("RMIServer is on");
                  rmiServer.readElectionFile();
                  rmiServer.readVoterFile();
                  aSocket = new DatagramSocket(6789);
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
                        aSocket = new DatagramSocket(port+1);
                        aSocket.setSoTimeout(10000);
                        while (flag) {
                              byte[]  m = new byte[]{(byte)(flag?1:0)};
                              InetAddress aHost = InetAddress.getByName("localhost");
                              DatagramPacket request = new DatagramPacket(m, 1, aHost, port);
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
                              Thread.sleep(10000);
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
