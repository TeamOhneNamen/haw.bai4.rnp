package haw.hamburg.TON;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class BusinessThreadList extends ArrayList<BusinessThread> {
	
	private static final long serialVersionUID = 1L;

	public int getFree() {
		
		for (int i = 0; i < this.size(); i++) {
			if(!this.get(i).clientAlive) {
				return i;
			}
		}
		return -1;
		
	}

	public int manyConnections() {
		int ammound = 0;
		for (int i = 0; i < this.size(); i++) {
			if(this.get(i).clientAlive) {
				ammound++;
			}
			System.out.println("Ist Client Nr. " + i + " frei? " + !this.get(i).isAlive());
		}
		return ammound;
		
	}
	
	public void sendAllShutdwn() throws IOException {
		for (int i = 0; i < this.size(); i++) {
			if (this.get(i)!=null) {
				PrintWriter out = new PrintWriter(new OutputStreamWriter(this.get(i).getClient().getOutputStream(), StandardCharsets.UTF_8), true);
				out.println("OK SHUTDOWN");
				
			}
		}
	}
	
}
