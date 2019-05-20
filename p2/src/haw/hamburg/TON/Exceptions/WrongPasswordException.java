package haw.hamburg.TON.Exceptions;

public class WrongPasswordException extends Exception {

	private static final long serialVersionUID = 1L;

	public WrongPasswordException() {
		super("Fasches Passwort");
	}
	
}
