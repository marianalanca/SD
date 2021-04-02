import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.*;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Calendar;
import java.util.concurrent.CopyOnWriteArrayList;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;

public class AdminConsole extends UnicastRemoteObject implements AdminConsole_I{   

    /**
     *
     */
    private static final long serialVersionUID = 1723520814373481188L;
    RMIServer_I rmi;
    Scanner myObj;  
    String address;
    int port;

    /**
     * Constructor AdminConsole
     * @param rmi interface RMIServer
     * @throws RemoteException
     */
    private AdminConsole() throws RemoteException {
        super();
        this.myObj = new Scanner(System.in); 
    }

    /**
     * prints all the features available on the administration console
     */
    public void menu(){
        System.out.println("\n0. Exit"); 
        System.out.println("Voter:\n\t1. Register voter"); 
        System.out.println("\t2. Change voter's personal data");                       
        System.out.println("Election:\n\t3. Create elections");   
        System.out.println("\t4. Change an election's properties");                     
        System.out.println("\t5. Manage candidate lists");                   
        System.out.println("Tables:\n\t6. Manage polling stations");
        System.out.println("\t7. Manage members of each polling station");  
        System.out.println("\t8. Show polling station status");             
        System.out.println("Consult:\n\t9. Local voted each voter");                             
        System.out.println("\t10. Show voters in real time");              
        System.out.println("\t11. See detailed results of past elections");  
        System.out.println("\t12. List all elections"); 
        System.out.println("\t13. List all voters"); 
        System.out.println("Extra:\n\t14. Early vote\n");                                                  
        
        options_menu();
    }

    /**
     * handles all the features available in the administration console
     */
    public void options_menu(){

        int option = check_number();

        switch(option){
            case 0:
                try{
                    rmi.logoutAdmin(this);
                    System.exit(0);
                }
                catch(Exception e){
                    System.out.println("Ups, I can't disconnect");
                }
                break;
            case 1:
                register_voter();
                break;
            case 2:
                change_voter_data();
                break;
            case 3:
                create_election();
                break;
            case 4:
                change_election();
                break;
            case 5:
                manage_list();
                break;
            case 6:
                manage_tables();
                break;
            case 7:
                manage_table_members();
                break;
            case 8:
                table_state();
                break;
            case 9:
                see_voters_local();
                break;
            case 10:
                voters_real_time();
                break;
            case 11:
                see_results();
                break;
            case 12:
                list_elections();
                break;
            case 13:
                list_voters();
                break;
            case 14:
                early_vote();
                break;
            default:
                System.out.println("Invalid option. Try again\n");
                break;
        }
        
        menu();
        
    }

    /**
     * Reads a string and evaluates the content of it is correct (doesn't contain ; or |)
     * @return the string read without errors
     */
    private String check_string(){

        String s;

        s = myObj.nextLine();

        while(s.contains(";") || s.contains("|")){
            System.out.println("Invalid character ; or |\nTry again");
            s = myObj.nextLine();
        }

        return s;
    }

    /**
     * Reads a string and evaluates if the given role in the string is valid
     * @return the correct role Type
     */
    private Type check_role(){

        String s;

        do{
            s = myObj.nextLine();

            if(s.equalsIgnoreCase("STUDENT")){
                return Type.STUDENT;
            }
            else if(s.equalsIgnoreCase("DOCENTE")){
                return Type.DOCENTE;
            }
            else if(s.equalsIgnoreCase("FUNCIONARIO")){
                return Type.FUNCIONARIO;
            }
            else{
                System.out.println("Invalid role");
            }

        }while(true);

    }

    /**
     * Reads a string, parse it into a int and evaluates if the given hour is correct
     * @return the hour
     */
    private int check_hour(){
  
        int h = check_number();

        while(h < 0 || h > 23){
            System.out.println("Invalid hour. Try again");
            h = check_number();
        }
        
        return h;

    }

    /**
     * Reads a string, parse it into a int and evaluates if the given minute is correct
     * @return the minute
     */
    private int check_minutes(){

        int m = check_number();

        while(m < 0 || m > 59){
            System.out.println("Invalid minutes. Try again");
            m = check_number();
        }

        return m;

    }

    /**
     * Reads a string, parse it into a integer and returns it
     * @return an integer number
     */
    private int check_number(){
        String aux;
        int n;

        do{
            aux = myObj.nextLine();
            try{
                n = Integer.parseInt(aux);
                return n;
            }
            catch(Exception e){
                System.out.print("Error in parse.\n Try again: ");
            }
        }while(true);
    }

    /**
     * Reads a date and retuns it in the Calendar format
     * @param i an integer that if i==1 we can specify hour and minutes
     * @return Calendar dete
     */
    private Calendar date(int i){

        int day, month, year;
        Calendar date_aux = Calendar.getInstance(); 

        System.out.print("\tDay:");
        day = check_number();
        System.out.print("\tMonth:");
        month = check_number() - 1;
        System.out.print("\tYear:");
        year = check_number();
        date_aux.set(year, month, day);

        if(i == 1){
            System.out.print("\tHour:");
            date_aux.set(Calendar.HOUR_OF_DAY, check_hour());
            System.out.print("\tMinute:");
            date_aux.set(Calendar.MINUTE, check_minutes());
        }

        return date_aux; 

    }

    /**
     * Recursive method that will try to recover the connetion with the rmi server when it fails
     */
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

    /**
     * Register a new voter.
     * All the information needed to create the new voter is read and treated. 
     * This information will be passed to the rmi server.
     */
    public void register_voter(){

        String name, department, contact, address, cc_number, password;
        Type role;
        Calendar cc_expiring;
        
        System.out.print("Enter name: ");
        name = check_string();

        System.out.print("Enter role: ");
        role = check_role();
        
        System.out.print("Enter cc number: ");
        cc_number = check_string();

        try{
            while(rmi.searchVoterCc(cc_number)!=null){
                System.out.print("That number is used. Try again: ");
                cc_number = check_string();
            }
        }
        catch (ConnectException e) {
           reconnect(); 
        }
        catch (Exception e) {
            System.out.println("Please try again");
            register_voter();
        }
        
        System.out.println("Enter cc expiring date: ");
        cc_expiring = date(0);
        
        System.out.print("Enter department: ");
        department = check_string();

        System.out.print("Enter contact: ");
        contact = check_string();

        System.out.print("Enter address: ");
        address = check_string();

        System.out.print("Enter password: ");
        password = check_string();

        try{
            if(rmi.createVoter(name, department, contact, address, cc_number, cc_expiring, password, role) )
                System.out.println("\nSuccessfully created new voter");
            else
                System.out.println("\nError creating new voter");
        }
        catch(ConnectException e){
            try{
                reconnect();
                if(rmi.createVoter(name, department, contact, address, cc_number, cc_expiring, password, role) )
                    System.out.println("\nSuccessfully created new voter");
            }
            catch(Exception excp){
                System.out.println("Register_voter: connecting failed " + excp);
            }
        }
        catch( Exception e){
            System.out.println("Error creating new voter: " + e);
        } 
    }

    /**
     * Create a new election.
     * All the information needed to create the new election is read and treated. 
     * This information will be passed to the rmi server.
     */
    public void create_election(){

        Calendar dateB = Calendar.getInstance(), dateE = Calendar.getInstance();
        List<Type> electionType = new CopyOnWriteArrayList<>();
        String electionName, description, department = "";
        int option;

        System.out.print("Insert election's name: ");
        electionName = check_string();
        
        System.out.println("1. General council election\n2. Simple election");
        
        option = check_number();

        switch(option){
            case 1:
                electionType.add(Type.STUDENT);
                electionType.add(Type.DOCENTE);
                electionType.add(Type.FUNCIONARIO);
                break;
            case 2:
                System.out.print("Insert election's type: ");
                electionType.add(check_role());
                System.out.print("Insert department: ");
                department = check_string();
                break;
            default:
                System.out.println("Invalid option.");
                break;
        }

        System.out.print("Insert a description: ");
        description = check_string();

        System.out.println("Insert begin date:");
        dateB = date(1);
        
        while(dateB.before(Calendar.getInstance())){
            System.out.println("Invalid date - insert new begin date:");
            dateB = date(1);
        }

        System.out.println("Insert end date:");
        dateE = date(1);

        while(dateE.before(dateB)){
            System.out.println("Invalid date - end date must be after\nInsert new end date:");
            dateE = date(1);
        }

        try{
            if(rmi.createElection(electionName, description, dateB, dateE, department, electionType))
                System.out.println("\nSuccessfully created new election");
            else
                System.out.println("\nError creating new election");
        }
        catch(ConnectException e){
            try{
                reconnect();
                if(rmi.createElection(electionName, description, dateB, dateE, department, electionType))
                    System.out.println("\nSuccessfully created new election");
                else
                    System.out.println("\nError creating new election");
            }
            catch(Exception excp){
                System.out.println("Create_election: connecting failed" + excp);
            }
        } 
        catch (Exception e){
            System.out.println("Error creating new election: " + e);
        }

    }

    /**
     * Prints all the lists in a election
     * @param c a list of candidates
     * @return a integer that correspond to the number of candidates in that election
     */
    private int printListInElection(List<Candidates> c){

        int size = c.size();

        System.out.println("\nPick a list: ");
        for(int i = 0; i < size; i ++){
            System.out.println(i + ". " + c.get(i).getName());
        }

        return size;
    }

    /**
     * prints all the available elections
     * @param elections a list of elections
     */
    private void printElection(List<Election> elections){

        System.out.println("\nPick a election:");
        for(int i =0; i < elections.size() ; i++){
            System.out.println(i + ". " + elections.get(i).getTitle());
        }

    }

    /**
     * Allows you to change the properties of the lists of an election:
     *  - Create a new list
     *  - Delete a existing list
     *  - Insert a candidate in a list
     *  - Delete a candidate in a list
     * All changes are passed to the rmi server
     */
    public void manage_list(){

        try{

            int option, optionCand, size;
            String nameList, cc_number; 
            List<Election> elections;
            List<Candidates> cand;
            List<Type> allowed;
            Election election;
            Type typeList;
            Voter voter;

            elections = rmi.stateElections(State.WAITING, null);

            if(elections.size() == 0){ 
                System.out.println("List of elections is empty.");
                return; 
            }

            printElection(elections);

            option = check_number();

            while(!(option >= 0 && option < elections.size())){
                System.out.println ("Invalid option. Try again");
                option = check_number();
            }

            election = elections.get(option);

            if(election.getAllowedVoters().size() == 3){
                System.out.println (election.getTitle() + " - general election ");
            }
            else{
                System.out.println (election.getTitle() + " -  " + election.getAllowedVoters().get(0));
            }

            System.out.println ("1. Create a new list\n2. Delete a existing list"); 
            System.out.println ("3. Insert a candidate in a list\n4. Delete a candidate in a list");         
            
            option = check_number();

            if(option == 1){

                System.out.print("Insert list's name: ");
                nameList = check_string();
                allowed = election.getAllowedVoters();

                if(allowed.size()==1){
                    typeList = allowed.get(0);
                }
                else{
                    System.out.print("Insert list's type: ");
                    typeList = check_role();
                }

                try{
                    if(rmi.createCandidate(nameList, election.getTitle(), typeList) )
                        System.out.println("Sucess creating new list.");
                    else
                        System.out.println("\nError in creating new list");
                }
                catch (ConnectException e){
                    reconnect();
                    if(rmi.createCandidate( nameList, election.getTitle(), typeList) )
                        System.out.println("Sucess creating new list.");
                    else
                        System.out.println("\nError in creating new list");
                }
                catch (Exception e){
                    System.out.println("Manage_list : " + e);
                }

            }
            else if(option == 2){

                cand = election.getCandidatesList();
                size = printListInElection(cand);

                if(size == 0){ 
                    System.out.println("List of candidates is empty.");
                    return; 
                }
                 
                option = check_number();

                while(!(option >= 0 && option < size)){
                    System.out.println("Invalid option. Try again: ");
                    option = check_number();
                }

                try{
                    if(rmi.removeCandidate(election.getTitle(), cand.get(option).getName()) )
                        System.out.println("Sucess removing list.");
                    else
                        System.out.println("\nError removing list");
                }
                catch (ConnectException e){
                    reconnect();
                    if(rmi.removeCandidate(election.getTitle(), cand.get(option).getName()) )
                        System.out.println("Sucess removing list.");
                }
                catch (Exception e){
                    System.out.println("Manage_list : " + e);
                }
                
            }
            else if(option == 3 || option == 4){

                cand = election.getCandidatesList();
                size = printListInElection(cand);
                optionCand = check_number();

                while(!(optionCand >= 0 && optionCand < size)){
                    System.out.print("Invalid option. Try again: ");
                    optionCand = check_number();
                }
                
                System.out.print("Insert voter's citizen card number: ");
                cc_number = check_string();

                if(cc_number.equals("")){
                    return;
                }
                
                voter = rmi.searchVoterCc(cc_number);
                        
                while(voter == null){
                    System.out.print("Invalid voter's citizen card number. Try again: ");
                    cc_number = check_string();
                    voter = rmi.searchVoterCc(cc_number);
                }

                try{

                    if(option == 3){
                        if(rmi.addMembroToLista(election, cand.get(optionCand).getName(), voter))
                            System.out.println("Sucess adding member to list");
                        else
                            System.out.println("Error adding menber to list");
                    }
                    else{
                        if(rmi.removeMembroToLista(election, cand.get(optionCand).getName(), voter))
                            System.out.println("Sucess removing member to list");
                        else
                            System.out.println("Error adding member to list");
                    }

                } 
                catch(ConnectException e){
                    reconnect();

                    if(option == 3){
                        if(rmi.addMembroToLista(election, cand.get(optionCand).getName(), voter) )
                            System.out.println("Sucess adding member to list");
                        else
                            System.out.println("Error adding member to list");
                    }
                    else{
                        if(rmi.removeMembroToLista(election, cand.get(optionCand).getName(), voter) )
                            System.out.println("Sucess removing member to list");
                    }

                }
                catch(Exception e){
                    System.out.println("Error adding/removing member to list: " + e );
                    e.printStackTrace();
                }
                
            }
            else{
                System.out.println("Invalid option. Try again");
            }

        } 
        catch (ConnectException e){
            reconnect();
            manage_list();
        }
        catch (Exception e){
            System.out.println("Manage_list : " + e);
        }
    }

    /**
     * Allows you to change the tables of an election:
     *  - Add table to election
     *  - Remove table to election
     * All changes are passed to the rmi server
     */
    public void manage_tables(){

        try{

            List<Election> elections;
            MulticastServer table;
            Election election;
            int option;
            String dep;

            elections = rmi.stateElections(State.WAITING, null);

            if(elections.size() == 0){ 
                System.out.println("The list of elections is empty.");
                return; 
            }
            
            printElection(elections);

            option = check_number();

            while(!(option >= 0 && option < elections.size())){
                System.out.println ("Invalid option. Try again");
                option = check_number();
            }

            election = elections.get(option);

            System.out.println("1. Add table to election\n2. Remove table to election");
            option = check_number();

            while(option < 1 || option > 2){
                System.out.print("Inavalid option. Try again: ");
                option = check_number();
            }

            System.out.print("Insert table's department: ");
            dep = check_string();

            if(dep.equals("")){
                return;
            }

            table =  rmi.searchTableDept(dep);

            while(table == null){
                System.out.print("Invalid id. Try again: ");
                dep = check_string();
                table =  rmi.searchTableDept(dep);
            }

            try{
                if(option == 1){
                    if(rmi.addTableElection(table, election))
                        System.out.println("Sucess adding table ");
                    else
                        System.out.println("Error adding table");
                }
                else{
                    if(rmi.removeTableElection(table, election))
                        System.out.println("Sucess removing table ");
                    else
                        System.out.println("Error removing table");
                }
            }
            catch(ConnectException e){
                reconnect();
                if(option == 1){
                    if(rmi.addTableElection(table, election))
                        System.out.println("Sucess adding table ");
                }
                else{
                    if(rmi.removeTableElection(table, election))
                        System.out.println("Sucess removing table ");
                }
            }
            catch(Exception e){
                System.out.println("Error adding/removing table: " + e);
            }

        }
        catch(ConnectException e){
            reconnect();
            manage_list();
        }
        catch(Exception e){
            System.out.println("Manage_tables: " + e);
        }

    }

    /**
     * Allows you to change the properties of an election
     * All changes are passed to the rmi server
     */
    public void change_election(){

        try{

            Calendar date;
            List<Election> elections;
            Election election = null;
            Election new_election = null;
            String aux;
            int option;

            elections = rmi.stateElections(State.WAITING, null);

            if(elections.size() == 0){ 
                System.out.println("List of waiting elections is empty.");
                return; 
            }

            printElection(elections);

            option = check_number();

            if(option >= 0 && option < elections.size()){

                election = elections.get(option);

                System.out.println("Change "+ election.getTitle() +" properties\n1.Change title\n2.Change description\n3.Beginning date\n4.End date");

                option = check_number();
                
                if(option == 1){
                    System.out.print("Insert new title: ");
                    aux = check_string();
                    new_election = new Election(aux, election.getDescription(), election.getBeggDate(), election.getEndDate(), election.getDepartment(), election.getAllowedVoters());
                }
                else if(option == 2){
                    System.out.print("Insert new description: ");
                    aux = check_string();
                    new_election = new Election(election.getTitle(), aux, election.getBeggDate(), election.getEndDate(), election.getDepartment(), election.getAllowedVoters());
                }
                else if(option == 3){
                    System.out.println(election.getBeggDate().getTime());
                    System.out.println("Insert new date:");
                    date = date(1);

                    //date B
                    while(date.before(Calendar.getInstance())){
                        System.out.println("Invalid date - insert new begin date:");
                        date = date(1);
                    }

                    new_election = new Election(election.getTitle(), election.getDescription(), date, election.getEndDate(), election.getDepartment(), election.getAllowedVoters());
                
                }
                else if(option == 4){
                    System.out.println(election.getEndDate().getTime());
                    System.out.println("Insert new date:");
                    date = date(1);

                    //date E
                    while(date.before(election.getBeggDate())){
                        System.out.println("Invalid date - end date must be after\nInsert new end date:");
                        date = date(1);
                    }
                    new_election = new Election(election.getTitle(), election.getDescription(), election.getBeggDate(), date, election.getDepartment(), election.getAllowedVoters());
                
                }
                else{
                    System.out.println("Invalid option!");
                }  
            }
            else{
                System.out.println("Invalid option!");
            }

            try{
                if(rmi.switchElection(election.getTitle(), new_election))
                    System.out.println("Sucess updating election information");
                else
                    System.out.println("Error updating election information");
                
            }
            catch(ConnectException e){
                reconnect();
                if(rmi.switchElection(election.getTitle(), new_election) ){
                    System.out.println("Sucess in updating election information");
                }
            }
            catch(Exception e){
                System.out.println("Error updating election information: " + e);
            }

        } 
        catch(ConnectException e){
            reconnect();
            change_election();
        }
        catch (Exception e){
            System.out.println("Change_eletion:  " + e);
        }
    }

    /**
     * Allows to know at which polling station and at what time each voter voted.
     */
    public void see_voters_local(){

        try{

            List<AlreadyVoted> voters;
            List<Election> elections;
            AlreadyVoted voter;
            Election election;
            int option;

            elections = rmi.stateElections(State.CLOSED, null);

            if(elections.size() == 0){
                System.out.println("The list of closed elections is empty");
                return;
            }

            printElection(elections);

            option = check_number();

            while(option < 0 || option > elections.size()){
                System.out.print("Invalid option. Try again: ");
                option = check_number();
            }

            election = elections.get(option);
            voters = election.getUsersVoted();

            for(int i = 0; i<voters.size(); i++ ){
                voter = voters.get(i);
                System.out.println(voter.getVote().getUsername() + ": " + voter.getLocal() + " " +voter.getTimeOfVote());
            }

        }
        catch(ConnectException e){
            reconnect();
            see_voters_local();
        }
        catch (Exception e){
            System.out.println("See_voter_local: " + e);
        }
    }

    @Override
    public void notify_state(String notification) throws RemoteException{
        System.out.println(notification + "\n");
    }

    /**
     * Print all the tables and their state
     */
    public void table_state(){
        
        try{

            List<MulticastServer> tablesOff;
            List<MulticastServer> tablesOn;

            tablesOff = rmi.getServers();
            tablesOn = rmi.getOnServers();
            

            System.out.println("Tables On");
            for(int i = 0; i < tablesOn.size(); i++){
                System.out.println(tablesOn.get(i).getQ().getDepartment());
            }
            boolean flag;
            System.out.println("\nTables Off");
            for(int i = 0; i < tablesOff.size(); i++){
                flag = true;
                for (MulticastServer multicastServer : tablesOn) {
                    if(multicastServer.getQ().getDepartment().equals(tablesOff.get(i).getQ().getDepartment())){
                        flag = false;
                        break;
                    }
                }
                if(flag){
                    System.out.println(tablesOff.get(i).getQ().getDepartment());
                }
            }
            
        }catch(ConnectException e){
            reconnect();
            table_state();
        }
        catch (Exception e){
            System.out.println("Table_state: exception in RMIServer.java(main) " + e);
        }
        

    }

    /**
     * prints the number of voters who have voted so far at each polling station for a chosen election
     */
    public void voters_real_time(){
        try {
            Runtime rt = Runtime.getRuntime();
            rt.exec("cmd.exe /c start java -jar realtime.jar");
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*
        try{

            List <Election> elections;
            Election election;
            int option;

            elections = rmi.stateElections(State.OPEN, null);

            if(elections.size() == 0){ 
                System.out.println("List of open elections is empty.");
                return; 
            }

            //Runtime rt = Runtime.getRuntime();
            //rt.exec("cmd.exe /c start java -jar console.jar");

            printElection(elections);

            option = check_number();

            while(option < 0 || option > elections.size()){
                System.out.println ("Invalid option. Try again");
                option = check_number();
            }

            election = elections.get(option);
            System.out.println("\nElection " + election.getTitle() + ":");

            (new Thread() {
                public void run() {
                    try {
                        Runtime.getRuntime().exec("cmd /c start cmd.exe");
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

        }catch(ConnectException e){
            reconnect();
            voters_real_time();
        }
        catch (Exception e){
            System.out.println("Voters_real_time: " + e);
        }*/

    }

    /**
     * Allows to see the results of a past election.
     */
    public void see_results(){

        try{

            List<Election> elections;
            Election election;
            int option;

            elections = rmi.stateElections(State.CLOSED, null);

            if(elections.size() == 0){ 
                System.out.println("List of closed elections is empty.");
                return; 
            }

            printElection(elections);

            option = check_number();

            while(!(option >= 0 && option < elections.size())){
                System.out.println("Invalid option! ");
                option = check_number();
            }
            
            election = elections.get(option);
            System.out.println("Result " + election.getTitle() + ": ");
            System.out.println("White - " + election.getWhiteVote()+ "\nNull - " + election.getNullVote());
            election.results();

        }
        catch (ConnectException e){
            reconnect();
            see_results();
        }
        catch(Exception e){
            System.out.println("Error : ");
        }        
    }

    /**
     * Allows a voter to be able to vote in a election, before the official start date and time through the administration console.
     */
    public void early_vote(){
        try{

            List<Candidates> candidates = new CopyOnWriteArrayList<>();
            List<Election> elections = new CopyOnWriteArrayList<>();
            String name, cand_name = "";
            Election election;
            String option_aux;
            int option, size;
            Voter voter = null;


            System.out.print("Insert name: ");
            name = check_string();

            if(name.equals("")){
                return;
            }

            voter = rmi.searchVoter(name);

            while(voter == null){
                System.out.print("Invalid username. Try again: ");
                name = check_string();
                voter = rmi.searchVoter(name);
            }

            elections = rmi.stateElections(State.WAITING, voter.getType());

            if(elections.size() == 0){ 
                System.out.println("List of waiting elections is empty.");
                return; 
            }

            printElection(elections);

            option = check_number();

            if(option >= 0 && option < elections.size()){
                election = elections.get(option);

                candidates = election.getCandidatesList();
                size = printListInElection(candidates);

                option_aux = check_string();

                if(!option_aux.equals("")){
                    try{
                        option = Integer.parseInt(option_aux);
                        if(option >=0 && option < size){
                            cand_name = candidates.get(option).getName();
                        }
                        else{
                            cand_name = null;
                        }
                    }
                    catch(Exception e){
                        cand_name = null;
                    }
                }

                try{
                    if(rmi.voterVotesAdmin(name, election.getTitle(), cand_name, election.getDepartment()) )
                        System.out.println("Sucess early vote");
                    else
                        System.out.println("Error in early vote");
                }
                catch(ConnectException e){
                    reconnect();
                    if(rmi.voterVotesAdmin(name, election.getTitle(), cand_name, election.getDepartment()) )
                        System.out.println("Sucess early vote");
                    else
                        System.out.println("Error in early vote");
                }
                catch(Exception e){
                    System.out.println("Error in early vote: " + e);
                }

            }
            else{
                System.out.println("Invalid option!");
            }

        }
        catch(ConnectException e){
            reconnect();
            early_vote();
        }
        catch(Exception e){
            System.out.println("Early vote: " + e);
        }

    } 

    /**
     * Allows you to change the properties of a voter
     * All changes are passed to the rmi server
     */
    public void change_voter_data(){

        try{
            Voter voter, new_voter;
            String cc_number;
            Calendar aux;
            int option;

            System.out.print("Insert citizen card number: ");
            cc_number = check_string();

            if(cc_number.equals("")){
                return;
            }

            voter = rmi.searchVoterCc(cc_number);

            while(voter == null){
                System.out.print("Invalid voter's citizen card number. Try again: ");
                cc_number = check_string();
                voter = rmi.searchVoterCc(cc_number);
            }

            new_voter = new Voter(voter.getUsername(), voter.getDepartment(), voter.getContact(), voter.getAddress(), voter.getCc_number(), voter.getCc_expiring(), voter.getPassword(), voter.getType());
            
            System.out.println("1. Change name");
            System.out.println("2. Change role");
            System.out.println("3. Change department");
            System.out.println("4. Change address");
            System.out.println("5. Change contact");
            System.out.println("6. Change citizen card number");
            System.out.println("7. Change citizen card expiring date");
            System.out.println("8. Password\n");

            option = check_number();

            switch(option){
                case 1:
                    System.out.print(voter.getUsername() + "\nEnter new name: ");
                    new_voter.setUsername(check_string());
                    break;
                case 2:
                    System.out.print(voter.getType() + "\nEnter new role: ");
                    new_voter.setType(check_role());
                    break;
                case 3:
                    System.out.print(voter.getDepartment() + "\nEnter new department: ");
                    new_voter.setDepartment(check_string());
                    break;
                case 4:
                    System.out.print("Enter address: ");
                    new_voter.setUsername(check_string());
                    break;
                case 5:
                    System.out.print(voter.getContact() + "\nEnter new contact: ");
                    new_voter.setContact(check_string());
                    break;
                case 6:
                    System.out.print(voter.getCc_number() + "\nEnter new citizen card number: ");
                    new_voter.setCc_number(check_string());
                    break;
                case 7:
                    System.out.println("Enter new date: ");
                    aux = date(0);
                    new_voter.setCc_expiring(aux);

                    break;
                case 8:
                    System.out.print("Enter new password: ");
                    new_voter.setPassword(check_string());
                    break;
                default:
                    System.out.println("Invalid option. Try again");
                    break;
            }

            try{
                if(rmi.switchUser(voter.getCc_number(), new_voter) )
                    System.out.println("Sucess in updating voter information. ");
            }
            catch(ConnectException e){
                reconnect();
                if(rmi.switchUser(voter.getCc_number(), new_voter) )
                    System.out.println("Sucess in updating voter information. ");
            }
            catch(Exception e){
                System.out.println("Error updating voter information: " + e);
            }
        }
        catch(ConnectException e){
            reconnect();
            change_voter_data();
        }
        catch(Exception e){
            System.out.println("Change_voter_data: " + e);
        }
        
        
    }

    /**
     * Allows you to change the tables of an election: <p>
     *  - Add new member to a table
     *  - Remove a member of a table
     * All changes are passed to the rmi server
     */
    public void manage_table_members(){

        try{

            MulticastServer table;
            List<Voter> members;
            Voter voter = null;
            String dep, cc_number;
            int option, aux;

            System.out.print("Insert table's department: ");
            dep = check_string();

            if(dep.equals("")){
                return;
            }

            table =  rmi.searchTableDept(dep);

            while(table == null){
                System.out.print("Invalid department. Try again: ");
                dep = check_string();
                table =  rmi.searchTableDept(dep);
            }

            members = table.getTableMembers();

            System.out.println("1. Add new member to table\n2. Remove member to table");
            option = check_number();

            while(option < 1 || option > 2){
                System.out.print("Inavalid option. Try again: ");
                option = check_number();
            }

            if(option == 1){
                if(members.size() == 3){
                    System.out.println("Error: the table is already full");
                }
                else{
                    System.out.print("Insert voter's citizen card number: ");
                    cc_number = check_string();

                    if(cc_number.equals("")){ return; }

                    voter = rmi.searchVoterCc(cc_number);

                    while(voter == null){
                        System.out.print("Invalid voter's citizen card number. Try again: ");
                        cc_number = check_string();
                        voter = rmi.searchVoterCc(cc_number);
                    }
                }
            }
            else{
                System.out.println("Pick the member: ");
                for(int i =0; i< members.size(); i++){
                    System.out.println(i + ". " + members.get(i).getUsername());
                }

                aux = check_number();

                while(aux < 0 || aux > members.size() - 1 ){
                    System.out.print("Invalid option. Try again: ");
                    aux = check_number();
                }

                voter = members.get(aux);
            }

            try{
                if(option == 1){
                    if(rmi.addVoterTable(table, voter))
                        System.out.println("Sucess adding member to table");
                }
                else{
                    if (rmi.removeVoterTable(table, voter))
                        System.out.println("Sucess removing member to table");
                }
            }
            catch(ConnectException e){
                reconnect();
                if(option == 1){
                    if(rmi.addVoterTable(table, voter) )
                        System.out.println("Sucess adding member to table");
                }
                else{
                    if (rmi.removeVoterTable(table, voter) )
                        System.out.println("Sucess removing member to table");
                }
            }
            catch(Exception e){
                System.out.println("Error adding/removing table: " + e);
            }

        }
        catch(ConnectException e){
            reconnect();
            manage_table_members();
        }
        catch(Exception e){
            System.out.println("Manage_tables: " + e);
        }

    }

    /**
     * list all election, its type, its state and the begging and end date
     */
    public void list_elections(){
        try{
            List<Election> elections;

            elections = rmi.getElections();

            if(elections.size() == 0){
                System.out.println("The list of elections is empty\n");
                return;
            }

            for(Election e: elections){
                if(e.getAllowedVoters().size() == 3){
                    System.out.println(e.getTitle() + ": general consul - " + e.getState());
                }
                else{
                    System.out.println(e.getTitle() + ": " + e.getAllowedVoters().get(0) + " - " + e.getState());
                }
                System.out.println("\tBegging date: " + e.getBeggDate().getTime() + "\n\tEnd date: " + e.getEndDate().getTime()  + "\n");
            }
        }
        catch(ConnectException e){
            reconnect();
            list_elections();
        }catch(Exception e){
            System.out.println("List_elections: " + e);
        }
    }

    public void list_voters(){
        try{
            List<Voter> voters;

            voters = rmi.getVoterList();

            if(voters.size() == 0){
                System.out.println("There is no voters in the database\n");
                return;
            }

            System.out.println("List of voters:");

            for(Voter v: voters){
                System.out.println(v.getUsername() + " " + v.getCc_number());
            }

            System.out.println("\n");

        }
        catch(ConnectException e){
            reconnect();
            list_voters();
        }catch(Exception e){
            System.out.println("List_voters: " + e);
        }
    }

    public static void main(String args[]) {

        try{ 

            AdminConsole admin = new AdminConsole();
            AdminConsole_I admin_I = (AdminConsole_I) admin;
            
            try{
                Properties prop = new Properties();
                String fileName = "config.properties";
                
                prop.load(new FileInputStream(fileName));
                admin.port = Integer.parseInt(prop.getProperty("port"));
                admin.address = prop.getProperty("ip");
                admin.rmi = (RMIServer_I) LocateRegistry.getRegistry(admin.address, admin.port).lookup("RMIServer");

            }catch(Exception e){
                System.out.println("Error in the file config.properties: " + e);
                System.exit(-1);   
            }

            try{
                admin.rmi.loginAdmin(admin_I);
            }
            catch(ConnectException e){
                admin.reconnect();
                admin.rmi.loginAdmin(admin_I);
            }
            catch(Exception e){
                System.out.println("Login Admin failed: " + e);
            }

            admin.menu();
            admin.myObj.close();

        }
        catch (Exception e){
            System.out.println("Main: " + e);
        }

    }

}
