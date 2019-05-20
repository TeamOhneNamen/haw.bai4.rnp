package haw.hamburg.TON.proxy.clinetSide;

import java.util.ArrayList;

/**
 * 
 * @author Ferdinand Trendelenburg AND Thorben Schomacker
 *
 * List to Manage The List of BusinessThreads in The System (Threads handle POP3-Server-sided Connections) 
 *
 */
public class BusinessThreadList extends ArrayList<RoutineThreadClientSide> {
	
	private static final long serialVersionUID = 1L;


	/**
	 * get the index of free "BusinessThreadList"(BTL) Slots 
	 * @return position in BTL | -1 if no free Slot 
	 */
	public int getFree() {
		
		for (int i = 0; i < this.size(); i++) {
			if(!this.get(i).clientAlive) {
				return i;
			}
		}
		return -1;
		
	}
	
	/**
	 * how many connections are free to connect;
	 * @return ammound
	 */
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
	
}
