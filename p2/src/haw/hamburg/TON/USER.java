package haw.hamburg.TON;

import java.util.ArrayList;

import haw.hamburg.TON.Exceptions.MailNotExistException;

public class USER {

	private String username;
	private String passwort;
	private String serverName; 
	private String serverPort;

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getServerPort() {
		return serverPort;
	}

	public void setServerPort(String serverPort) {
		this.serverPort = serverPort;
	}

	private ArrayList<Mail> mailingQueue = new ArrayList<Mail>();
	
	public ArrayList<Mail> getMailingQueue() {
		return mailingQueue;
	}

	public void setMailingQueue(ArrayList<Mail> mailingQueue) {
		this.mailingQueue = mailingQueue;
	}

	public USER(String username, String passwort, String serverName, String serverPort) {
		this.username = username;
		this.passwort = passwort;
		this.serverName = serverName;
		this.serverPort = serverPort;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPasswort() {
		return passwort;
	}

	public void setPasswort(String passwort) {
		this.passwort = passwort;
	}
	
	public void setAllMailsDeleteState(boolean newValue) {
		for (int i = 0; i < mailingQueue.size(); i++) {
			mailingQueue.get(i).setDeleteFlag(newValue);
		}
	}

	public int getAmmoundOfDeletedMessages() {
		int ammound = 0;
		for (int i = 0; i < mailingQueue.size(); i++) {
			if(mailingQueue.get(i).isDeleteFlag()){
				ammound++;
			}
		}
		return ammound;
	}
	
	public int getAmmoundOfDeletedOctets() {
		int ammound = 0;
		for (int i = 0; i < mailingQueue.size(); i++) {
			if(mailingQueue.get(i).isDeleteFlag()){
				ammound =(int) + mailingQueue.get(i).getOctets();
			}
		}
		return ammound;
	}
	
	public Mail getMailByNumber(int mailNumber) throws MailNotExistException {
		try {
			return getMailingQueue().get(mailNumber-1);
		} catch (Exception e) {
			throw new MailNotExistException();
		}
	}
	
}
