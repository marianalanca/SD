public class TerminalVoter {
    private String ID;
    private Voter data;

    public TerminalVoter(String ID, Voter data){
        this.ID = ID;
        this.data = data;
    }


    /** 
     * @return String containing the ID of the voter
     */
    public String getID() {
        return ID;
    }
    
    /** 
     * @return Voter Object containing all the voter data
     */
    public Voter getData() {
        return data;
    }
}
