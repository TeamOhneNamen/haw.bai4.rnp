package haw.hamburg.TON.proxy.clinetSide;

import java.util.ArrayList;

public class BusinessThreadList extends ArrayList<RoutineThreadClientSide> {
	
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
	
}
