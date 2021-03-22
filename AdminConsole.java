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
        System.out.println("2. Create elections");                         //done
        System.out.println("3. Manage candidate lists");             //quase done
        System.out.println("4. Manage polling stations");                    //nop
        System.out.println("5. Change an election's properties");    //quase done
        System.out.println("6. Local voted each voter");               //nop
        System.out.println("7. Show polling station status");       //nop
        System.out.println("8. Show voters in real time");        //nop
        System.out.println("9. Fechar election- sitio errado ??");       //aqui?
        System.out.println("10. See detailed results of past elections"); //done?
        System.out.println("11. Early vote");                      //nop
        System.out.println("12. Change personal data");               //done
        System.out.println("13. Manage members of each polling station");   //nop

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

        do{
            if(h >= 0 && h < 24){
                return h;
            }
            else{
                System.out.println("Invalid hour. Try again");
            }
        }while(true);

    }

    private int check_minutes(){

        int m = check_number();

        do{
            if(m >= 0 && m < 60){
                return m;
            }
            else{
                System.out.println("Invalid minutes. Try again");
            }
        
        }while(true);

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

    public void register_voter(){

        String name, department, contact, address, cc_number, password;
        Type role;
        Calendar cc_expiring;
        int day, month, year;

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
        cc_expiring = Calendar.getInstance();
        
        System.out.print("Day: ");
        day = check_number();
        System.out.print("Month: ");
        month = check_number();
        System.out.print("Year: ");
        year = check_number();
        cc_expiring.set(year, month, day);

        System.out.print("Enter password: ");
        password = check_string();

        try{
            rmi.createVoter(name, department, contact, address, cc_number, cc_expiring, password, role); 
            System.out.println("\nSuccessfully created new voter");
        } 
        catch(Exception e){
            System.out.println("Error creating new voter: " + e);
        }
    }

    public void create_election(){

        Calendar dateB = Calendar.getInstance(), dateE = Calendar.getInstance();
        List<Type> electionType = new CopyOnWriteArrayList<>();
        String electionName, department = null;
        int day, month, year;
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

        System.out.print("Insert begin date:\nDay:");
        day = check_number();
        System.out.print("Month:");
        month = check_number();
        System.out.print("Year:");
        year = check_number();
        dateB.set(year, month, day);

        System.out.print("Hour:");
        dateB.set(Calendar.HOUR_OF_DAY, check_hour());
        System.out.print("Minute:");
        dateB.set(Calendar.MINUTE, check_minutes());

        System.out.print("Insert end date:\nDay:");
        day = check_number();
        System.out.print("Month:");
        month = check_number();
        System.out.print("Year:");
        year = check_number();
        dateE.set(year, month, day);

        System.out.print("Hour:");
        dateE.set(Calendar.HOUR_OF_DAY, check_hour());
        System.out.print("Minute:");
        dateE.set(Calendar.MINUTE, check_minutes());

        try{
            rmi.createElection(electionName, dateB, dateE, department, electionType);
            System.out.println("\nSuccessfully created new election");
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

        for(int i = 0; i < size; i ++){
            System.out.println(i + ". " + c.get(i));
        }

        return size;
    }

    public void manage_list(){ //falta 2 casos aqui: 3 e 4 - adicionar/remover membros

        List<Election> elections;
        List<Candidates> cand;
        List<Type> allowed;
        Election election;
        int option, size;
        String nameList; 
        Type typeList;

        try{

            elections = rmi.getElections();

            System.out.println("Pick a election:");
            for(int i =0; i < elections.size() ; i++){
                System.out.println(i + ". " + elections.get(i));
            }

            option = check_number();

            if(option >= 0 && option < elections.size()){

                election = elections.get(option);

                System.out.println ("1. Create a new list");
                System.out.println ("2. Delete a existing list"); 
                System.out.println ("3. Insert a candidate in a list");   
                System.out.println ("4. Delete a candidate in a list");         
                
                option = check_number();

                switch(option){

                    case 1:
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

                        rmi.createCandidate(null, nameList, election.getTitle(), typeList);
                        break;

                    case 2:
                        cand = election.getCandidatesList();
                        size = printListInElection(cand);
                        option = check_number();
                        if(option >= 0 && option < size){
                            rmi.removeCandidate(election.getTitle(), cand.get(option).getName());
                        }
                        else{
                            System.out.println ("Invalid option. Try again");
                        }
                        break;

                    case 3:
                        cand = election.getCandidatesList();
                        size = printListInElection(cand);
                        option = check_number();
                        if(option >= 0 && option < size){
                            //inserir membro na lista
                        }
                        else{
                            System.out.println ("Invalid option. Try again");
                        }
                        break;

                    case 4:
                        cand = election.getCandidatesList();
                        size = printListInElection(cand);
                        option = check_number();
                        if(option >= 0 && option < size){
                            //eliminar membro na lista
                        }
                        else{
                            System.out.println ("Invalid option. Try again");
                        }
                        break;
                    default:
                        System.out.println ("Invalid option. Try again");
                        break;
                }

            }
            else{
                System.out.println ("Invalid option. Try again");
            }

        } catch (Exception e){
            System.out.println("Exception in RMIServer.java(main) " + e);
        }
    }

    public void manage_tables(){}

    public void change_election(){

        try{

            Calendar date = Calendar.getInstance();
            List<Election> elections;
            Election election = null;
            String aux;
            int option, day, month, year;

            elections = rmi.getElections();

            System.out.println("Pick a election:");
            for(int i =0; i < elections.size() ; i++){
                System.out.println(i + ". " + elections.get(i));
            }

            option = check_number();

            if(option >= 0 && option < elections.size()){

                election = elections.get(option);

                System.out.println("Change "+ election.getTitle() +" properties\n1.Change title\n2.Change description\n3.Beginning date\n4.End date");

                option = check_number();
                
                if(option == 1){
                    System.out.println("Insert title: ");
                    aux = check_string();
                    election.setTitle(aux);
                }
                else if(option == 2){
                    System.out.println("Insert description: ");
                    //adicionar descrição na election
                }
                else if(option == 3 || option == 4){
                    System.out.print("Insert new date:\nDay:");
                    day = check_number();
                    System.out.print("Month:");
                    month = check_number();
                    System.out.print("Year:");
                    year = check_number();
                    date.set(year, month, day);
                        
                    System.out.print("Hour:");
                    date.set(Calendar.HOUR_OF_DAY, check_hour());
                    System.out.print("Minute:");
                    date.set(Calendar.MINUTE, check_minutes());

                    if(option == 3){
                        //date B
                    }
                    else{ 
                        //date E
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
            catch(Exception e){
                System.out.println("Error updating election information: " + e);
            }

        } catch (Exception e){
            System.out.println("Exception in RMIServer.java(main) " + e);
        }


    }

    public void see_voters_local(){}

    public void table_state(){}

    public void voters_real_time(){}

    public void count_results(){}

    public void see_results(){

        try{

            List<Election> elections;
            Election election;
            int option;

            elections = rmi.getElections();

            System.out.println("Pick a election:");
            for(int i =0; i < elections.size() ; i++){
                System.out.println(i + ". " + elections.get(i));
            }

            option = check_number();

            if(option >= 0 && option < elections.size()){
                election = elections.get(option);
                System.out.println("Result " + election.getTitle() + ": ");
                System.out.println("White - " + election.getWhiteVote()+ "\nNull - " + election.getNullVote());
                election.results();
            }
            else{
                System.out.println("Invalid option! ");
            }

        }
        catch(Exception e){
            System.out.println("Error : ");
        }        
    }

    public void early_vote(){} 

    public void change_voter_data(Voter voter){

        Voter new_voter = voter;
        int option, day, month, year;
        Calendar aux = Calendar.getInstance();
        
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
                System.out.print("Day: ");
                day = check_number();

                System.out.print("Month: ");
                month = check_number();

                System.out.print("Year: ");
                year = check_number();

                aux.set(year, month, day);
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
            System.out.println("Sucess in updating voter information: ");
        }
        catch(Exception e){
            System.out.println("Error updating voter information: " + e);
        }
        
    }

    public void manage_table_members(){}

    public void general_council_elections(){}

    
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
            System.out.println("Exception in RMIServer.java(main) " + e);
        }

    }

}
