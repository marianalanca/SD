import java.io.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Protocol implements Serializable {
	public String type, id;
	public List<String> item_name;
	public String username, password, logged, msg;
	public int item_count;

	public String login(String id, String username, String password) {
		return "type|login;id|"+id+";username|"+username+";password|"+password;
	}
	public String status(String id, String logged, String msg) {
		return "type|status;id|"+id+";logged|"+logged+";msg|"+msg;
	}
	public String status(String id, String logged) {
		return "type|status;id|"+id+";logged|"+logged;
	}

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
						if (type.equals("login"))
							username = token[1];
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
					case "item_count":
						try {item_count = Integer.parseInt(token[1]);}
						catch (NumberFormatException e) { System.out.println(e); }
						break;
					//case "item_0_name"
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				return null;
			}
		}
		return this;
	}

}
