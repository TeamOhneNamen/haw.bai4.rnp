package haw.hamburg.TON;

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
		}
		return ammound;
		
	}
	
}
