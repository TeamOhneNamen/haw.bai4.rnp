package haw.hamburg.TON;

import java.io.IOException;
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


	
	public void joinAll() {
		for (int i = 0; i < this.size(); i++) {
			if (this.get(i).getClient() != null) {
				try {
					this.get(i).join();
					System.out.println("Thread " + i + " wurde beendet!");
				} catch (InterruptedException e) {
					this.get(i).interrupt();
					e.printStackTrace();
				}
			}
		}
	}

	public void shutdownAll() throws IOException {
		for (int i = 0; i < this.size(); i++) {
			if (this.get(i).getClient() != null) {

				System.out.println(i);
				
				this.get(i).sendOkay("SHUTDOWN");
			}
		}
	}
	
}
