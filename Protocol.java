import java.io.*;

public class Protocol implements Serializable {
    public String text;

	public Protocol(String text) {
		this.text = text;
	}

	public void change_text(String text) {
		this.text = text;
	}

	public String toString() {
		return text;
	}
}
