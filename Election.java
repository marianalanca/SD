
import java.util.Calendar;
import java.util.List;



enum Type {
      STUDENT, DOCENTE, FUNCIONARIO
}
public class Election {

      

      private Calendar beggDate;
      private Calendar endDate;
      private String title;
      private List<Type> allowedVoters;
      private String department;
      private List<Integer> candidatesList;
      private List<Voter> usersVoted;
      

      public Election(String title,Calendar beggDate,Calendar endDate,List<Type> allowedVoters,String department){
            this.beggDate = beggDate;
            this.endDate = endDate;
            this.title =title;
            this.allowedVoters = allowedVoters;
            this.department = department;

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


      public void addCandidateList(Integer candidate){
            this.candidatesList.add(candidate);
      }

      public void removeCandidateList(Integer candidate){
            this.candidatesList.remove(candidate);
      }

      public List<Integer> getCandidatesList() {
            return this.candidatesList;
      }

      public void setCandidatesList(List<Integer> candidatesList) {
            this.candidatesList = candidatesList;
      }

      public List<Voter> getUsersVoted() {
            return this.usersVoted;
      }

      public void setUsersVoted(List<Voter> usersVoted) {
            this.usersVoted = usersVoted;
      }

      public void results() {
            for (Integer type : candidatesList) {
                  System.out.println(type);
            }
            
      }

      public static void main(String[] args) {
            System.out.println("Created");
      }
}