import java.io.Serializable;
import java.util.Calendar;

public class AlreadyVoted implements Serializable {

      Voter vote;
      Calendar timeOfVote;
      String local;


      public AlreadyVoted(Voter vote, Calendar timeOfVote, String local){
            this.vote = vote;
            this.timeOfVote = timeOfVote;
            this.local = local;
      }



      public Voter getVote() {
            return this.vote;
      }

      public void setVote(Voter vote) {
            this.vote = vote;
      }

      public Calendar getTimeOfVote() {
            return this.timeOfVote;
      }

      public void setTimeOfVote(Calendar timeOfVote) {
            this.timeOfVote = timeOfVote;
      }

      public String getLocal() {
            return this.local;
      }

      public void setLocal(String local) {
            this.local = local;
      }


      
}
