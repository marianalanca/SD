import java.io.Serializable;
import java.util.List;

public class Candidates implements Serializable {
      
      /**
       *
       */
      private static final long serialVersionUID = 8752409987302828808L;
      private List<Voter> members;
      private int numberOfVotes;
      private String name;


      public Candidates(List<Voter> members, String name){
            this.members = members;
            this.numberOfVotes = 0;
            this.name = name;
      }
      public void addVote(){
            this.numberOfVotes++;
      }

      public void addCandidateList(Voter candidate){
            members.add(candidate);
      }

      public List<Voter> getMembers() {
            return this.members;
      }

      public void setMembers(List<Voter> members) {
            this.members = members;
      }

      public int getNumberOfVotes() {
            return this.numberOfVotes;
      }

      public void setNumberOfVotes(int numberOfVotes) {
            this.numberOfVotes = numberOfVotes;
      }

      public String getName() {
            return this.name;
      }

      public void setName(String name) {
            this.name = name;
      }

}
