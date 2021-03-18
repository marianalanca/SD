// Podem existir uma ou mais consolas de administração. 
//Estas consolas comunicam apenas através de Java RMI.

import java.rmi.*;
import java.util.List;
import java.util.Scanner;
import java.util.Calendar;


public class AdminConsole {   

    public void menu(){
        System.out.println("1. Registar pessoas");
        System.out.println("2. Criar eleições");
        System.out.println("3. Gerir listas de candidatos");
        System.out.println("4. Gerir mesas de voto");
        System.out.println("9. Alterar propriedades de uma eleição");
        System.out.println("10. Local votou cada eleitor");
        System.out.println("11. Mostrar estado das mesas de voto");
        System.out.println("12. Mostram eleitores em tempo real");
        System.out.println("13. -- ??");
        System.out.println("14. Consultar resultados detalhados de eleições passadas");
        System.out.println("15. Voto antecipado");
        System.out.println("16. Alterar dados pessoais");
        System.out.println("17. Gerir membros de cada mesa de voto");
        System.out.println("18. Eleições para conselho geral -- ??");

        options_menu();
    }

    public void options_menu(){
        Scanner myObj = new Scanner(System.in);
        int option = myObj.nextInt();

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
            default:
                System.out.println("Invalid option");
                break;
        }

        myObj.close();
    }

    private String check_string(){

        Scanner myObj = new Scanner(System.in);
        int flag = 0;
        String s;

        do{
            s = myObj.nextLine();

            if(s.contains(";") || s.contains("|")){
                System.out.println("Invalid character ; or |");
                flag = 1;
            }
            else{
                flag = 0;
            }

        }while(flag == 1);

        myObj.close();

        return s;
    }

    private String check_role(){

        Scanner myObj = new Scanner(System.in);
        int flag = 0;
        String s;

        do{
            s = myObj.nextLine();

            if(s.equalsIgnoreCase("STUDENT") || s.equalsIgnoreCase("DOCENTE") || s.equalsIgnoreCase("FUNCIONARIO")){
                System.out.println("Invalid role");
                flag = 1;
            }
            else{
                flag = 0;
            }

        }while(flag == 1);

        myObj.close();

        return s;
    }

    public void register_voter(){

        String name, role, department, contact, address, cc_number, password;
        Calendar cc_expiring;
        int day, month, year;

        try{
            RMIServer_I rmi = (RMIServer_I) Naming.lookup("RMIServer");

            System.out.println("Enter name: ");
            name = check_string();

            System.out.println("Enter role: ");
            role = check_role();

            System.out.println("Enter department: ");
            department = check_string();

            System.out.println("Enter contact: ");
            contact = check_string();

            System.out.println("Enter address: ");
            address = check_string();
            
            System.out.println("Enter cc number: ");
            cc_number = check_string();
            
            Scanner myObj = new Scanner(System.in);
            
            System.out.println("Enter cc expiring date: ");
            cc_expiring = Calendar.getInstance();
            
            System.out.println("Day: ");
            day = myObj.nextInt();
            System.out.println("Month: ");
            month = myObj.nextInt();
            System.out.println("Year: ");
            year = myObj.nextInt();
            cc_expiring.set(year, month, day);

            myObj.close();

            System.out.println("Enter password: ");
            password = check_string();

            //voter = new Voter(name, role, department, contact, address, cc_number, cc_expiring, password);
            rmi.createVoter(name, role, department, contact, address, cc_number, cc_expiring, password);

        } catch (Exception e){
            System.out.println("Exception in RMIServer.java(main) " + e);
        }
        

    }

    public void create_election(){

        try{

            RMIServer_I rmi = (RMIServer_I) Naming.lookup("RMIServer");
            Scanner myObj = new Scanner(System.in);

            myObj.close();

        } catch (Exception e){
            System.out.println("Exception in RMIServer.java(main) " + e);
        }

    }

    private int printListInElection(List<Candidates> c){

        int size = c.size();

        for(int i = 0; i < size; i ++){
            System.out.println(i + ". " + c.get(i));
        }

        return size;
    }

    public void manage_list(){

        //recolher lista de elecoes

        try{

            RMIServer_I rmi = (RMIServer_I) Naming.lookup("RMIServer");
            Scanner myObj = new Scanner(System.in);

            List<Election> elections;
            List<Candidates> cand;
            Election election;
            String nameList; 
            int option, size;

            elections = rmi.getElections();

            System.out.println("Pick a election:");
            for(int i =0; i < elections.size() ; i++){
                System.out.println(i + ". " + elections.get(i));
            }

            option = myObj.nextInt();

            if(option > 0 && option < elections.size()){

                election = elections.get(option-1);

                System.out.println ("1. Create a new list");
                System.out.println ("2. Delete a existing list");  //existente escreve-se assim?
                System.out.println ("3. Insert a candidate in a list");   
                System.out.println ("4. Delete a candidate in a list");         
                
                option = myObj.nextInt();

                switch(option){
                    case 1:
                        System.out.println ("Insert list's name: ");
                        nameList = check_string();
                        rmi.createCandidate(null, nameList, election.getTitle());
                        break;

                    case 2:
                        cand = election.getCandidatesList();
                        size = printListInElection(cand);
                        option = myObj.nextInt();
                        if(option > 0 && option < size){
                            rmi.removeCandidate(election.getTitle(), cand.get(option).getName());
                        }
                        else{
                            System.out.println ("Invalid option");
                        }
                        break;

                    case 3:
                        cand = election.getCandidatesList();
                        size = printListInElection(cand);
                        option = myObj.nextInt();
                        if(option > 0 && option < size){
                            //inserir membro na lista
                        }
                        else{
                            System.out.println ("Invalid option");
                        }
                        break;

                    case 4:
                        cand = election.getCandidatesList();
                        size = printListInElection(cand);
                        option = myObj.nextInt();
                        if(option > 0 && option < size){
                            //eliminar membro na lista
                        }
                        else{
                            System.out.println ("Invalid option");
                        }
                        break;
                    default:
                        System.out.println ("Invalid option");
                        break;
                }

            }
            else{
                System.out.println ("Invalid option!");
            }

            myObj.close();

        } catch (Exception e){
            System.out.println("Exception in RMIServer.java(main) " + e);
        }
    }

    public void manage_tables(){}

    public void change_election(){}

    public void see_voters_local(){}

    public void table_state(){}

    public void voters_real_time(){}

    public void count_results(){}

    public void see_results(){}

    public void early_vote(){} 

    public void change_voter_data(Voter voter){

        Scanner myObj = new Scanner(System.in);
        int option, day, month, year;
        Calendar aux = Calendar.getInstance();
        
        System.out.println("1. Change name");
        System.out.println("2. Change role");
        System.out.println("3. Change department");
        System.out.println("4. Change contact");
        System.out.println("5. Change address");
        System.out.println("6. Change citizen card number");
        System.out.println("7. Change citizen card expiring date");
        System.out.println("8. Password");

        option = myObj.nextInt();

        switch(option){
            case 1:
                System.out.println("Enter new name: ");
                voter.setUsername(check_string());
                break;
            case 2:
                System.out.println("Enter new role: ");
                voter.setRole(check_string());
                break;
            case 3:
                System.out.println("Enter new department: ");
                voter.setDepartment(check_string());
                break;
            case 4:
                System.out.println("Enter name: ");
                voter.setUsername(check_string());
                break;
            case 5:
                System.out.println("Enter new contact: ");
                voter.setContact(check_string());
                break;
            case 6:
                System.out.println("Enter new citizen card number: ");
                voter.setCc_number(check_string());
                break;
            case 7:
                System.out.println("Enter new date: ");
                System.out.println("Day: ");
                day = myObj.nextInt();
                System.out.println("Month: ");
                month = myObj.nextInt();
                System.out.println("Year: ");
                year = myObj.nextInt();
                aux.set(year, month, day);
                voter.setCc_expiring(aux);
                break;
            case 8:
                System.out.println("Enter new password: ");
                voter.setPassword(check_string());
                break;
            default:
                System.out.println("Invalid option");
                break;
        }

        myObj.close();
        
    }

    public void manage_table_members(){}

    public void general_council_elections(){}

}
