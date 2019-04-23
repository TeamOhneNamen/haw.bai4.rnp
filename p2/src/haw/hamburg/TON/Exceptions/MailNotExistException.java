package haw.hamburg.TON.Exceptions;

public class MailNotExistException extends Exception {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MailNotExistException() {
		super("Mail Do Not Exist");
	}
}
