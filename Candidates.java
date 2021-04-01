import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Candidates implements Serializable {
      /**
       * @param members - list of members that are candidate
       * @param numberOfVotes - number of votes in the candidate
       * @param name - name of the candidate
       * @param type - type of candidate
       */
      private static final long serialVersionUID = 8752409987302828808L;
      private List<Voter> members = new CopyOnWriteArrayList<>();
      private int numberOfVotes;
      private String name;
      private Type type;

      /**
       * 
       * @param name
       * @param type
       */
      public Candidates(String name, Type type){
            this.numberOfVotes = 0;
            this.name = name;
            this.type = type;
      }

      /**
       * Adds 1 vote to the candidate
       */
      public void addVote(){
            this.numberOfVotes++;
      }

      /**
       * 
       * @param candidate
       * Adds a candidate
       */
      public void addCandidateList(Voter candidate){
            members.add(candidate);
      }

      /**
       * 
       * @param candidate
       * removes a candidate
       */
      public void removeCandidateList(Voter candidate){
            members.remove(candidate);
      }

      /**
       * 
       * @return the list of members in the candidate
       */
      public List<Voter> getMembers() {
            return this.members;
      }

      /**
       * 
       * @param members
       */
      public void setMembers(List<Voter> members) {
            this.members = members;
      }
      /**
       * 
       * @return the number of votes
       */
      public int getNumberOfVotes() {
            return this.numberOfVotes;
      }

      /**
       * 
       * @param numberOfVotes
       */
      public void setNumberOfVotes(int numberOfVotes) {
            this.numberOfVotes = numberOfVotes;
      }

      /**
       * 
       * @return the name of the candidate
       */
      public String getName() {
            return this.name;
      }


      /**
       * 
       * @param name
       */
      public void setName(String name) {
            this.name = name;
      }

      /**
       * 
       * @return the type of candidate
       */
      public Type getType() {
            return this.type;
      }

      /**
       * 
       * @param type
       */
      public void setType(Type type) {
            this.type = type;
      }

}
