import java.io.Serializable;
import java.util.Calendar;

public class Voter implements Serializable {
    /**
     * Represents the Voters/Users
     * @param username - the username of the voter
     * @param department - the department the voter that users participates
     * @param address - the address of the voter
     * @param cc_number - the user cc_number
     * @param cc_expiring - the date of expiration
     * @param password - the voters password
     */
    public String username, department;
    public String contact, address, cc_number;
    public Calendar cc_expiring;
    private String password;
    private Type type;

    /**
     * Construtor Voter
     * @param username
     * @param department
     * @param contact
     * @param address
     * @param cc_number
     * @param cc_expiring
     * @param password
     * @param type
     */
    public Voter (String username, String department, String contact, String address, String cc_number, Calendar cc_expiring, String password, Type type) {
        this.username = username;
        this.department = department;
        this.contact = contact;
        this.address = address;
        this.cc_number = cc_number;
        this.cc_expiring = cc_expiring;
        this.password = password;
        this.type = type;
    }

    /**
     * set the voter's username
     * @param username a string with the voter's username
     */
    public void setUsername(String username){ this.username = username; }

    /**
     * 
     * @param department
     */
    public void setDepartment(String department){ this.department = department; }

    /**
     * 
     * @param contact
     */
    public void setContact(String contact){ this.contact = contact; }

    /**
     * 
     * @param address
     */
    public void setAddress(String address){ this.address = address; }

    /**
     * 
     * @param cc_number
     */
    public void setCc_number(String cc_number){ this.cc_number = cc_number; }

    /**
     * 
     * @param cc_expiring
     */
    public void setCc_expiring(Calendar cc_expiring){ this.cc_expiring = cc_expiring;}

    /**
     * 
     * @param password
     */
    public void setPassword(String password){ this.password = password;}

    /**
     * 
     * @return the username password
     */
    public String getPassword() {return password; }

    /**
     * 
     * @return the voters username
     */
    public String getUsername() { return this.username; }

    /**
     * 
     * @return the department of the voter
     */
    public String getDepartment(){ return this.department; }
    
    /**
     * 
     * @return the voter's contact
     */
    public String getContact(){ return this.contact; }

    /**
     * 
     * @return a string with the voter's address
     */
    public String getAddress(){ return this.address; }

    /**
     * 
     * @return the Cc_number
     */
    public String getCc_number() { return this.cc_number; }

    /**
     * 
     * @return the expiration date of the CC
     */
    public Calendar getCc_expiring(){ return this.cc_expiring; }

    /**
     * 
     * @return the Type
     */
    public Type getType() { return this.type; }

    /**
     * 
     * @param type
     */
    public void setType(Type type) { this.type = type; }

}
