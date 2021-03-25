import java.rmi.*;
import java.util.List;
import java.util.Scanner;
import java.util.Calendar;
import java.util.concurrent.CopyOnWriteArrayList;
import java.rmi.server.UnicastRemoteObject;
//import java.io.Serializable;

public class AdminConsole extends UnicastRemoteObject implements AdminConsole_I/*, Serializable */{   

    /**
     *
     */
    //private static final long serialVersionUID = 1L;

    RMIServer_I rmi;
    Scanner myObj  = new Scanner(System.in);

    private AdminConsole(RMIServer_I rmi) throws RemoteException {
        super();
        this.rmi = rmi;
    }

    public void menu(){
        System.out.println("\n1. Register people");                       //done
        System.out.println("2. Create elections");                        //done
        System.out.println("3. Manage candidate lists");                  //done - mas ver
        System.out.println("4. Manage polling stations");                 //done - mas ver
        System.out.println("5. Change an election's properties");         //done
        System.out.println("6. Local voted each voter");                  //nop
        System.out.println("7. Show polling station status");             //nop
        System.out.println("8. Show voters in real time");                //nop
        System.out.println("9. See detailed results of past elections");  //done
        System.out.println("10. Early vote");                               //done
        System.out.println("11. Change personal data");                     //done
        System.out.println("12. Manage members of each polling station");   //done - mas ver

        options_menu();
    }

    public void options_menu(){

        int option = check_number();

        switch(option){
            case 1:
                register_voter();
                break;
            case 2:
                create_election();
                break;
            case 3:
                manage_list();
                break;
            case 4:
                manage_tables();
                break;
            case 5:
                change_election();
                break;
            case 6:
                see_voters_local();
                break;
            case 7:
                table_state();
                break;
            case 8:
                voters_real_time();
                break;
            case 9:
                see_results();
                break;
            case 10:
                early_vote();
                break;
            case 11:
                change_voter_data();
                break;
            case 12:
                manage_table_members();
                break;
            default:
                System.out.println("Invalid option. Try again\n");
                break;
        }
        
        
        //Runtime.getRuntime().exec("cls");
        menu();
        
    }

    private String check_string(){

        String s;

        s = myObj.nextLine();

        while(s.contains(";") || s.contains("|")){
            System.out.println("Invalid character ; or |\nTry again");
            s = myObj.nextLine();
        }

        return s;
    }

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

    private int check_hour(){
  
        int h = check_number();

        while(h < 0 || h > 23){
            System.out.println("Invalid hour. Try again");
            h = check_number();
        }
        
        return h;

    }

    private int check_minutes(){

        int m = check_number();

        while(m < 0 || m > 60){
            System.out.println("Invalid minutes. Try again");
            m = check_number();
        }

        return m;

    }

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

    private Calendar date(int i){

        int day, month, year;
        Calendar date_aux = Calendar.getInstance(); 

        System.out.print("\tDay:");
        day = check_number();
        System.out.print("\tMonth:");
        month = check_number();
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

    public void reconnect(){
        try{
            rmi = (RMIServer_I) Naming.lookup("rmi://localhost:5001/RMIServer");
        }
        catch(ConnectException e){
            reconnect();
        }
        catch(Exception e){
            System.out.println("reconnect and I are not friends :) " + e);
        }
    }

    public void register_voter(){

        String name, department, contact, address, cc_number, password;
        Type role;
        Calendar cc_expiring;

        System.out.print("Enter name: ");
        name = check_string();

        System.out.print("Enter role: ");
        role = check_role();

        System.out.print("Enter department: ");
        department = check_string();

        System.out.print("Enter contact: ");
        contact = check_string();

        System.out.print("Enter address: ");
        address = check_string();
        
        System.out.print("Enter cc number: ");
        cc_number = check_string();        
        
        System.out.println("Enter cc expiring date: ");
        cc_expiring = date(0);

        System.out.print("Enter password: ");
        password = check_string();

        try{
            rmi.createVoter(name, department, contact, address, cc_number, cc_expiring, password, role); 
            System.out.println("\nSuccessfully created new voter");
        }
        catch(ConnectException e){
            try{
                reconnect();
                rmi.createVoter(name, department, contact, address, cc_number, cc_expiring, password, role); 
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

    public void create_election(){

        Calendar dateB = Calendar.getInstance(), dateE = Calendar.getInstance();
        List<Type> electionType = new CopyOnWriteArrayList<>();
        String electionName, description, department = null;
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
                System.out.println("Insert election's type");
                electionType.add(check_role());
                System.out.println("Insert department");
                department = check_string();
                break;
            default:
                System.out.println("Invalid option.");
                break;
        }

        System.out.print("Insert a description");
        description = check_string();

        System.out.println("Insert begin date:");
        dateB = date(1);
        
        while(dateB.after(Calendar.getInstance())){
            System.out.println("Invalid date - insert new begin date:");
            dateB = date(1);
        }

        System.out.println("Insert end date:");
        dateE = date(1);

        while(dateE.before(dateB)){
            System.out.println("Invalid date - end date must be after\nInsert new end date:");
            dateB = date(1);
        }

        try{
            rmi.createElection(electionName, description, dateB, dateE, department, electionType);
            System.out.println("\nSuccessfully created new election");
        }
        catch(ConnectException e){
            try{
                reconnect();
                rmi.createElection(electionName, description, dateB, dateE, department, electionType);
                System.out.println("\nSuccessfully created new election");
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
     * prints all the lists in a election
     * @param c list of candidates
     * @return number of candidates
     */
    private int printListInElection(List<Candidates> c){

        int size = c.size();

        System.out.println("Pick a list: ");
        for(int i = 0; i < size; i ++){
            System.out.println(i + ". " + c.get(i));
        }

        return size;
    }

    private void printElection(List<Election> elections){

        System.out.println("Pick a election:");
        for(int i =0; i < elections.size() ; i++){
            System.out.println(i + ". " + elections.get(i));
        }

    }

    public void manage_list(){ //REVER 3 e 4 - repetitivo?????

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

            printElection(elections);

            option = check_number();

            while(!(option >= 0 && option < elections.size())){
                System.out.println ("Invalid option. Try again");
                option = check_number();
            }

            election = elections.get(option);

            System.out.println ("1. Create a new list");
            System.out.println ("2. Delete a existing list"); 
            System.out.println ("3. Insert a candidate in a list");   
            System.out.println ("4. Delete a candidate in a list");         
            
            option = check_number();

            if(option == 1){

                System.out.println ("Insert list's name: ");
                nameList = check_string();
                allowed = election.getAllowedVoters();

                if(allowed.size()==1){
                    typeList = allowed.get(0);
                }
                else{
                    System.out.println ("Insert list's type: ");
                    typeList = check_role();
                }

                try{
                    rmi.createCandidate(null, nameList, election.getTitle(), typeList);
                    System.out.println("Sucess creating new list.");
                }
                catch (ConnectException e){
                    reconnect();
                    rmi.createCandidate(null, nameList, election.getTitle(), typeList);
                    System.out.println("Sucess creating new list.");
                }
                catch (Exception e){
                    System.out.println("Manage_list : " + e);
                }

            }
            else if(option == 2){

                cand = election.getCandidatesList();
                size = printListInElection(cand);
                option = check_number();

                while(!(option >= 0 && option < size)){
                    System.out.println ("Invalid option. Try again: ");
                    option = check_number();
                }
                try{
                    rmi.removeCandidate(election.getTitle(), cand.get(option).getName());
                    System.out.println("Sucess removing list.");
                }
                catch (ConnectException e){
                    reconnect();
                    rmi.removeCandidate(election.getTitle(), cand.get(option).getName());
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
                
                try{
                    voter = rmi.searchVoterCc(cc_number);
                        
                    while(voter == null){
                        System.out.print("Invalid voter's citizen card number. Try again: ");
                        cc_number = check_string();
                        voter = rmi.searchVoterCc(cc_number);
                    }

                    if(option == 3){
                        rmi.addMembroToLista(election, cand.get(option).getName(), voter);
                    }
                    else{
                        rmi.removeMembroToLista(election, cand.get(option).getName(), voter);
                    }

                    System.out.println("Sucess adding member to list");

                } 
                catch(ConnectException e){
                    reconnect();
                    voter = rmi.searchVoterCc(cc_number);
                        
                    while(voter == null){
                        System.out.print("Invalid voter's citizen card number. Try again: ");
                        cc_number = check_string();
                        voter = rmi.searchVoterCc(cc_number);
                    }

                    if(option == 3){
                        rmi.addMembroToLista(election, cand.get(option).getName(), voter);
                    }
                    else{
                        rmi.removeMembroToLista(election, cand.get(option).getName(), voter);
                    }
                    System.out.println("Sucess adding member to list");
                }
                catch(Exception e){
                    System.out.println("Error adding member to list: " + e);
                }
                
            }
            else{
                System.out.println ("Invalid option. Try again");
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

    public void manage_tables(){

        try{

            List<Election> elections;
            MulticastServer table;
            Election election;
            int option;
            String id;

            elections = rmi.stateElections(State.WAITING, null);
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

            System.out.print("Insert table's id: ");
            id = check_string();
            table =  rmi.searchTable(id);

            while(table == null){
                System.out.print("Invalid id. Try again: ");
                id = check_string();
                table =  rmi.searchTable(id);
            }

            try{
                if(option == 1){
                    rmi.addTableElection(table, election);
                    System.out.println("Sucess adding table ");
                }
                else{
                    rmi.removeTableElection(table, election);
                    System.out.println("Sucess removing table ");
                }
            }
            catch(ConnectException e){
                reconnect();
                if(option == 1){
                    rmi.addTableElection(table, election);
                    System.out.println("Sucess adding table ");
                }
                else{
                    rmi.removeTableElection(table, election);
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

    public void change_election(){

        try{

            Calendar date;
            List<Election> elections;
            Election election = null;
            String aux;
            int option;

            elections = rmi.stateElections(State.WAITING, null);
            printElection(elections);

            option = check_number();

            if(option >= 0 && option < elections.size()){

                election = elections.get(option);

                System.out.println("Change "+ election.getTitle() +" properties\n1.Change title\n2.Change description\n3.Beginning date\n4.End date");

                option = check_number();
                
                if(option == 1){
                    System.out.println("Insert new title: ");
                    aux = check_string();
                    election.setTitle(aux);
                }
                else if(option == 2){
                    System.out.println("Insert new description: ");
                    aux = check_string();
                    election.setDescription(aux);
                }
                else if(option == 3 || option == 4){
                    System.out.print("Insert new date:");
                    date = date(1);

                    if(option == 3){ //date B
                        while(date.after(Calendar.getInstance())){
                            System.out.println("Invalid date - insert new begin date:");
                            date = date(1);
                        }
                    }
                    else{ //date E
                        while(date.before(election.getBeggDate())){
                            System.out.println("Invalid date - end date must be after\nInsert new end date:");
                            date = date(1);
                        }
                    }
                }
                else{
                    System.out.println("Invalid option!");
                }  
            }
            else{
                System.out.println("Invalid option!");
            }

            try{
                rmi.switchElection(elections.get(option), election);
                System.out.println("Sucess in updating election information");
            }
            catch(ConnectException e){
                reconnect();
                rmi.switchElection(elections.get(option), election);
                System.out.println("Sucess in updating election information");
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
            System.out.println("Change_eletion: Exception in RMIServer.java(main) " + e);
        }
    }

    public void see_voters_local(){

        try{

            List<AlreadyVoted> voters;
            List<Election> elections;
            AlreadyVoted voter;
            Election election;
            int option;

            elections = rmi.stateElections(State.CLOSED, null);
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
            System.out.println("See_voter_local: exception in RMIServer.java(main) " + e);
        }
    }

    public void table_state(){}

    public void voters_real_time(){}

    public void see_results(){

        try{

            List<Election> elections;
            Election election;
            int option;

            elections = rmi.stateElections(State.CLOSED, null);
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

    public void early_vote(){
        try{

            List<Candidates> candidates = new CopyOnWriteArrayList<>();
            List<Election> elections = new CopyOnWriteArrayList<>();
            String name, cand_name = null;
            Election election;
            int option, size;
            Voter voter = null;


            System.out.print("Insert name: "); //preferia fazer pelo cc_number, discutir com o goncalo!!
            name = check_string();
            voter = rmi.searchVoter(name);

            while(voter == null){
                System.out.print("Invalid username. Try again: ");
                name = check_string();
                voter = rmi.searchVoter(name);
            }

            elections = rmi.stateElections(State.WAITING, voter.getType());
            printElection(elections);

            option = check_number();

            if(option >= 0 && option < elections.size()){
                election = elections.get(option);

                candidates = election.getCandidatesList();
                size = printListInElection(candidates);

                option = check_number();

                if(option >=0 && option < size){
                    cand_name = candidates.get(option).getName();
                }
                try{
                    rmi.voterVotesAdmin(name, election.getTitle(), cand_name, election.getDepartment());
                    System.out.println("Sucess early vote");
                }
                catch(ConnectException e){
                    reconnect();
                    rmi.voterVotesAdmin(name, election.getTitle(), cand_name, election.getDepartment() );
                    System.out.println("Sucess early vote");
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

    public void change_voter_data(){

        try{
            Voter voter, new_voter;
            String cc_number;
            Calendar aux;
            int option;

            System.out.print("Insert citizen card number: ");
            cc_number = check_string();

            voter = rmi.searchVoterCc(cc_number);

            while(voter == null){
                System.out.print("Invalid voter's citizen card number. Try again: ");
                cc_number = check_string();
                voter = rmi.searchVoterCc(cc_number);
            }

            new_voter = voter;
            
            System.out.println("1. Change name");
            System.out.println("2. Change role");
            System.out.println("3. Change department");
            System.out.println("4. Change contact");
            System.out.println("5. Change address");
            System.out.println("6. Change citizen card number");
            System.out.println("7. Change citizen card expiring date");
            System.out.println("8. Password\n");

            option = check_number();

            switch(option){
                case 1:
                    System.out.print("Enter new name: ");
                    new_voter.setUsername(check_string());
                    break;
                case 2:
                    System.out.print("Enter new role: ");
                    new_voter.setType(check_role());
                    break;
                case 3:
                    System.out.print("Enter new department: ");
                    new_voter.setDepartment(check_string());
                    break;
                case 4:
                    System.out.print("Enter name: ");
                    new_voter.setUsername(check_string());
                    break;
                case 5:
                    System.out.print("Enter new contact: ");
                    new_voter.setContact(check_string());
                    break;
                case 6:
                    System.out.print("Enter new citizen card number: ");
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
                rmi.switchUser(voter, new_voter);
                System.out.println("Sucess in updating voter information. ");
            }
            catch(ConnectException e){
                reconnect();
                rmi.switchUser(voter, new_voter);
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

    public void manage_table_members(){ //tenho que rever proteções!!!

        try{

            MulticastServer table;
            List<Voter> members;
            Voter voter = null;
            String id, cc_number;
            int option, aux;

            System.out.print("Insert table's id: ");
            id = check_string();
            table =  rmi.searchTable(id);

            while(table == null){
                System.out.print("Invalid id. Try again: ");
                id = check_string();
                table =  rmi.searchTable(id);
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
                    voter = rmi.searchVoterCc(cc_number);

                    while(voter == null && cc_number != "0"){
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
                    rmi.addVoterTable(table, voter);
                    System.out.println("Sucess adding member to table");
                }
                else{
                    rmi.removeVoterTable(table, voter);
                    System.out.println("Sucess removing member to table");
                }
            }
            catch(ConnectException e){
                reconnect();
                if(option == 1){
                    rmi.addVoterTable(table, voter);
                    System.out.println("Sucess adding member to table");
                }
                else{
                    rmi.removeVoterTable(table, voter);
                    System.out.println("Sucess removing member to table");
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

    
    public static void main(String args[]) {

        try{
            
            RMIServer_I rmi = (RMIServer_I) Naming.lookup("rmi://localhost:5001/RMIServer");
            AdminConsole admin = new AdminConsole(rmi);
            //AdminConsole_I admin_I = (AdminConsole_I) admin;

            try{
                //rmi.loginAdmin(admin_I);
            }
            catch(Exception e ){
                //argument type mismatch
                System.out.println("Ups loginAdim deu merda :) " + e);
            }

            admin.menu();
            admin.myObj.close();

        }
        catch (Exception e){
            System.out.println("Main: Exception in RMIServer.java(main) " + e);
        }

    }

}
