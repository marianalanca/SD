import java.io.Serializable;
import java.util.Calendar;



public class AlreadyVoted implements Serializable {
      /**
       * @param voter - person who already voted
       * @param timeOfVot - the time of vote of the person
       * @param location - the location of the vote
       */
      Voter vote;
      Calendar timeOfVote;
      String local;


      public AlreadyVoted(Voter vote, Calendar timeOfVote, String local){
            this.vote = vote;
            this.timeOfVote = timeOfVote;
            this.local = local;
      }


      /**
       * 
       * @return returns the vote
       */
      public Voter getVote() {
            return this.vote;
      }
      /**
       * @param vote
       */
      public void setVote(Voter vote) {
            this.vote = vote;
      }

      /**
       * 
       * @return the timeofthe vote
       */
      public Calendar getTimeOfVote() {
            return this.timeOfVote;
      }

      /**
       * Changes the time of the vote
       * @param timeOfVote
       */
      public void setTimeOfVote(Calendar timeOfVote) {
            this.timeOfVote = timeOfVote;
      }

      /**
       * 
       * @return the local of the voter vote
       */
      public String getLocal() {
            return this.local;
      }

      /**
       * 
       * @param local sets a new local
       */
      public void setLocal(String local) {
            this.local = local;
      }


      
}
