import java.io.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Protocol implements Serializable {
    public String message;
	public List<String> parsed;

	public Protocol(String message) {
		this.message = message;
		parsed = parse();
	}
	// ver que tipo de função é colocar em ordem
	public List<String> parse() {
		// fazer verificação das palavras chave -> ex: join, type, etc
		String[] tokens = message.split(";");
		List<String> list = new CopyOnWriteArrayList<String>();

		for (String string : tokens) {

			String[] token = string.split("\\|"); // VER

			try {
				list.add(token[1]);

			} catch (ArrayIndexOutOfBoundsException e) {
				return null;
			}
		}
		return list;
	}

	public String toString() {
		return message;
	}
}
