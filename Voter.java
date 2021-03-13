public class Voter {
    public String username, role, department; // is it necessary?
    public String contact, address, cc_number, cc_expiring;
    private String password;

    public Voter (String username, String role, String department, String contact, String address, String cc_number, String cc_expiring, String password) {
        this.username = username;
        this.role = role;
        this. department = department;
        this.contact = contact;
        this.address = address;
        this.cc_number = cc_number;
        this.cc_expiring = cc_expiring;
        this.password = password;
    }

    /*public String getPassword() {
        return password;
    }*/
}
