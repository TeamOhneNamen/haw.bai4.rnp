package haw.hamburg.TON;

import java.util.ArrayList;

import haw.hamburg.TON.Exceptions.WrongUsernameException;

/**
 * 
 * @author Ferdinand Trendelenburg AND Thorben Schomacker
 *
 * Manage Multiply Users
 *
 */
public class USERList extends ArrayList<USER>{

	private static final long serialVersionUID = 1L;
	
	/**
	 * get User by Name: "username"
	 * @param username new Value
	 * @return USER
	 * @throws WrongUsernameException = if the User not Exist
	 */
	public USER getUserbyName(String username) throws WrongUsernameException {
		for (int i = 0; i < this.size(); i++) {
			if (username.equals(this.get(i).getUsername())) {
				return this.get(i);
			}
		} 
		throw new WrongUsernameException();
	}
	
}
