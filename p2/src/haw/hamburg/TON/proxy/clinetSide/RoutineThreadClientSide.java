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

/**
 * 
 * @author Ferdinand Trendelenburg AND Thorben Schomacker
 *
 *         Deal with the CLIENTS wich are connected with the POP3-Proxy-Server
 * 
 *
 */
public class RoutineThreadClientSide extends Thread {

	// Konstanten
	final static String USERNAME_NOT_FOUND = "-ERR USERNAME NOT EXIST";
	final static String COMMAND_NOT_FOUND = "-ERR COMMAND NOT FOUND";

	// variabeln
	private BufferedReader inFromClient;
	private PrintWriter out2Client;
	private USER user;
	Socket client;
	boolean clientAlive = false;
	int timeout;

	// messages

	/**
	 * Constructor
	 * @param userClinet = socket connection to client to handle
	 * @param timeout = after timeout The Server not get message from Clients will shutdown
	 */
	public RoutineThreadClientSide(Socket userClinet, int timeout) {
		client = userClinet;
		this.timeout = timeout;
	}

	/**
	 * starts the Clientside Routine with 
	 * "ANMELDEN" - "ARBEITEN" - "LOESCHEN" - "ABMELDEN",
	 */
	@Override
	public void run() {

		try {

			inFromClient = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
			out2Client = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8),
					true);

			client.setSoTimeout(timeout);
			clientAlive = true;
			// willkommensnachricht an den Clienten
			sendMSG("+OK Welcome to Proxy");

			try {
//				Den Anmeldeversuch unendlich wiederholen
				while (!anmeldung()) {
				}
				abholung();
			} catch (IOException e) {
				Pop3ProxyClientSide.send2ProxyConsole("Client: " + client.getLocalAddress() + " disconnected.");
				clientAlive = false;
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

	/**
	 * login user
	 * PLAN:
	 * 1. command = "USER Username" send "+OK USERNAME CORREKT" if Username exist | ELSE send "-ERR USERNAME NOT EXIST"
	 * 2. command = "PASS Password" send "+OK PASSWORD CORREKT" if Passwort fits to Username | ELSE send "-ERR PASSWORD INCORREKT"
	 * 
	 * ELSE:
	 * command = "CAPA" send "+OK" and "."
	 * command = "QUIT" send "+OK" 
	 * command = "AUTH" send "-ERR"
	 * 
	 * @return true if the Login was sucessfull
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	private boolean anmeldung() throws UnsupportedEncodingException, IOException {
		Pop3ProxyClientSide.send2ProxyConsole("---------ANMELDEN---------");

		sendMSG("+OK hallo hier ist der ProxyServer");

		String msg = getMSG();
		while (!msg.startsWith("USER")) {
			if (msg.startsWith("CAPA")) {
				Pop3ProxyClientSide.send2ProxyConsole("+OK " + msg);
				sendMSG("+OK");
				sendMSG(".");
				msg = getMSG();
			} else if (msg.startsWith("AUTH")) {
				Pop3ProxyClientSide.send2ProxyConsole("-ERR " + msg);
				sendMSG("-ERR");
				msg = getMSG();
			} else if (msg.startsWith("QUIT")) {
				Pop3ProxyClientSide.send2ProxyConsole("+OK " + msg);
				sendMSG("+OK");
				msg = getMSG();
			} else {

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

	/**
	 * send Messages to the Client if he wants some
	 * Commands: getMSG();
	 * 
	 * command = LIST -> sendList()
	 * command = LIST NUM -> sendList(NUM)
	 * command = DELE NUM -> sendDELE(NUM)
	 * command = RETR NUM -> sendRETR(NUM)
	 * command = NOOP -> sendNOOP()
	 * command = STAT -> sendSTAT()
	 * command = RSET -> sendRSET()
	 * command = QUIT -> sendQUIT()
	 * ELSE -> send COMMAND NOT FOUND
	 * 
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	private void abholung() throws UnsupportedEncodingException, IOException {

		Pop3ProxyClientSide.send2ProxyConsole("---------ARBEITEN---------");
		while (clientAlive) {

			// command interpreter
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

	/**
	 * NOT IN USE
	 */
	private void sendUIDL() {
		sendMSG("+OK");
		for (int i = 0; i < user.getMailingQueue().size(); i++) {
			sendMSG((i + 1) + " " + user.getMailingQueue().get(i).hashCode());

		}
		sendMSG(".");

	}

	/**
	 * NOT IN USE
	 * @param argumentNumber
	 */
	private void sendUIDL(int argumentNumber) {

		try {
			sendMSG("+OK " + argumentNumber + " " + user.getMailByNumber(argumentNumber).hashCode());
		} catch (MailNotExistException e) {
			sendMSG("-ERR " + "Mail nr: " + argumentNumber + " Not Found");
		}
	}

	/**
	 * recieve a Message from Client with the length of 255
	 * @return MESSAGE
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	private String getMSG() throws UnsupportedEncodingException, IOException {
		char[] message = new char[255];
		inFromClient.read(message, 0, 255);
		String[] messages = new String(message, 0, 255).split("\r\n");
		String messageString = messages[0];
		return messageString;
	}

	/**
	 * send a Message to Client
	 * @param msg
	 */
	private void sendMSG(String msg) {
		out2Client.print(msg + "\r\n");
		out2Client.flush();
	}

	/**
	 * get Username from Client
	 * msg should have the Format: "USER Username" and the Username should exist in the UserList
	 * ELSE send to Client "-ERR USERNAME NOT FOUND"
	 * @param msg
	 * @return true -> if the Username esist in The UserList
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
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

	/**
	 * get Passwort from Client
	 * msg should have the Format: "PASS Passwort" and the Passwort should fit to the the recieved Username
	 * ELSE send to Client "-ERR USERNAME NOT FOUND"
	 * 
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
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

	/**
	 * send NOOP sends "+OK" to the Client 
	 */
	private void sendNOOP() {
		sendMSG("+OK");
	}

	/**
	 * 
	 * Format by Succ:
	 * "+OK MESSAGES messages OCTETS octets"
	 * 
	 * 
	 * MESSAGES = ammount of Messages saves on the Proxy
	 * OCTETS = Size of all Messages on Bytes (Octets) 
	 */
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

	/**
	 * Set DeletedFlag on the Mail with the position "index" in the MailingList to true
	 * 
	 * if it is allready on true send to Client: "-ERR message "index" allready deleted"
	 * 
	 * Format by Succ:
	 * +OK message "index" deleted
	 * 
	 * @param index
	 */
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

	/**
	 * Delete all Mail from the User, with the DeletedFlar==true
	 * close connection to Client
	 */
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

	/**
	 * send the Mail with Position "index" to the Client if the DeleteFlag is flase
	 * if DeleteFlag is true -> "-ERR message " + index + " already deleted"
	 * 
	 * Format by Succ:
	 * "+OK "Octets" octets"
	 * "MAIL_CONTENT"
	 * "MAIL_CONTENT"
	 * "."
	 * 
	 * @param index
	 */
	private void sendRETR(int index) {

		try {
			Mail message = user.getMailByNumber(index);
			if (!message.isDeleteFlag()) {
				sendMSG("+OK " + message.getOctets() + " octets");
				String[] msgarr = message.getMsg();
				for (int i = 0; i < msgarr.length; i++) {
					sendMSG(msgarr[i]);

				}
			} else {
				sendMSG("-ERR message " + index + " already deleted");

			}

		} catch (MailNotExistException e) {
			sendMSG("-ERR message " + index + " dont exist");
		}

	}

	/**
	 * set all DeletedFlags -> true
	 */
	private void sendRSET() {
		int ammoundOfDeletedMSG = user.getAmmoundOfDeletedMessages();
		int ammoundOfDeletedOctets = user.getAmmoundOfDeletedOctets();
		user.setAllMailsDeleteState(false);
		sendMSG("+OK maildrop has " + ammoundOfDeletedMSG + " messages (" + ammoundOfDeletedOctets + " octets)");
	}

	/**
	 * send a List of Mails saved on the Server to the Client
	 * 
	 * Format by Succ:
	 * "+OK "MESSANGES" messages ("OCTETS" octets)"
	 * "NUMBER_OF_MAIL SINGLE_OCTETS"
	 * "NUMBER_OF_MAIL SINGLE_OCTETS"
	 * "NUMBER_OF_MAIL SINGLE_OCTETS"
	 * "."
	 * 
	 * MESSANGES = Ammound of Mails in the MailingList of User
	 * OCTETS = Size of all Mails in the MailingList of User
	 * NUMBER_OF_MAIL = Position of the Mail in The MailingList
	 * SINGLE_OCTETS = Size of the Mail in Bytes (Octets)
	 * 
	 */
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

	/**
	 * sends Position and Size of Mail do Client 
	 * 
	 * Format by Succ:
	 * "+OK "index" OCTETS"
	 * 
	 * OCTETS = Size of the Mail on Position "index" in MailingList in Bytes (Octets)
	 * 
	 * @param index
	 */
	private void sendList(int index) {

		try {
			Long ammoundOfOctets = user.getMailByNumber(index).getOctets();
			sendMSG("+OK " + index + " " + ammoundOfOctets);

		} catch (MailNotExistException e) {
			sendMSG("-ERR message " + index + " dont Exist");
		}
	}

}
