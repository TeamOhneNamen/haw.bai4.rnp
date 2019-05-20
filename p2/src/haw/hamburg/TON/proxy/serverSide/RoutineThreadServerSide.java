package haw.hamburg.TON.proxy.serverSide;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import haw.hamburg.TON.Mail;
import haw.hamburg.TON.Pop3ProxyServer;
import haw.hamburg.TON.USER;
//import haw.hamburg.TON.Exceptions.NoopException;
import haw.hamburg.TON.Exceptions.WrongPasswordException;
import haw.hamburg.TON.Exceptions.WrongUsernameException;

/**
 * 
 * @author Ferdinand Trendelenburg AND Thorben Schomacker
 * RoutineThreadServerSide mannage the recieving of Mails from The POP3-Server 
 */

public class RoutineThreadServerSide extends Thread {

	/**
	 * Reader and Writer
	 */
	private BufferedReader inFromServer;
	private PrintWriter out2Server;

	/**
	 * Anzahl der Emails
	 */
	int ammound = 0;

	Socket server;

	String addr;
	int port;

	String user;
	String pass;

	private boolean clientAlive;
	private int zeitabstand;

	/**
	 * 
	 * @param zeitabstand time between the pull of Mails
	 * @param user = the Userdata includes The Password, username, serverAdress, port
	 * @throws IOException = if User.getServerPoint is not parseable to a Integer
	 */
	public RoutineThreadServerSide(int zeitabstand, USER user) throws IOException {
		this.zeitabstand = zeitabstand;
		this.user = user.getUsername();
		pass = user.getPasswort();
		addr = user.getServerName();
		port = Integer.parseInt(user.getServerPort());
	}

	/**
	 * starts the ServersideRoutine with 
	 * "ANMELDEN" - "ARBEITEN" - "LOESCHEN" - "ABMELDEN", 
	 */
	@Override
	public void run() {

		clientAlive = true;
		while (clientAlive) {

			try {
				server = new Socket(addr, port);
				send2ProxyConsole("Verbindung zu: >" + addr + ":" + port + "< hergestellt.");

				inFromServer = new BufferedReader(new InputStreamReader(server.getInputStream(), StandardCharsets.UTF_8));
				out2Server = new PrintWriter(new OutputStreamWriter(server.getOutputStream(), StandardCharsets.UTF_8), true);

				send2ProxyConsole(receveMSG());

			} catch (IOException e) {
				e.printStackTrace();
			}

			ArrayList<Mail> mailList = Pop3ProxyServer.userList.get(0).getMailingQueue();

			send2ProxyConsole("--------ANMELDEN----------");
			anmeldung(user, pass);
			send2ProxyConsole("--------ARBEITEN----------");
			abholung(mailList);
			send2ProxyConsole("--------LOESCHEN----------");
			loeschung(mailList);
			send2ProxyConsole("--------ABMELDEN----------");
			sendQuit();
			send2ProxyConsole("----------ENDE------------");

			try {
				Thread.sleep(zeitabstand);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

	}

	/**
	 * login User on server with The Username and Password
	 * @param user -> Username of the User of the Thread
	 * @param pass -> Password of the User of the Thread
	 */
	private void anmeldung(String user, String pass) {

		send2ProxyConsole("Anmeldung bei dem jeweiligen POP3-Server mit den gueltigen Account-Daten");

		try {

			sendUser(user);
			sendPasswort(pass);

		} catch (WrongPasswordException e) {
			send2ProxyConsole("Falsches Passwort");
		} catch (WrongUsernameException e) {
			send2ProxyConsole("Falscher Username");
		}

		send2ProxyConsole("User: " + user + " erfolgreich angemeldet");

	}

	/**
	 * recieve all messages from POP3 Server and put it in the MailingList of the User
	 * @param mailList -> The List of Mail from the User
	 */
	private void abholung(ArrayList<Mail> mailList) {
		send2ProxyConsole("Abholung aller fuer diesen Account eingetroffenen E-Mails");

		// get the ammound of mails on the Server with STAT
		String ergString[] = sendStat();
		ammound = Integer.parseInt(ergString[1]);

		send2ProxyConsole("Empfange " + ammound + " mails.");
		for (int j = 1; j < ammound + 1; j++) {

			// get a list of all Messages on the Server
			String[] msg = sendRetr(j);
			if (isOk(msg[0])) {

				// split the first Line of every Mail and get the Octets Value
				String msgFirstLine = msg[0];
				String octetsString = msgFirstLine.split(" ")[1];
				long octets = Long.valueOf(octetsString);

				// delete first Message from the List of recieved Mails
				String msg1[] = new String[msg.length - 1];
				for (int i = 0; i < msg1.length; i++) {
					msg1[i] = msg[i + 1];
				}
				 // add Mail to The MailingList of the User
				mailList.add(new Mail(msg1, octets));
				send2ProxyConsole("Empfange Nachricht: " + j + " von POP3-Server");
			} else {
				send2ProxyConsole("Nachricht: " + j + " konnte nicht vom POP3-Server gelesen werden");
			}

		}

		// print out some Stats
		send2ProxyConsole("Empfangene Nachrichten:");
		for (int j = 0; j < mailList.size(); j++) {
			send2ProxyConsole(" -> \t " + (j + 1) + ": " + mailList.get(j).getOctets() + " Octets");
		}

	}

	/**
	 * deleting all Messages from the POP3 Server with where recieved from the Proxy
	 * @param mailList -> all Messages receved by the Proxy (ArrayList<Mail>)
	 */
	private void loeschung(ArrayList<Mail> mailList) {
		send2ProxyConsole("Loeschen erfolgreich abgeholter E-Mails");

		send2ProxyConsole("Loesche " + ammound + " mails.");
		for (int j = 0; j < ammound; j++) {

			sendDele(j + 1);
			send2ProxyConsole("loesche mail: " + (j + 1) + " von POP3-Server");

		}
	}

	/**
	 * Send: USER "username"; to the POP3-Server to set the Username and expect a "+OK USER"
	 * @param username
	 * @throws WrongUsernameException -> thrown if POP3-Server's response is not +OK  
	 */
	private void sendUser(String username) throws WrongUsernameException {
		try {
			// send to server "USER" command
			sendMSG("USER " + username);
			System.out.println("USER " + username);

			String response = receveMSG();
			System.out.println("USER response " + response);

			if (isOk(response)) {
				send2ProxyConsole("User: " + username + " ist vorhanden!");
				user = username;
			} else {
				throw new WrongUsernameException();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Send: PASS "passwort"; to the POP3-Server to set the Password and expect a "+OK PASS"
	 * @param passwort2
	 * @throws WrongPasswordException -> thrown if POP3-Server's response is not +OK 
	 */
	private void sendPasswort(String passwort2) throws WrongPasswordException {
		try {
			// send to server "PASS" command
			sendMSG("PASS " + passwort2);
			String response = receveMSG();
			if (!isOk(response)) {
				throw new WrongPasswordException();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Send: STAT; to the POP3-Server to set the Password and expect a "+OK NUMBER OCTETS" 
	 * Number: ammound of Mails on the POP3-Server
	 * Octets: size of Mails on the POP3-Server in Bytes (Octets)
	 * @return -> String array of: [OK, NUMBER, OCTETS] if sucsess
	 */
	private String[] sendStat() {
		String[] output = new String[3];
		String response = "";
		try {
			// send to server "STAT" command
			sendMSG("STAT");
			response = receveMSG();
			System.out.println("sendStat-response: " + response);
			if (isOk(response)) {
				String flag = response.substring(0, response.indexOf(" "));
				String rest = response.substring(response.indexOf(" ") + 1, response.length());
				String number = rest.substring(0, rest.indexOf(" "));
				String octets = rest.substring(rest.indexOf(" ") + 1, rest.length());

				output[0] = flag;
				output[1] = number;
				output[2] = octets;

			} else {
				output[0] = "-ERR";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return output;
	}

//	private String sendList() {
//		String response = "";
//		String temp;
//		try {
//			// send to server "LIST" command
//			sendMSG("LIST");
//
//			 temp = receveMSG();
//			do {
//				response += temp + "\n";
//				temp = receveMSG();
//			} while (!temp.equals("."));
//			
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return response;
//	}

//	private String[] sendList(int place) {
//		String[] output = new String[3];
//		String response = "";
//		try {
//			// send to server "LIST" command
//			sendMSG("LIST " + place);
//			response = receveMSG();
//			if (isOk(response)) {
//				String flag = response.substring(0, response.indexOf(" "));
//				String rest = response.substring(response.indexOf(" ")+1, response.length());
//				String number = rest.substring(0, rest.indexOf(" "));
//				String octets = rest.substring(rest.indexOf(" ")+1, rest.length());
//				
//				output[0] = flag;
//				output[1] = number;
//				output[2] = octets;
//				
//			}else {
//				output[0] = "-ERR";
//				output[1] = "";
//				output[2] = "";
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return output;
//	}

	/**
	 * Send: RETR "number"; to the POP3-Server to get the Message on Position "number" and expect a "+OK OCTETS \n + MAIL_TEXT + .\n" 
	 * OCTETS: size of the Mail in Bytes (Octets)
	 * MAIL_TEXT: the Text of the Mail - ENDS WITH  ".\n"
	 * @param number
	 * @return String[] of lines of the Message
	 */
	private String[] sendRetr(int number) {
		ArrayList<String> response = new ArrayList<String>();
		try {
			// send to server "LIST" command
			sendMSG("RETR " + number);

			String partResponse;
			partResponse = receveMSG();
			if (partResponse.startsWith("-ERR")) {
				response.add(partResponse);
			} else {
				response.add(partResponse);
				do {
					partResponse = receveMSG();
					response.add(partResponse);

				} while (!partResponse.equals("."));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// build a String[] for Output
		String[] mail = new String[response.size()];
		for (int i = 0; i < mail.length; i++) {
			mail[i] = response.get(i);
		}

		return mail;
	}

	/**
	 * Send: DELE "number"; to the POP3-Server to Delete the Message on Position "number" and expect a "+OK delete" 
	 * not realy delete the message on the POP3 Server - just setting the delete Flag
	 * @param number
	 * @return String -> +OK ""; if sucsess
	 */
	private String sendDele(int number) {
		String response = "";
		try {
			// send to server "LIST" command
			sendMSG("DELE " + number);
			response = receveMSG();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

//	private void sendNoop() throws NoopException {
//		String response = "";
//		try {
//			// send to server "LIST" command
//			sendMSG("NOOP");
//			response = receveMSG();
//			if (!isOk(response)) {
//				throw new NoopException();
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	/**
	 * Send: QUIT; to the POP3-Server to quit the session and signalize the POP3 server to delete the "deleteflaggesd" Mails and expect a "+OK QUIT" 
	 */
	private void sendQuit() {
		try {
			sendMSG("QUIT");
			if (isOk(receveMSG())) {
				send2ProxyConsole("User: " + user + " erfolgreich abgemeldet");
			} else {
				send2ProxyConsole("Fehler beim abmelden von: " + user);
			}

		} catch (IOException e) {
			send2ProxyConsole("Fehler beim abmelden von: " + user);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * sendMSG to server: msg
	 * @param msg 
	 */
	private void sendMSG(String msg) {
		out2Server.print(msg + "\r\n");
		out2Server.flush();
	}

	/**
	 * recieve a message from the Server
	 * @return message
	 * @throws IOException if the Server is not reachable
	 */
	private String receveMSG() throws IOException {
		return inFromServer.readLine();
	}

	/**
	 * check if the input starts with "+OK"
	 * @param input2
	 * @return
	 */
	private boolean isOk(String input2) {
		if (input2 == null) {
			return false;
		} else {
			return input2.startsWith("+OK");
		}
	}

	
	/**
	 * calls the Method send2ProxyConsole(msg, user) from the Pop3ProxyServerSide
	 * @param msg
	 */
	private void send2ProxyConsole(String msg) {
		Pop3ProxyServerSide.send2ProxyConsole(msg, user);
	}

}
