
import java.io.Serializable;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;



enum Type {
      STUDENT, DOCENTE, FUNCIONARIO
}
public class Election implements Serializable {

      

      /**
       *
       */
      private static final long serialVersionUID = 1L; 
      private Calendar beggDate;
      private Calendar endDate;
      private String title;
      private List<Type> allowedVoters;
      private String department;
      private List<Candidates> candidatesList = new CopyOnWriteArrayList<>();
      private List<Voter> usersVoted;
      

      public Election(String title,Calendar beggDate,Calendar endDate,String department, List<Type> allowedVoters ){
            this.beggDate = beggDate;
            this.endDate = endDate;
            this.title =title;
            this.department = department;
            this.allowedVoters = allowedVoters;
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

      public void removeCandidateList(Candidates candidate){
            this.candidatesList.remove(candidate);
      }

      public List<Candidates> getCandidatesList() {
            return this.candidatesList;
      }

      public void setCandidatesList(List<Candidates> candidatesList) {
            this.candidatesList = candidatesList;
      }

      public List<Voter> getUsersVoted() {
            return this.usersVoted;
      }

      public void setUsersVoted(List<Voter> usersVoted) {
            this.usersVoted = usersVoted;
      }

      public Boolean addUsersVoted(Voter voter){
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

      public boolean vote(Voter voter,String name){
            /**
             * This functions is to simulate the act of voting in a particular list and add to pile of voters who already voted
             * 
             * @return if the vote was successful or not
             */
            Candidates candidates = searchCandidates(name);
            if(candidates == null){
                  return false;
            }
            Boolean isNotIn = addUsersVoted(voter);
            if(isNotIn){
                  candidates.addVote();
                  return true;
            }else{
                  return false;
            }

            
      }

      public static void main(String[] args) {
            System.out.println("Created");
      }
}