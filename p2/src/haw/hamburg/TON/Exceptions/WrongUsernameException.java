package haw.hamburg.TON.Exceptions;

public class WrongUsernameException extends Exception {

	private static final long serialVersionUID = 1L;

	public WrongUsernameException() {
		super("Fascher Username");
	}
	
}
