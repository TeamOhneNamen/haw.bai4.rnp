package haw.hamburg.TON;

import java.io.IOException;
import java.util.ArrayList;

public class BusinessThreadList extends ArrayList<BusinessThread> {
	
	private static final long serialVersionUID = 1L;


	// liefert den index der connection in der "BusinessThreadList"(BTL),
	// die noch frei ist | wenn keine dann -1
	public int getFree() {
		
		for (int i = 0; i < this.size(); i++) {
			if(!this.get(i).clientAlive) {
				return i;
			}
		}
		return -1;
		
	}
	
	// wieviele Connecrions hat der Server noch offen?
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


	// wartet auf alle "BusinessThreads"(BT) in der BTL bis diese beendet sind
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

	//sendet an alle Clineten, die mit dem Server Verbunden sind "OK SHUTDOWN"
	public void shutdownAll() throws IOException {
		for (int i = 0; i < this.size(); i++) {
			if (this.get(i).getClient() != null) {
				this.get(i).sendOkay("SHUTDOWN");
			}
		}
	}

	// gebe allen Clineten, die mit dem Server Verbunden sind einen SoTimeout
	public void setAllSoTimeout(int timeout) throws IOException {
		for (int i = 0; i < this.size(); i++) {
			if (this.get(i).getClient() != null) {
				this.get(i).getClient().setSoTimeout(timeout);
			}
		}
	}
	
	
}
