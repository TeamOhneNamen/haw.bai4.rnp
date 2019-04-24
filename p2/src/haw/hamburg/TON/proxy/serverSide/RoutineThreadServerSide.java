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
import haw.hamburg.TON.Exceptions.NoopException;
import haw.hamburg.TON.Exceptions.WrongPasswordException;
import haw.hamburg.TON.Exceptions.WrongUsernameException;

public class RoutineThreadServerSide extends Thread {


	private static boolean clientAlive;
	private static int ZEITABSTAND;

	private BufferedReader inFromServer;
	private PrintWriter out2Server;

	int ammound = 0;
	
	int[] mailNumbers;
	
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
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		clientAlive = true;
		while (clientAlive) {

			for (int i = 0; i < Pop3ProxyServer.userList.size(); i++) {
				String user = Pop3ProxyServer.userList.get(i).getUsername();
				String pass = Pop3ProxyServer.userList.get(i).getPasswort();

				ArrayList<Mail> mailList = Pop3ProxyServer.userList.get(i).getMailingQueue();

				anmeldung(user, pass);
				abholung(mailList);
				loeschung(mailList);

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
		mailNumbers = getWichMails(ammound);
		
		send2ProxyConsole("Empfange " + ammound + " mails.");
		for (int j = 0; j < mailNumbers.length; j++) {
			
			/**
			 * Zerteile die erste Zeile in seine Attribute
			 */
			String msg = sendRetr(mailNumbers[j]);
			if (isOk(msg)) {
				String msgFirstLine = msg.substring(0, msg.indexOf("\n"));
				String msgFirstLineWithountState = msgFirstLine.substring(msgFirstLine.indexOf(" ")+1, msgFirstLine.length());
				String numberString = msgFirstLineWithountState.substring(0, msgFirstLineWithountState.indexOf(" "));
				
				String octetsString = msgFirstLineWithountState.substring(msgFirstLineWithountState.indexOf(" ")+1, msgFirstLineWithountState.length());
				int number = Integer.valueOf(numberString);
				int octets = Integer.valueOf(octetsString);
				
				// Loesche die erste Zeile von der nachricht
				msg = msg.substring(msg.indexOf("\n")+1, msg.length());
				
				mailList.add(new Mail(msg, octets, number));
				send2ProxyConsole("Empfange Nachricht: " + number + " von POP3-Server");
			}else {
				send2ProxyConsole("Nachricht: " + mailNumbers[j] + " konnte nicht vom POP3-Server gelesen werden");
			}
			
		}
		
		send2ProxyConsole("Empfangene Nachrichten:");
		for (int j = 0; j < mailList.size(); j++) {
			send2ProxyConsole(" -> \t " + mailNumbers[j] + ": " + mailList.get(j).getOctets() + " Octets");
		}
		
	}

	

	private void loeschung(ArrayList<Mail> mailList) {
		send2ProxyConsole("Loeschen erfolgreich abgeholter E-Mails");
		
		send2ProxyConsole("Loesche " + ammound + " mails.");
		for (int j = 0; j < ammound; j++) {

			sendDele(j);
			send2ProxyConsole("loesche mail: " + mailNumbers[j] + " von POP3-Server");
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
	
	private int[] getWichMails(int lenght) {
		int[] listOfNumber = new int[lenght];
		String list = sendList();
		if (isOk(list)) {
			String[] zeilen = list.split("\n");
			for (int i = 1; i < zeilen.length; i++) {
				String mailNumberString = zeilen[i].substring(0, zeilen[i].indexOf(" "));
				int mailNumber = Integer.valueOf(mailNumberString);
				listOfNumber[i-1] = mailNumber;
			}
		}
		return listOfNumber;
	}
	
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
	
	private String sendList() {
		String response = "";
		String temp;
		try {
			// send to server "LIST" command
			sendMSG("LIST");

			 temp = receveMSG();
			do {
				response += temp + "\n";
				temp = receveMSG();
			} while (!temp.equals("."));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}
	
	private String[] sendList(int place) {
		String[] output = new String[3];
		String response = "";
		try {
			// send to server "LIST" command
			sendMSG("LIST " + place);
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
				output[1] = "";
				output[2] = "";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return output;
	}
	
	private String sendRetr(int number) {
		String response = "";
		try {
			// send to server "LIST" command
			sendMSG("RETR " + number);
			
			String partResponse;
			partResponse = receveMSG();
			if (partResponse.startsWith("-ERR")) {
				response = partResponse;
			}else {
				response = partResponse + "\n";
				do {
					partResponse = receveMSG();
					response = response + partResponse + "\n";
					
				} while (!partResponse.equals("."));
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
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
	
	private void sendNoop() throws NoopException {
		String response = "";
		try {
			// send to server "LIST" command
			sendMSG("NOOP");
			response = receveMSG();
			if (!isOk(response)) {
				throw new NoopException();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String sendQuit() throws Exception {
		String response = "";
		try {
			// send to server "LIST" command
			sendMSG("QUIT");
			response = receveMSG();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}


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
