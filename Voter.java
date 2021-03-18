import java.io.Serializable;
import java.util.Calendar;

public class Voter implements Serializable {

    public String username, department;
    public String contact, address, cc_number;
    public Calendar cc_expiring;
    private String password;
    private Type type;

    
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

    public void setUsername(String username){ this.username = username; }

    public void setDepartment(String department){ this.department = department; }

    public void setContact(String contact){ this.contact = contact; }

    public void setAddress(String address){ this.address = address; }

    public void setCc_number(String cc_number){ this.cc_number = cc_number; }

    public void setCc_expiring(Calendar cc_expiring){ this.cc_expiring = cc_expiring;}

    public void setPassword(String password){ this.password = password;}

    public String getPassword() {return password; }

    public String getUsername() {
        return this.username;
    }

    public String getDepartment(){ return this.department; }
    
    public String getCc_number() {
        return this.cc_number;
    }

    public Calendar getCc_expiring(){ return this.cc_expiring; }

    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        this.type = type;
    }

}
