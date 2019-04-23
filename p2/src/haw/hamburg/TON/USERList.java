package haw.hamburg.TON;

import java.net.Socket;
import java.util.ArrayList;

import haw.hamburg.TON.Exceptions.WrongUsernameException;

public class USERList extends ArrayList<USER>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public USER getUserbyName(String username) throws WrongUsernameException {
		int index;
		for (int i = 0; i < this.size(); i++) {
			if (username.equals(this.get(i).getUsername())) {
				return this.get(i);
			}
		} 
		throw new WrongUsernameException();
	}
	
}
