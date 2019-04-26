package haw.hamburg.TON.proxy.clinetSide;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

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
	int timeout;

	//messages
	final static String USERNAME_NOT_FOUND = "-ERR USERNAME NOT EXIST";
	final static String COMMAND_NOT_FOUND = "-ERR COMMAND NOT FOUND";
	
	
	
	String input;

	public RoutineThreadClientSide(Socket userClinet, int timeout) throws IOException {
		client = userClinet;
		this.timeout = timeout;
	}

	@Override
	public void run() {

		try {
			inFromClient = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
			out2Client = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true);

			client.setSoTimeout(timeout);
			clientAlive = true;
			sendMSG("+OK Welcome to Proxy");
			while (Pop3ProxyServer.serverAlive && clientAlive) {
				
				try {
					while (!anmeldung()) {
					}
					abholung();
				} catch (IOException e) {
					Pop3ProxyClientSide.send2ProxyConsole("Client: " + client.getLocalAddress() + " disconnected.");
					clientAlive = false;
				} 
			}
		} catch (SocketException e1) {
			sendMSG("-ERR Out Of Time");
			Pop3ProxyClientSide.send2ProxyConsole("verliert die Verbundung");
			clientAlive = false;
		} catch (IOException e1) {
			clientAlive = false;
			e1.printStackTrace();
		}
	}

	private boolean anmeldung() throws UnsupportedEncodingException, IOException {
		Pop3ProxyClientSide.send2ProxyConsole("---------ANMELDEN---------");

		String msg = getMSG();
		while (!msg.startsWith("USER")) {
			if (msg.startsWith("CAPA")) {
				//Pop3ProxyClientSide.send2ProxyConsole("+OK " + msg);
				sendMSG("+OK");
				sendMSG(".");
				msg = getMSG();
			}else if (msg.startsWith("AUTH")) {
				Pop3ProxyClientSide.send2ProxyConsole("-ERR " + msg);
				sendMSG("-ERR");
				msg = getMSG();
			}else {
				Pop3ProxyClientSide.send2ProxyConsole("+OK " + msg);
				sendMSG("+OK " + msg.toUpperCase());
				msg = getMSG();
			}
			
		}
		if (getUsername(msg)) {
			Pop3ProxyClientSide.send2ProxyConsole("anmelden von: " + user.getUsername());
			Pop3ProxyClientSide.send2ProxyConsole("...");
			sendMSG("+OK USERNAME CORREKT");
			if (checkPasswort()) {
				sendMSG("+OK PASSWORD CORREKT");
				Pop3ProxyClientSide.send2ProxyConsole(user.getUsername() + " angemeldet");
				return true;
			} else {
				sendMSG("-ERR PASSWORD INCORREKT");
				return false;
			}

		} else {
			return false;
		}

	}

	private void abholung() throws UnsupportedEncodingException, IOException {

		Pop3ProxyClientSide.send2ProxyConsole("---------ARBEITEN---------");
		while (clientAlive) {

			String msg = getMSG();
			Pop3ProxyClientSide.send2ProxyConsole("(" + user.getUsername() + ") Verarbeite: " + msg);
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
					sendMSG("-ERR no arguments an 'DELE'");
				}
			} else if (msg.startsWith("RETR")) {
				if (msg.length() >= 6) {
					String argument = msg.substring(msg.indexOf(" ") + 1, msg.length());
					int argumentNumber = Integer.valueOf(argument);
					sendRETR(argumentNumber);
				} else {
					sendMSG("-ERR no arguments an 'RETR'");
				}
			} else if (msg.startsWith("NOOP")) {
				if (msg.length() == 4) {
					sendNOOP();
				} else {
					sendMSG("-ERR no arguments an 'NOOP'");
				}
			} else if (msg.startsWith("STAT")) {
				if (msg.length() == 4) {
					sendSTAT();
				} else {
					sendMSG("-ERR no arguments an 'STAT'");
				}
			} else if (msg.startsWith("RSET")) {
				if (msg.length() == 4) {
					sendRSET();
				} else {
					sendMSG("-ERR no arguments an 'RSET'");
				}
			} else if (msg.startsWith("UIDL")) {
				if (msg.length() == 4) {
					sendUIDL();
				} else if (msg.length() >= 4) {
					String argument = msg.substring(msg.indexOf(" ") + 1, msg.length());
					int argumentNumber = Integer.valueOf(argument);
					sendUIDL(argumentNumber);
				}
			} else if (msg.startsWith("QUIT")) {
				if (msg.length() == 4) {
					sendQUIT();
				} else {
					sendMSG("-ERR no arguments an 'QUIT'");
				}
			} else {
				sendMSG(COMMAND_NOT_FOUND);
			}
		}
	}

	private void sendUIDL() {
		sendMSG("+OK");
		for (int i = 0; i < user.getMailingQueue().size(); i++) {
			sendMSG((i + 1) + " " + user.getMailingQueue().get(i).hashCode());

		}
		sendMSG(".");

	}

	private void sendUIDL(int argumentNumber) {

		try {
			sendMSG("+OK " + argumentNumber + " " + user.getMailByNumber(argumentNumber).hashCode());
		} catch (MailNotExistException e) {
			sendMSG("-ERR " + "Mail nr: " + argumentNumber + " Not Found");
		}
	}


	//TODO: KEIL READ
	private String getMSG() throws UnsupportedEncodingException, IOException {
//		char[] message = new char[255];
//		inFromClient.read(message, 0, 255);
//		String[] messages = new String(message, 0, 255).split("\r\n");
//		System.out.print("getMSG(): "+ messages[0]);
//		String messageString = messages[0];
		String messageString = inFromClient.readLine();
		return messageString;
	}

	//TODO: KEIL READ
	private void sendMSG(String msg) {
//		out2Client.println(msg);
		out2Client.print(msg + "\r\n");
		out2Client.flush();
	}

	private boolean getUsername(String msg) throws UnsupportedEncodingException, IOException {
		if (msg.startsWith("USER")) {
			String username = msg.substring(msg.indexOf(" ") + 1, msg.length());
			
			try {
				user = Pop3ProxyServer.userList.getUserbyName(username);
				return true;
				
			} catch (WrongUsernameException e) {
				Pop3ProxyClientSide.send2ProxyConsole(USERNAME_NOT_FOUND);
				sendMSG(USERNAME_NOT_FOUND);
				return false;
			}
			
		} else {
			sendMSG(COMMAND_NOT_FOUND);
		}
		return false;
	}

	private boolean checkPasswort() throws UnsupportedEncodingException, IOException {
		String msg = getMSG();
		String passwort;
		if (msg.startsWith("PASS")) {
			passwort = user.getPasswort();
			return passwort.equals(msg.substring(msg.indexOf(" ") + 1, msg.length()));
		} else {
			sendMSG(COMMAND_NOT_FOUND);
		}
		return false;
	}

	private void sendNOOP() {

		sendMSG("+OK");

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
		sendMSG("+OK " + ammoundOfMSG + " messages (" + ammoundOfOctets + " octets)");
	}

	private void sendDELE(int index) {
		try {
			Mail message = user.getMailByNumber(index);
			if (!message.isDeleteFlag()) {
				message.setDeleteFlag(true);
				sendMSG("+OK message " + index + " deleted");
			} else {
				sendMSG("-ERR message " + index + " allready deleted");
			}
		} catch (MailNotExistException e) {
			sendMSG("-ERR message " + index + " allready deleted");
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
		Pop3ProxyClientSide
				.send2ProxyConsole("Verbindung zu: " + client.getInetAddress().getHostAddress() + " geschlossen.");
		sendMSG("+OK bye");
		Pop3ProxyClientSide.send2ProxyConsole("-----------ENDE-----------");
		clientAlive = false;
	}

	private void sendRETR(int index) {

		try {
			Mail message = user.getMailByNumber(index);
			if (!message.isDeleteFlag()) {
				sendMSG("+OK " + message.getOctets() + " octets");
				String[] msgarr = message.getMsg().split("\n");
				for (int i = 0; i < msgarr.length; i++) {
					String temp = msgarr[i];
					if (temp.equals(" ")) {
						temp = "\n";
					}
					sendMSG(temp);

				}
			} else {
				sendMSG("-ERR message " + index + " already deleted");

			}

		} catch (MailNotExistException e) {
			sendMSG("-ERR message " + index + " dont exist");
		}

	}

	private void sendRSET() {
		int ammoundOfDeletedMSG = user.getAmmoundOfDeletedMessages();
		int ammoundOfDeletedOctets = user.getAmmoundOfDeletedOctets();
		user.setAllMailsDeleteState(false);
		sendMSG("+OK maildrop has " + ammoundOfDeletedMSG + " messages (" + ammoundOfDeletedOctets + " octets)");
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
		sendMSG("+OK " + ammoundOfMSG + " messages (" + ammoundOfOctets + " octets)");
		for (int i = 0; i < user.getMailingQueue().size(); i++) {
			if (!user.getMailingQueue().get(i).isDeleteFlag()) {
				sendMSG((i + 1) + " " + user.getMailingQueue().get(i).getOctets());
			}

		}
		sendMSG(".");
	}

	private void sendList(int argumentNumber) {

		try {
			int ammoundOfOctets = user.getMailByNumber(argumentNumber).getOctets();
			sendMSG("+OK " + argumentNumber + " " + ammoundOfOctets);

		} catch (MailNotExistException e) {
			sendMSG("-ERR message " + argumentNumber + " dont Exist");
		}
	}

}
