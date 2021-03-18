
import java.io.Serializable;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;




enum State {
      WAITING, OPEN, CLOSED
}
public class Election implements Serializable {

      

      /**
       *
       */
      
      private static final long serialVersionUID = 1L; 
      private Calendar beggDate;
      private Calendar endDate;
      private String title;
      private List<Type> allowedVoters = new CopyOnWriteArrayList<>();
      private String department;
      private List<Candidates> candidatesList = new CopyOnWriteArrayList<>();
      private List<AlreadyVoted> usersVoted = new CopyOnWriteArrayList<>();
      private int whiteVote;
      private int nullVote;
      private State state;

      
       

      public Election(String title,Calendar beggDate,Calendar endDate,String department, List<Type> allowedVoters ){
            this.beggDate = beggDate;
            this.endDate = endDate;
            this.title =title;
            this.department = department;
            this.allowedVoters = allowedVoters;
            whiteVote = 0;
            nullVote = 0;
            this.state = State.WAITING;
            new Thread(new Runnable(){
                  @Override
                  public void run(){
                        
                        while ( beggDate.getTimeInMillis() > Calendar.getInstance().getTimeInMillis()) {
                              try {
                                    Thread.sleep(1000);
                              } catch (InterruptedException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                    Thread.currentThread().interrupt();
                              }
                              
                        }
                        
                        setState(State.OPEN);
                        if(!candidatesList.isEmpty()){
                              while (Calendar.getInstance().getTimeInMillis() > endDate.getTimeInMillis()) {
                                    try {
                                          Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                          e.printStackTrace();
                                          Thread.currentThread().interrupt();
                                    }
                              }
                        }
                        setState(State.CLOSED);
                        System.out.println("Acabou");

                  }
            },"Something").start();
            
      }


      public State getState() {
            return this.state;
      }

      public void setState(State state) {
            this.state = state;
      }

      public Calendar getBeggDate() {
            return this.beggDate;
      }

      public void setBeggDate(Calendar beggDate) {
            this.beggDate = beggDate;
      }

      public Calendar getEndDate() {
            return this.endDate;
      }

      public void setEndDate(Calendar endDate) {
            this.endDate = endDate;
      }

      public String getTitle() {
            return this.title;
      }

      public void setTitle(String title) {
            this.title = title;
      }

      public List<Type> getAllowedVoters() {
            return this.allowedVoters;
      }

      public void setAllowedVoters(List<Type> allowedVoters) {
            this.allowedVoters = allowedVoters;
      }

      public String getDepartment() {
            return this.department;
      }

      public void setDepartment(String department) {
            this.department = department;
      }


      public boolean addCandidateList(Candidates candidate){
            if(searchCandidates(candidate.getName())== null){
                  this.candidatesList.add(candidate);
                  return true;
            }else{
                  return false;
            }
      }

      public boolean removeCandidateList(String candidateName){
            Candidates candidates = searchCandidates(candidateName);
            if(candidates != null){
                  candidatesList.remove(candidates);
                  return true;
            }
            return false;
      }

      public void removeCandidateList(Candidates candidate){
            this.candidatesList.remove(candidate);
      }

      public List<Candidates> getCandidatesList() {
            return this.candidatesList;
      }

      public void setCandidatesList(List<Candidates> candidatesList) {
            this.candidatesList = candidatesList;
      }

      public List<AlreadyVoted> getUsersVoted() {
            return this.usersVoted;
      }

      public void setUsersVoted(List<AlreadyVoted> usersVoted) {
            this.usersVoted = usersVoted;
      }

      public Boolean addUsersVoted(AlreadyVoted voter){
            if(this.usersVoted.contains(voter)){
                  return false;
            }else{
                  this.usersVoted.add(voter);
                  return true;
            }
      }

      public void results() {
            System.out.println("Number of Votes");
            for (Candidates type : candidatesList) {
                  System.out.println(type.getName() +" - "+ type.getNumberOfVotes());
            }
            
      }

      public Candidates searchCandidates(String name){
            for (Candidates candidates : candidatesList) {
                  if(candidates.getName().equals(name)){
                        return candidates;
                  }
            }
            return null;

      }

      public boolean vote(Voter vote,String name, String voteLocal){
            /**
             * This functions is to simulate the act of voting in a particular list and add to pile of voters who already voted
             * 
             * @return if the vote was successful or not
             */
            if(this.getDepartment().equals(vote.getDepartment()) && this.getAllowedVoters().contains(vote.getType())){
                  Calendar timeOfVote = Calendar.getInstance();
                  Candidates candidates = searchCandidates(name);
                  AlreadyVoted voter = new AlreadyVoted(vote, timeOfVote, voteLocal);
                  Boolean isNotIn = addUsersVoted(voter);
                  if(Boolean.TRUE.equals(isNotIn)){
                        if(name.isEmpty()){
                              whiteVote++;
                        }else if(candidates == null){
                              nullVote++;     
                        }else{
                              candidates.addVote();
                        }
                        return true;
                  }
            }
            return false;
            

            
      }

      public int getWhiteVote() {
            return this.whiteVote;
      }

      public void setWhiteVote(int whiteVote) {
            this.whiteVote = whiteVote;
      }

      public int getNullVote() {
            return this.nullVote;
      }

      public void setNullVote(int nullVote) {
            this.nullVote = nullVote;
      }     

      public static void main(String[] args) {
            System.out.println("Created");
      }
}