package haw.hamburg.TON.proxy.clinetSide;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import javax.swing.text.MaskFormatter;

import haw.hamburg.TON.Mail;
import haw.hamburg.TON.Pop3ProxyServer;
import haw.hamburg.TON.USER;
import haw.hamburg.TON.Exceptions.MailNotExistException;
import haw.hamburg.TON.Exceptions.WrongUsernameException;

public class RoutineThreadClientSide extends Thread {

	private BufferedReader inFromClient;
	private PrintWriter out2Client;
	private USER user;
	Socket client;
	boolean clientAlive = false;

	String input;

	public RoutineThreadClientSide(Socket userClinet) throws IOException {
		client = userClinet;
		inFromClient = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
		out2Client = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true);

	}

	@Override
	public void run() {
		clientAlive = true;
		sendMSGLine("+OK example.com POP3-Server");
		while (Pop3ProxyServer.serverAlive && clientAlive) {
			try {
				while (!anmeldung()) {
				}
				abholung();
			} catch (IOException e) {
				Pop3ProxyClientSide.send2ProxyConsole("Client: " + client.getLocalAddress() + " disconnected.");
				clientAlive = false;
			} catch (WrongUsernameException e) {
				Pop3ProxyClientSide.send2ProxyConsole("Username existiert nicht");
			}
		}

	}

	private boolean anmeldung() throws UnsupportedEncodingException, IOException, WrongUsernameException {
		Pop3ProxyClientSide.send2ProxyConsole("Anmeldung von POP3-Client mit den gueltigen Account-Daten");
		Pop3ProxyClientSide.send2ProxyConsole("Versuche Anmeldung von:");
		if (getUsername()) {
			Pop3ProxyClientSide.send2ProxyConsole(user.getUsername());
			sendMSGLine("+OK USERNAME CORREKT");
			if (checkPasswort()) {
				sendMSGLine("+OK PASSWORD CORREKT");
				Pop3ProxyClientSide.send2ProxyConsole("User angemeldet");
				return true;
			} else {
				sendMSGLine("-ERR PASSWORD INCORREKT");
				return false;
			}

		} else {
			sendMSGLine("-ERR USERNAME INCORREKT");
			return false;
		}

	}

	private void abholung() throws UnsupportedEncodingException, IOException {
		while (Pop3ProxyServer.serverAlive) {

			String msg = getMSG();
			System.out.println(msg);
			if (msg.startsWith("LIST")) {
				if (msg.length() >= 6) {
					String argument = msg.substring(msg.indexOf(" ") + 1, msg.length());
					int argumentNumber = Integer.valueOf(argument);
					sendList(argumentNumber);
				} else {
					sendList();
				}
			} else if (msg.startsWith("DELE")) {
				if (msg.length() >= 6) {
					String argument = msg.substring(msg.indexOf(" ") + 1, msg.length());
					int argumentNumber = Integer.valueOf(argument);
					sendDELE(argumentNumber);
				} else {
					sendMSGLine("-ERR no arguments an 'DELE'");
				}
			} else if (msg.startsWith("RETR")) {
				if (msg.length() >= 6) {
					String argument = msg.substring(msg.indexOf(" ") + 1, msg.length());
					int argumentNumber = Integer.valueOf(argument);
					sendRETR(argumentNumber);
				} else {
					sendMSGLine("-ERR no arguments an 'RETR'");
				}
			} else if (msg.startsWith("NOOP")) {
				if (msg.length() == 4) {
					sendNOOP();
				} else {
					sendMSGLine("-ERR no arguments an 'NOOP'");
				}
			} else if (msg.startsWith("STAT")) {
				if (msg.length() == 4) {
					sendSTAT();
				} else {
					sendMSGLine("-ERR no arguments an 'STAT'");
				}
			} else if (msg.startsWith("RSET")) {
				if (msg.length() == 4) {
					sendRSET();
				} else {
					sendMSGLine("-ERR no arguments an 'RSET'");
				}
			} else if (msg.startsWith("UIDL")) {
				if (msg.length() == 4) {
					sendUIDL();
				} else if (msg.length() >= 4){
					String argument = msg.substring(msg.indexOf(" ") + 1, msg.length());
					int argumentNumber = Integer.valueOf(argument);
					sendUIDL(argumentNumber);
				}
			} else if (msg.startsWith("QUIT")) {
				if (msg.length() == 4) {
					sendQUIT();
				} else {
					sendMSGLine("-ERR no arguments an 'QUIT'");
				}
			}
		}
	}

	private void sendUIDL() {
		sendMSGLine("+OK");
		for (int i = 0; i < user.getMailingQueue().size(); i++) {
			sendMSGLine(user.getMailingQueue().get(i).getMailNumber() + " " + user.getMailingQueue().get(i).hashCode());
		}
		sendMSGLine(".");
	}

	private void sendUIDL(int argumentNumber) {
		
		try {
			sendMSGLine("+OK " + user.getMailByNumber(argumentNumber).getMailNumber() + " " + user.getMailByNumber(argumentNumber).hashCode());
		} catch (MailNotExistException e) {
			sendMSGLine("-ERR " + "Mail nr: " + argumentNumber + " Not Found");
		}
	}

	private String getMSG() throws UnsupportedEncodingException, IOException {
		return inFromClient.readLine();
	}

	private void sendMSGLine(String msg) {
		out2Client.println(msg);
	}
	private void sendMSG(String msg) {
		out2Client.print(msg);
		out2Client.flush();
	}

	private boolean getUsername() throws UnsupportedEncodingException, IOException, WrongUsernameException {
		String msg = getMSG();
		System.out.println("erhalten: " +msg);
		if (msg.startsWith("USER")) {
			String username = msg.substring(msg.indexOf(" ") + 1, msg.length());
			user = Pop3ProxyServer.userList.getUserbyName(username);
			if (user!=null) {
				return true;	
			} 
			
		}
		return false;
	}

	private boolean checkPasswort() throws UnsupportedEncodingException, IOException {
		String msg = getMSG();
		System.out.println("erhalten: " +msg);
		String passwort;
		if (msg.startsWith("PASS")) {
			passwort = user.getPasswort();
			if (passwort.equals(msg.substring(msg.indexOf(" ") + 1, msg.length()))) {
				return true;
			}
		}
		return false;
	}

	private void sendNOOP() {

		sendMSGLine("+OK");

	}

	private void sendSTAT() {
		int ammoundOfMSG = 0;
		int ammoundOfOctets = 0;
		for (int i = 0; i < user.getMailingQueue().size(); i++) {
			if (!user.getMailingQueue().get(i).isDeleteFlag()) {
				ammoundOfMSG++;
				ammoundOfOctets += user.getMailingQueue().get(i).getOctets();

			}

		}
		sendMSGLine("+OK " + ammoundOfMSG + " messages (" + ammoundOfOctets + " octets)");
	}

	private void sendDELE(int index) {
		try {
			Mail message = user.getMailByNumber(index);
			if (!message.isDeleteFlag()) {
				message.setDeleteFlag(true);
				sendMSGLine("+OK message " + message.getMailNumber() + " deleted");
			} else {
				sendMSGLine("-ERR message " + message.getMailNumber() + " allready deleted");
			}
		} catch (MailNotExistException e) {
			sendMSGLine("-ERR message " + index + " allready deleted");
		}

	}

	private void sendQUIT() {
		int ammoundDeletet = 0;
		for (int i = user.getMailingQueue().size() - 1; i > 0; i--) {
			if (user.getMailingQueue().get(i).isDeleteFlag()) {
				user.getMailingQueue().remove(i);
				ammoundDeletet++;
			}
		}
		Pop3ProxyClientSide.send2ProxyConsole(ammoundDeletet + " Nachrichten wurden endgueltig geloescht!");
		clientAlive = false;
	}

	private void sendRETR(int index) {

		try {
			Mail message = user.getMailByNumber(index);
			if (!message.isDeleteFlag()) {
				sendMSGLine("+OK " + message.getOctets() + " octets");
				String[] msgarr = message.getMsg().split("\n");
				for (int i = 0; i < msgarr.length; i++) {
					String temp = msgarr[i];
					if (temp.equals(" ")) {
						temp = "\n";
					}
					sendMSGLine(temp);

					System.out.println(i + "_:" +temp);
					
				}
			} else {
				sendMSGLine("-ERR message " + index + " already deleted");
				
			}

		} catch (MailNotExistException e) {
			sendMSGLine("-ERR message " + index + " dont exist");
		}
		
	}

	private void sendRSET() {
		int ammoundOfDeletedMSG = user.getAmmoundOfDeletedMessages();
		int ammoundOfDeletedOctets = user.getAmmoundOfDeletedOctets();
		user.setAllMailsDeleteState(false);
		sendMSGLine("+OK maildrop has " + ammoundOfDeletedMSG + " messages (" + ammoundOfDeletedOctets + " octets)");
	}

	private void sendList() {
		int ammoundOfMSG = 0;
		int ammoundOfOctets = 0;
		for (int i = 0; i < user.getMailingQueue().size(); i++) {
			if (!user.getMailingQueue().get(i).isDeleteFlag()) {
				ammoundOfMSG++;
				ammoundOfOctets += user.getMailingQueue().get(i).getOctets();
			}

		}
		sendMSGLine("+OK " + ammoundOfMSG + " messages (" + ammoundOfOctets + " octets)");
		for (int i = 0; i < user.getMailingQueue().size(); i++) {
			if (!user.getMailingQueue().get(i).isDeleteFlag()) {
				sendMSGLine(user.getMailingQueue().get(i).getMailNumber() + " "
						+ user.getMailingQueue().get(i).getOctets());
			}

		}
		sendMSGLine(".");
	}

	private void sendList(int argumentNumber) {

		try {
			int ammoundOfOctets = user.getMailByNumber(argumentNumber).getOctets();
			sendMSGLine("+OK " + argumentNumber + " " + ammoundOfOctets);

		} catch (MailNotExistException e) {
			sendMSGLine("-ERR message " + argumentNumber + " dont Exist");
		}
	}

}
