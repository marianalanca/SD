import java.io.*;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class Protocol implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	public String type, id, department;
	public List<String> item_name = new CopyOnWriteArrayList<String>();
	public String username, password, logged, msg, candidate, election;
	public int item_count;
	public Long msgId;
	public List<String> types = new CopyOnWriteArrayList<String>(){{
		add("accepted");
		add("ack");
		add("crashed");
		add("election");
		add("item_list");
		add("leave");
		add("login");
		add("request");
		add("response");
		add("status");
		add("turnoff");
		add("vote");
	}};

	/**
	 * @param msgId id that identifies the message; In case this one is replicated, it is discarted
	 * @param id of the terminal to which the information must be sent
	 * @param username of the user that wants to login
	 * @param password of the user that wants to login
	 * @return String containing the protocol with all the data received as param
	 */
	public String login(String id, String username, String password) {
		return "type|login;msgID|"+Math.abs(new Random(System.currentTimeMillis()).nextLong())+";id|"+id+";username|"+username+";password|"+password;
	}

	/** 
	 * @param msgId id that identifies the message; In case this one is replicated, it is discarted
	 * @param id of the terminal to which the information must be sent
	 * @param department of the table and terminal where the voter is voting
	 * @return String containing the protocol message with all the data received as param
	 */
	public String ack(String id, String department){
		return "type|ack;msgID|"+Math.abs(new Random(System.currentTimeMillis()).nextLong())+";id|"+id+";department|"+department;
	}

	/** 
	 * @param msgId id that identifies the message; In case this one is replicated, it is discarted
	 * @param department of the table and terminal where the voter is voting
	 * @return String containing the protocol with all the data received as param
	 */
	public String turnoff(String department) {
		return "type|turnoff;msgID|"+Math.abs(new Random(System.currentTimeMillis()).nextLong())+";department|"+department;
	}

	/** 
	 * @param msgId id that identifies the message; In case this one is replicated, it is discarted
	 * @param id of the terminal to which the information must be sent
	 * @param department of the table and terminal where the voter is voting
	 * @param election that the user chose
	 * @return String containing the protocol with all the data received as param
	 */
	public String election(String id, String department, String election) {
		return "type|election;msgID|"+Math.abs(new Random(System.currentTimeMillis()).nextLong())+";id|"+id+";department|"+department+";election|"+election;
	}

	/** 
	 * @param msgId id that identifies the message; In case this one is replicated, it is discarted
	 * @param id of the terminal to which the information must be sent
	 * @param department of the table and terminal where the voter is voting
	 * @return String containing the protocol with all the data received as param
	 */
	public String leave(String id, String department) {
		return "type|leave;msgID|"+Math.abs(new Random(System.currentTimeMillis()).nextLong())+";id|"+id+";department|"+department;
	}

	/**
	 * @param msgId id that identifies the message; In case this one is replicated, it is discarted
	 * @param department of the table and terminal where the voter is voting
	 * @return String containing the protocol with all the data received as param
	 */
	public String request(String department) {
		return "type|request;msgID|"+Math.abs(new Random(System.currentTimeMillis()).nextLong())+";department|"+department;
	}

	/**
	 * @param msgId id that identifies the message; In case this one is replicated, it is discarted
	 * @param id of the terminal to which the information must be sent
	 * @param department of the table and terminal where the voter is voting
	 * @param username of the user that wants to vote
	 * @param election that the user chose
	 * @param candidate in which the voter wants to vote
	 * @return String containing the protocol with all the data received as param
	 */
	public String vote(String id, String department, String username, String election, String candidate) {
		return "type|vote;msgID|"+Math.abs(new Random(System.currentTimeMillis()).nextLong())+";id|"+id+";department|"+department+";username|"+username+";election|"+election+";candidate|"+candidate;
	}

	/** 
	 * @param msgId id that identifies the message; In case this one is replicated, it is discarted
	 * @param id of the terminal to which the information must be sent
	 * @param department of the table and terminal where the voter is voting
	 * @param logged contains the value of the status (on/off)
	 * @param msg is some message that is to be sent
	 * @return String containing the protocol with all the data received as param
	 */
	public String status(String id, String department, String logged, String msg) {
		return "type|status;msgID|"+Math.abs(new Random(System.currentTimeMillis()).nextLong())+";id|"+id+";department|"+department+";logged|"+logged+";msg|"+msg;
	}

	/** 
	 * @param msgId id that identifies the message; In case this one is replicated, it is discarted
	 * @param id of the terminal to which the information must be sent
	 * @param department of the table and terminal where the voter is voting
	 * @param logged contains the value of the status (on/off)
	 * @return String containing the protocol with all the data received as param
	 */
	public String status(String id, String department, String logged) {
		return "type|status;msgID|"+Math.abs(new Random(System.currentTimeMillis()).nextLong())+";id|"+id+";department|"+department+";logged|"+logged;
	}

	/** 
	 * @param msgId id that identifies the message; In case this one is replicated, it is discarted
	 * @param department of the table and terminal where the voter is voting
	 * @param id of the terminal to which the information must be sent
	 * @return String containing the protocol with all the data received as param
	 */
	public String response(String department, String id) {
		return "type|response;msgID|"+Math.abs(new Random(System.currentTimeMillis()).nextLong())+";department|"+department+";id|"+id;
	}

	/** 
	 * @param msgId id that identifies the message; In case this one is replicated, it is discarted
	 * @param id of the terminal to which the information must be sent
	 * @return String containing the protocol with all the data received as param
	 */
	public String accepted(String id) {
		return "type|accepted;msgID|"+Math.abs(new Random(System.currentTimeMillis()).nextLong())+";id|"+id;
	}

	/** 
	 * @param msgId id that identifies the message; In case this one is replicated, it is discarted
	 * @param id of the terminal to which the information must be sent
	 * @param item_count contains the size of the list to be passed
	 * @param item_name contains the list to be passed in the protocol
	 * @return String containing the protocol with all the data received as param
	 */
	public String item_list(String id, int item_count, List<String> item_name) {
		String result = "type|item_list;msgID|"+Math.abs(new Random(System.currentTimeMillis()).nextLong())+";id|"+id+";item_count|"+item_count;
		for (int i=0;i<item_name.size();i++){
			result = result.concat(";item_"+i+"_name|"+item_name.get(i));
		}
		return result;
	}

	/** 
	 * @param msgId id that identifies the message; In case this one is replicated, it is discarted
	 * @param id of the terminal to which the information must be sent
	 * @param department of the table and terminal where the voter is voting
	 * @return String containing the protocol with all the data received as param
	 */
	public String crashed(String id, String department) {
		return "type|crashed;msgID|"+Math.abs(new Random(System.currentTimeMillis()).nextLong())+";id|"+id+";department|"+department;
	}

	/**
	 * @param message the information to be parsed
	 * @return Protocol itself
	 */
	public Protocol parse(String message) {
		String[] tokens = message.split(";");

		for (String string : tokens) {

			String[] token = string.split("\\|");
			try {
				switch(token[0]) {
					case "type":
						if (types.contains(token[1]))
							type = token[1];
						break;
					case "msgID":
						if (types.contains(type))
							msgId = Long.parseLong(token[1]);
						break;
					case "id":
						if (type!=null && (type.equals("login") || type.equals("election")  || type.equals("vote") || type.equals("status") || type.equals("response")  || type.equals("accepted") || type.equals("item_list") || type.equals("crashed") || type.equals("leave") || type.equals("ack")))
						id = token[1];
						else {
							System.out.println("Wrong format");
							return null;
						}
						break;
					case "username":
						if (type.equals("login") || type.equals("vote")){
							username = token[1];}
						else {
							System.out.println("Wrong format");
							return null;
						}
						break;
					case "department":
						if (type!=null && (type.equals("request") || type.equals("election")  || type.equals("vote") || type.equals("status") || type.equals("response")  || type.equals("crashed") || type.equals("leave") || type.equals("turnoff") || type.equals("ack")))
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
					case "election":
						if (type.equals("vote") || type.equals("election"))
							election = token[1];
						else {
							System.out.println("Wrong format");
							return null;
						}
						break;
					case "item_count":
						if (type.equals("item_list"))
							try {item_count = Integer.parseInt(token[1]);}
							catch (NumberFormatException e) { System.out.println(e); }
						else {
							System.out.println("Wrong format");
							return null;
						}
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
