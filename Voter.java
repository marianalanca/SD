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
     * set the voter's department
     * @param department a string with the voter's department
     */
    public void setDepartment(String department){ this.department = department; }

    /**
     * set the voter's contact
     * @param contact a string with the voter's contact
     */
    public void setContact(String contact){ this.contact = contact; }

    /**
     * set the voter's address
     * @param address a string with the voter's address
     */
    public void setAddress(String address){ this.address = address; }

    /**
     * set the voter's Citizen Card number
     * @param cc_number a string with the voter's Citizen Card number
     */
    public void setCc_number(String cc_number){ this.cc_number = cc_number; }

    /**
     * set the voter's Citizen Card Expiring Date
     * @param cc_expiring the voter's Citizen Card Expiring Date
     */
    public void setCc_expiring(Calendar cc_expiring){ this.cc_expiring = cc_expiring;}

    /**
     * set the voter's password
     * @param password a string with set the voter's password
     */
    public void setPassword(String password){ this.password = password;}

    /**
     * get the voter's password
     * @return the voterr's password
     */
    public String getPassword() {return password; }

    /**
     * get the voter's username
     * @return the voter's username
     */
    public String getUsername() { return this.username; }

    /**
     * get the voter's department
     * @return the department of the voter
     */
    public String getDepartment(){ return this.department; }
    
    /**
     * get the voter's contact
     * @return the voter's contact
     */
    public String getContact(){ return this.contact; }

    /**
     * get the voter's address
     * @return a string with the voter's address
     */
    public String getAddress(){ return this.address; }

    /**
     * get the voter's cc_number
     * @return the cc_number
     */
    public String getCc_number() { return this.cc_number; }

    /**
     * get the voter's cc_expiring date
     * @return the expiration date of the CC
     */
    public Calendar getCc_expiring(){ return this.cc_expiring; }

    /**
     * get the voter's type
     * @return the Type
     */
    public Type getType() { return this.type; }

    /**
     * set the voter's type
     * @param type the type
     */
    public void setType(Type type) { this.type = type; }

}
