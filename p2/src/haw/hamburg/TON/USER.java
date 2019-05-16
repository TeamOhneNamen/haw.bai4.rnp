package haw.hamburg.TON;

import java.util.ArrayList;

import haw.hamburg.TON.Exceptions.MailNotExistException;

/**
 * 
 * @author Ferdinand Trendelenburg AND Thorben Schomacker
 *
 * USER for POP3-Proxy-Server contains:
 * username as Username of POP3 EMAIL_Accounts
 * passwort as Passwort of POP3 EMAIL_Accounts
 * serverName ServerAdress of the POP3-Server of the EMAIL_Accounts
 * serverPort ServerPort of the POP3-Server of the EMAIL_Accounts
 *
 */
public class USER {

	private String username;
	private String passwort;
	private String serverName; 
	private String serverPort;

	private ArrayList<Mail> mailingQueue = new ArrayList<Mail>();
	
	
	/**
	 * getServerName
	 * @return serverName
	 */
	public String getServerName() {
		return serverName;
	}

	/**
	 * setServerName
	 * @param serverName new Value
	 */
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	/**
	 * getServerPort
	 * @return serverPort
	 */
	public String getServerPort() {
		return serverPort;
	}

	/**
	 * setServerPort
	 * @param serverPort new Value
	 */
	public void setServerPort(String serverPort) {
		this.serverPort = serverPort;
	}

	/**
	 * getMailingQueue
	 * @return mailingQueue = ArrayList of all Mails from a User
	 */
	public ArrayList<Mail> getMailingQueue() {
		return mailingQueue;
	}

	/**
	 * setMailingQueue
	 * @param mailingQueue = sets ArrayList of all Mails from a User
	 */
	public void setMailingQueue(ArrayList<Mail> mailingQueue) {
		this.mailingQueue = mailingQueue;
	}
	
	/**
	 * getUsername
	 * @return username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * setUsername
	 * @param username new Value
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * getPasswort
	 * @return passwort
	 */
	public String getPasswort() {
		return passwort;
	}
	/**
	 * setPasswort
	 * @param passwort new Value
	 */
	public void setPasswort(String passwort) {
		this.passwort = passwort;
	}

	/**
	 * Constructor of USER
	 * @param username new Value
	 * @param passwort new Value
	 * @param serverName = String serverName or serverIP
	 * @param serverPort = String ServerPort most be parsable to Integer
	 */
	public USER(String username, String passwort, String serverName, String serverPort) {
		this.username = username;
		this.passwort = passwort;
		this.serverName = serverName;
		this.serverPort = serverPort;
	}

	/**
	 * set All Mails the Delete flag on "newValue"(boolean)
	 * @param newValue new Value
	 */
	public void setAllMailsDeleteState(boolean newValue) {
		for (int i = 0; i < mailingQueue.size(); i++) {
			mailingQueue.get(i).setDeleteFlag(newValue);
		}
	}

	/**
	 * get Ammound Of Messages with delete Flag==true
	 * @return ammound
	 */
	public int getAmmoundOfDeletedMessages() {
		int ammound = 0;
		for (int i = 0; i < mailingQueue.size(); i++) {
			if(mailingQueue.get(i).isDeleteFlag()){
				ammound++;
			}
		}
		return ammound;
	}
	
	/**
	 * get Ammound of octets from Messages with delete Flag==true
	 * @return ammound
	 */
	public int getAmmoundOfDeletedOctets() {
		int ammound = 0;
		for (int i = 0; i < mailingQueue.size(); i++) {
			if(mailingQueue.get(i).isDeleteFlag()){
				ammound =(int) + mailingQueue.get(i).getOctets();
			}
		}
		return ammound;
	}
	
	/**
	 * Get Mail Witch has the Number "mailNumber"
	 * @param mailNumber new Value
	 * @return Mail
	 * @throws MailNotExistException = when the mail not exist
	 */
	public Mail getMailByNumber(int mailNumber) throws MailNotExistException {
		try {
			return getMailingQueue().get(mailNumber-1);
		} catch (Exception e) {
			throw new MailNotExistException();
		}
	}
	
}
