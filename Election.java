import java.util.List;
import java.util.HashMap;
import java.util.Calendar;
import java.io.Serializable;
import java.util.concurrent.CopyOnWriteArrayList;


public class Election implements Serializable {

      private static final long serialVersionUID = -5360950595778030024L;
      private Calendar beggDate;
      private Calendar endDate;
      private String title;
      private String department;
      private String description;
      private List<Type> allowedVoters = new CopyOnWriteArrayList<>();
      private List<Candidates> candidatesList = new CopyOnWriteArrayList<>();
      private List<AlreadyVoted> usersVoted = new CopyOnWriteArrayList<>();
      private List<MulticastServer> tables = new CopyOnWriteArrayList<>();
      private HashMap<String, Integer> voterPerTable = new HashMap<String, Integer>();
      private int whiteVote;
      private int nullVote;
      private State state;
       

      /**
       * Constructor Election
       * @param title a string with the election's title 
       * @param description a string with the election's description
       * @param beggDate the election's begging date
       * @param endDate the election's ending date
       * @param department a string with the department where the election takes place. If general council election, department must be a empty string
       * @param allowedVoters a list with the types allowed in the election
       */
      public Election(String title, String description, Calendar beggDate,Calendar endDate,String department, List<Type> allowedVoters ){
            this.beggDate = beggDate;
            this.endDate = endDate;
            this.title =title;
            this.description = description;
            this.department = department;
            this.allowedVoters = allowedVoters;
            whiteVote = 0;
            nullVote = 0;
            this.state = State.WAITING;
            
            runThread();
      }
      /**
       * It creates a thread that will take care of the State of the function
       */
      public void runThread(){
            new Thread((Runnable) () -> {
                  try {
                        
                  
                  while ( beggDate.getTimeInMillis() > Calendar.getInstance().getTimeInMillis()) {
                        try {
                              Thread.sleep(1000);
                        } catch (InterruptedException e1) {
                              // TODO Auto-generated catch block
                              e1.printStackTrace();
                              Thread.currentThread().interrupt();
                        }
                        
                  }
                  
                  setState(State.OPEN);
                  if(candidatesList.size() > 0 ){
                        while (Calendar.getInstance().getTimeInMillis() < endDate.getTimeInMillis()) {
                              try {
                                    Thread.sleep(1000);
                              } catch (InterruptedException e2) {
                                    e2.printStackTrace();
                                    Thread.currentThread().interrupt();
                              }
                        }
                  }
                  setState(State.CLOSED);
                  
                  } catch (Exception e3) {
                        setState(State.CLOSED);
                        Thread.currentThread().interrupt();
                  }
            },"Something").start();
      }

      /**
       * adds votes to a table in the election
       * @param nameTable a string with the name of the table
       */
      public void addHash(String nameTable){
            int votes;

            if(nameTable.equals("Administration console")){
                  return;
            }

            for (String i : voterPerTable.keySet()) {
                  if(i.equals(nameTable)){
                        votes = voterPerTable.get(i) + 1;
                        voterPerTable.put(nameTable, votes);
                        return;
                  }
            }

            voterPerTable.put(nameTable, 0);

      }

      /**
       * adds a voter to the list a voters
       * @param voter a voter
       * @return true in sucess, false otherwise
       */
      public boolean voterInAlreadyVoter(Voter voter) {
            for (AlreadyVoted voted : usersVoted) {
                  if(voted.getVote().getCc_number().equals(voter.getCc_number())){
                        return true;
                  }
            }
            return false;
      }

      /**
       * get all the tables associated with the election
       * @return a list of multicastServer
       */
      public List<MulticastServer> getTables() { return tables; }

      /**
       * set tables associated with the election
       * @param tables a list of multicastServer
       */
      public void setTables(List<MulticastServer> tables) { this.tables = tables; }

      /**
       * get state os the election
       * @return state
       */
      public State getState() { return this.state; }

      /**
       * set election's state
       * @param state the new state of the Election
       */
      public void setState(State state) { this.state = state; }

      /**
       * set election's description
       * @param description a string with the election's description
       */
      public void setDescription(String description) { this.description = description; }      

      /**
       * get election's description
       * @return description a string with the election's description
       */
      public String getDescription() { return this.description; }  
      
      /**
       * get election's begging date
       * @return election's begging date
       */
      public Calendar getBeggDate() { return this.beggDate; }

      /**
       * set election's begging date
       * @param beggDate election's begging date
       */
      public void setBeggDate(Calendar beggDate) { this.beggDate = beggDate; }

      /**
       * get election's ending date
       * @return election's ending date
       */
      public Calendar getEndDate() { return this.endDate; }

      /**
       * set election's ending date
       * @param endDate election's ending date
       */
      public void setEndDate(Calendar endDate) { this.endDate = endDate; }

      /**
       * get election's title
       * @return a string with election's title
       */
      public String getTitle() { return this.title; }

      /**
       * set election's title
       * @param title a string with election's title
       */
      public void setTitle(String title) { this.title = title; }

      /**
       * get a list with the Type of all allowed voters
       * @return a list with the Type of all allowed voters
       */
      public List<Type> getAllowedVoters() { return this.allowedVoters; }

      /**
       * add a member to a candidate list
       * @param nome name of a candidate list
       * @param member voter to inserte in the candidate list
       * @return true if success, false otherwise
       */
      public boolean addMemberToLista(String nome, Voter member){

            for (Candidates candidates : candidatesList) {
                  if(candidates.getName().equals(nome)){
                        int index = candidatesList.indexOf(candidates);
                        candidatesList.get(index).addCandidateList(member);
                        return true;
                  }
            }
            return false;
      }

      /**
       * remove a member to a candidate list
       * @param nome name of a candidate list
       * @param member voter to remove in the candidate list
       * @return true if success, false otherwise
       */
      public boolean removeMemberToLista(String nome, Voter member){
            for (Candidates candidates : candidatesList) {
                  if(candidates.getName().equals(nome)){
                        int index = candidatesList.indexOf(candidates);
                        candidatesList.get(index).removeCandidateList(member);
                        return true;
                  }
            }
            return false;
      }

      /**
       * set allowed voters
       * @param allowedVoters list with the Type of allowed voters
       */
      public void setAllowedVoters(List<Type> allowedVoters) { this.allowedVoters = allowedVoters; }

      /**
       * get election's department
       * @return a string with the election's department
       */
      public String getDepartment() { return this.department; }

      /**
       * set election's department
       * @param department a string with the election's department
       */
      public void setDepartment(String department) { this.department = department; }

      /**
       * add a table to the election
       * @param server musticast server
       * @return true if success, false otherwise
       */
      public boolean addTable(MulticastServer server){
            if(!tables.contains(server)){
                  tables.add(server);
                  addHash(server.getName());
                  return true;
            }
            return false;
      }

      /**
       * renove a table to the election
       * @param server musticast server
       * @return true if success, false otherwise
       */
      public boolean removeTable(MulticastServer server){
            if(tables.contains(server)){
                  tables.remove(server);
                  voterPerTable.remove(server.getName());
                  return true;
            }
            return false;

      }

      /**
       * add a new candidate list to the election
       * @param candidate candidate
       * @return true if success, false otherwise
       */
      public boolean addCandidateList(Candidates candidate){
            if(searchCandidates(candidate.getName())== null){
                  this.candidatesList.add(candidate);
                  return true;
            }
            return false;
      }

      /**
       * remove a candidate list to the election by candidate name
       * @param candidateName a string with the candidate name
       * @return true if success, false otherwise
       */
      public boolean removeCandidateList(String candidateName){
            for(Candidates c: candidatesList){
                  if(c.getName().equals(candidateName)){
                        candidatesList.remove(c);
                        return true;
                  }
            }
            return false;
      }

      /**
       * remove a candidate list to the election
       * @param candidate candidate
       */
      public void removeCandidateList(Candidates candidate){ this.candidatesList.remove(candidate); }

      /**
       * get list of election's candidate list
       * @return list of election's candidate list
       */
      public List<Candidates> getCandidatesList() { return this.candidatesList; }

      /**
       * set list of election's candidate list
       * @param candidatesList list of election's candidate list
       */
      public void setCandidatesList(List<Candidates> candidatesList) { this.candidatesList = candidatesList; }

      /**
       * get list the voter that already voted
       * @return list the voter that already voted
       */
      public List<AlreadyVoted> getUsersVoted() { return this.usersVoted; }

      /**
       * set list the voter that already voted
       * @param usersVoted list the voter that already voted
       */
      public void setUsersVoted(List<AlreadyVoted> usersVoted) { this.usersVoted = usersVoted; }

      /**
       * add a voter that already voted
       * @param voter voter
       * @return true if success, false otherwise
       */
      public Boolean addUsersVoted(AlreadyVoted voter){
            for (AlreadyVoted vote : usersVoted) {
                  if(vote.getVote().getCc_number().equals(voter.getVote().getCc_number())){
                        return false;
                  }
            }
            if(!this.usersVoted.contains(voter)){
                  this.usersVoted.add(voter);
                  return true;
            }
            return false;
      }

      /**
       * prints the election's candidate lists results
       */
      public void results() {
            System.out.println("Number of Votes");
            for (Candidates type : candidatesList) {
                  System.out.println(type.getName() +" - "+ type.getNumberOfVotes());
            }
            
      }

      /**
       * search a candidate list by name
       * @param name a string with the name of the candidate list
       * @return candidate is it exists, null otherwise
       */
      public Candidates searchCandidates(String name){
            for (Candidates candidates : candidatesList) {
                  if(candidates.getName().equals(name)){
                        return candidates;
                  }
            }
            return null;
      }

      /**
       * Simulate the act of voting in a particular list and add to pile of voters who already voted
       * @param vote the voter that voted
       * @param name a string with the name of the candidate list
       * @param voteLocal a string with the department where the voter voted
       * @return true if success, false otherwise
       */
      public boolean vote(Voter vote, String name, String voteLocal){

            if( this.getAllowedVoters().contains(vote.getType())){

                  Calendar timeOfVote = Calendar.getInstance();
                  AlreadyVoted voter = new AlreadyVoted(vote, timeOfVote, voteLocal);
                  Boolean isNotIn = addUsersVoted(voter);

                  if(Boolean.TRUE.equals(isNotIn) && name == null){
                        nullVote++;
                        addHash(voteLocal);
                        return true;  
                  }
                  else{
                        Candidates candidates = searchCandidates(name);
                        if(Boolean.TRUE.equals(isNotIn)){
                              if(name.isEmpty()){
                                    whiteVote++;
                                    addHash(voteLocal);
                                    return true;
                              }
                              else{
                                    candidates.addVote();
                                    addHash(voteLocal);
                                    //System.out.println(candidates.getNumberOfVotes());
                                    return true;
                              }
                        }
                  }
            }
            return false;
      }

      /**
       * get the number of white votes
       * @return a integer with the number of white votes
       */
      public int getWhiteVote() { return this.whiteVote; }

      /**
       * set the number of white vote
       * @param whiteVote a integer with the number of white votes
       */
      public void setWhiteVote(int whiteVote) { this.whiteVote = whiteVote; }

      /**
       * get a integer with the number of null votes
       * @return a integer with the number of null votes
       */
      public int getNullVote() { return this.nullVote; }

      /**
       * set a integer with the number of null votes
       * @param nullVote a integer with the number of null votes
       */
      public void setNullVote(int nullVote) { this.nullVote = nullVote; }

      /**
       * get the hasmap that contains the voters per table
       * @return a hashmap
       */
      public HashMap<String, Integer> getVotesPerTable(){ return voterPerTable; }

      public static void main(String[] args) {
            System.out.println("Created");
      }
}