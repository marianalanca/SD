import java.io.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Protocol implements Serializable {
	public String type, id, department;
	public List<String> item_name = new CopyOnWriteArrayList<String>();
	public String username, password, logged, msg, candidate;
	public int item_count;

	
	/**
	 * @param id of the terminal to which the information must be sent
	 * @param username 
	 * @param password
	 * @return String containing the protocol with all the data received as param
	 */
	public String login(String id, String username, String password) {
		return "type|login;id|"+id+";username|"+username+";password|"+password;
	}

	
	/** 
	 * @param department
	 * @return String
	 */
	public String request(String department) {
		return "type|request;department|"+department;
	}

	
	/** 
	 * @param id of the terminal to which the information must be sent
	 * @param username
	 * @param candidate
	 * @return String
	 */
	public String vote(String id, String username, String candidate) {
		return "type|vote;id|"+id+"username|"+username+";candidate|"+candidate;
	}

	
	/** 
	 * @param id of the terminal to which the information must be sent
	 * @param logged
	 * @param msg
	 * @return String
	 */
	public String status(String id, String logged, String msg) {
		return "type|status;id|"+id+";logged|"+logged+";msg|"+msg;
	}

	
	/** 
	 * @param id of the terminal to which the information must be sent
	 * @param logged
	 * @return String
	 */
	public String status(String id, String logged) {
		return "type|status;id|"+id+";logged|"+logged;
	}

	
	/** 
	 * @param id of the terminal to which the information must be sent
	 * @return String
	 */
	public String response(String id) {
		return "type|response;id|"+id;
	}

	
	/** 
	 * @param id of the terminal to which the information must be sent
	 * @return String
	 */
	public String accepted(String id) {
		return "type|accepted;id|"+id;
	}

	
	/** 
	 * @param id of the terminal to which the information must be sent
	 * @param item_count
	 * @param item_name
	 * @return String
	 */
	public String item_list(String id, int item_count, List<String> item_name) {
		String result = "type|item_list;item_count|"+item_count;
		for (int i=0;i<item_name.size()-1;i++){
			result.concat(";item_"+i+"_name|"+item_name.get(i));
		}
		System.out.println(result);
		return result;
	}

	
	/** 
	 * @param message
	 * @return Protocol
	 */
	public Protocol parse(String message) {
		String[] tokens = message.split(";");

		for (String string : tokens) {

			String[] token = string.split("\\|");
			try {
				switch(token[0]) {
					case "type":
						type = token[1];
						break;
					case "id":
						id = token[1];
						break;
					case "username":
						if (type.equals("login") || type.equals("vote"))
							username = token[1];
						else {
							System.out.println("Wrong format");
							return null;
						}
						break;
					case "department":
						if (type.equals("request"))
							department = token[1];
						else {
							System.out.println("Wrong format");
							return null;
						}
						break;
					case "password":
						if (type.equals("login"))
							password = token[1];
						else {
							System.out.println("Wrong format");
							return null;
						}
						break;
					case "logged":
						if (type.equals("status"))
							logged = token[1];
						else {
							System.out.println("Wrong format");
							return null;
						}
						break;
					case "msg":
						if (type.equals("status"))
							msg = token[1];
						else {
							System.out.println("Wrong format");
							return null;
						}
						break;
					case "candidate":
						if (type.equals("vote"))
							candidate = token[1];
						else {
							System.out.println("Wrong format");
							return null;
						}
						break;
					case "item_count":
						try {item_count = Integer.parseInt(token[1]);}
						catch (NumberFormatException e) { System.out.println(e); }
						break;
					default:
						if (type.equals("item_list"))
							item_name.add(token[1]);
						else {
							System.out.println("Wrong format");
							return null;
						}
						break;
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				return null;
			}
		}
		return this;
	}
}
