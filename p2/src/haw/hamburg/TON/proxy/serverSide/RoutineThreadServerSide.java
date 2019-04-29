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
//import haw.hamburg.TON.Exceptions.NoopException;
import haw.hamburg.TON.Exceptions.WrongPasswordException;
import haw.hamburg.TON.Exceptions.WrongUsernameException;
//import haw.hamburg.TON.proxy.clinetSide.Pop3ProxyClientSide;

public class RoutineThreadServerSide extends Thread {


	private static boolean clientAlive;
	private static int ZEITABSTAND;

	private BufferedReader inFromServer;
	private PrintWriter out2Server;

	String user;
	
	int ammound = 0;
	
	Socket server;

	String input;

	public RoutineThreadServerSide(int zeitabstand, Socket tempServer) throws IOException {
		server = tempServer;
		ZEITABSTAND = zeitabstand;
		inFromServer = new BufferedReader(new InputStreamReader(server.getInputStream(), StandardCharsets.UTF_8));
		out2Server = new PrintWriter(new OutputStreamWriter(server.getOutputStream(), StandardCharsets.UTF_8), true);

	}

	@Override
	public void run() {

		try {
			Pop3ProxyServerSide.send2ProxyConsole(receveMSG());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		clientAlive = true;
		while (clientAlive) {

			for (int i = 0; i < Pop3ProxyServer.userList.size(); i++) {
				String user = Pop3ProxyServer.userList.get(i).getUsername();
				String pass = Pop3ProxyServer.userList.get(i).getPasswort();

				ArrayList<Mail> mailList = Pop3ProxyServer.userList.get(i).getMailingQueue();

				Pop3ProxyServerSide.send2ProxyConsole("--------ANMELDEN----------");
				anmeldung(user, pass);
				Pop3ProxyServerSide.send2ProxyConsole("--------ARBEITEN----------");
				abholung(mailList);
				Pop3ProxyServerSide.send2ProxyConsole("--------LOESCHEN----------");
				loeschung(mailList);
				Pop3ProxyServerSide.send2ProxyConsole("--------ABMELDEN----------");
//				sendQuit();
				Pop3ProxyServerSide.send2ProxyConsole("----------ENDE------------");
				
				

//				for (int j = 0; j < mailList.size() ; j++) {
//					System.out.println(mailList.get(j).getMsg());
//				}
				
			}
			try {
				Thread.sleep(ZEITABSTAND);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	private void anmeldung(String user, String pass) {

		send2ProxyConsole("Anmeldung bei dem jeweiligen POP3-Server mit den gueltigen Account-Daten");
		
		try {
			
			sendUser(user);
			sendPasswort(pass);

		} catch (WrongPasswordException e) {
			Pop3ProxyServerSide.send2ProxyConsole("Falsches Passwort");
		} catch (WrongUsernameException e) {
			Pop3ProxyServerSide.send2ProxyConsole("Falscher Username");
		}

		send2ProxyConsole("User: " + user + " erfolgreich angemeldet");

	}

	private void abholung(ArrayList<Mail> mailList) {
		send2ProxyConsole("Abholung aller fuer diesen Account eingetroffenen E-Mails");

		String ergString[] = sendStat();
		ammound = Integer.parseInt(ergString[1]);
		
		send2ProxyConsole("Empfange " + ammound + " mails.");
		for (int j = 1; j < ammound+1; j++) {
			
			/**
			 * Zerteile die erste Zeile in seine Attribute
			 */
			String[] msg = sendRetr(j);
			if (isOk(msg[0])) {
				String msgFirstLine = msg[0];
				String msgFirstLineWithountState = msgFirstLine.substring(msgFirstLine.indexOf(" ")+1, msgFirstLine.length()-1);
				
				String octetsString = msgFirstLineWithountState.substring(msgFirstLineWithountState.indexOf(" ")+1, msgFirstLineWithountState.length());
				int octets = Integer.valueOf(octetsString);
				
				// Loesche die erste Zeile von der nachricht
				String msg1[] = new String[msg.length-1];
				for (int i = 0; i < msg1.length; i++) {
					msg1[i] = msg[i+1];
				}
				
				
				
				mailList.add(new Mail(msg1, octets));
				send2ProxyConsole("Empfange Nachricht: " + j + " von POP3-Server");
			}else {
				send2ProxyConsole("Nachricht: " + j + " konnte nicht vom POP3-Server gelesen werden");
			}
			
		}
		
		send2ProxyConsole("Empfangene Nachrichten:");
		for (int j = 0; j < mailList.size(); j++) {
			send2ProxyConsole(" -> \t " + (j+1) + ": " + mailList.get(j).getOctets() + " Octets");
		}
		
	}


	private void loeschung(ArrayList<Mail> mailList) {
		send2ProxyConsole("Loeschen erfolgreich abgeholter E-Mails");
		
		send2ProxyConsole("Loesche " + ammound + " mails.");
		for (int j = 0; j < ammound; j++) {

			sendDele(j);
			send2ProxyConsole("loesche mail: " + (j+1) + " von POP3-Server");
		}
		
	}

	private void sendUser(String username) throws WrongUsernameException {

		try {
			// send to server "USER" command
			sendMSG("USER " + username);
			String response = receveMSG();
			if (!isOk(response)) {
				
				throw new WrongUsernameException();
			}else {
				send2ProxyConsole("User: " + username + " ist vorhanden!");
				user = username;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

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
	
//	private int[] getWichMails(int lenght) {
//		int[] listOfNumber = new int[lenght];
//		String list = sendList();
//		if (isOk(list)) {
//			String[] zeilen = list.split("\n");
//			for (int i = 1; i < zeilen.length; i++) {
//				String mailNumberString = zeilen[i].substring(0, zeilen[i].indexOf(" "));
//				int mailNumber = Integer.valueOf(mailNumberString);
//				listOfNumber[i-1] = mailNumber;
//			}
//		}
//		return listOfNumber;
//	}
	
	private String[] sendStat() {
		String[] output = new String[3];
		String response = "";
		try {
			// send to server "STAT" command
			sendMSG("STAT");
			response = receveMSG();
			if (isOk(response)) {
				String flag = response.substring(0, response.indexOf(" "));
				String rest = response.substring(response.indexOf(" ")+1, response.length());
				String number = rest.substring(0, rest.indexOf(" "));
				String octets = rest.substring(rest.indexOf(" ")+1, rest.length());
				
				output[0] = flag;
				output[1] = number;
				output[2] = octets;
				
			}else {
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
	
	private String[] sendRetr(int number) {
		ArrayList<String> response = new ArrayList<String>();
		try {
			// send to server "LIST" command
			sendMSG("RETR " + number);
			
			String partResponse;
			partResponse = receveMSG();
			if (partResponse.startsWith("-ERR")) {
				response.add(partResponse);
			}else {
				response.add(partResponse);
				do {
					partResponse = receveMSG();
					response.add(partResponse);
					
				} while (!partResponse.equals("."));
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		String[] mail = new String[response.size()];
		for (int i = 0; i < mail.length; i++) {
			mail[i] = response.get(i);
		}
		
//		for (int i = 0; i < mail.length; i++) {
//			System.out.println(mail[i]);
//		}
		
		return mail;
	}
	
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
	
//	private void sendQuit() {
//			// send to server "LIST" command
//			
//			try {
//				sendMSG("QUIT");
//				if(isOk(receveMSG())) {
//					Pop3ProxyClientSide.send2ProxyConsole("User: " + user + " erfolgreich abgemeldet"); 
//				}else {
//					Pop3ProxyClientSide.send2ProxyConsole("Fehler beim abmelden von: " + user); 
//				}
//			} catch (IOException e) {
//				Pop3ProxyClientSide.send2ProxyConsole("Fehler beim abmelden von: " + user); 
//			} catch (NullPointerException e) {
//				Pop3ProxyClientSide.send2ProxyConsole("Nullpointer Exception");
//			}
//		
//	}


	private void sendMSG(String msg) {
		out2Server.println(msg);
	}
	
	private String receveMSG() throws IOException {
		return inFromServer.readLine();
	}

	private boolean isOk(String input2) {
		return input2.substring(0, 3).toUpperCase().equals("+OK");
	}

	private void send2ProxyConsole(String msg) {
		Pop3ProxyServerSide.send2ProxyConsole(msg);
	}
	
}
