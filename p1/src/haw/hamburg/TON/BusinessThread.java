package haw.hamburg.TON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class BusinessThread extends Thread{

	public boolean clientAlive = false;
	Socket client = null;
	static String passwort = "1234";
	
	public BusinessThread(Socket client) {
		this.client = client;
	}
	
	@Override
	public void run() {
		clientAlive = true;
		while (clientAlive) {
			BufferedReader in;
			try {
				in = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));
				String serverResponse = in.readLine();
				String command = "";
				String arguments = "";
				
				try {
					command = serverResponse.substring(0, serverResponse.indexOf(" "));
					arguments = serverResponse.substring(serverResponse.indexOf(" ")+1, serverResponse.length());
				
				} catch (Exception e) {
					if (!serverResponse.contains(" ")) {
						
						if (serverResponse.equals("BYE")) {
							
							sendOkay("BYE");
							client.close();
						}else {
							sendError("Argumente Fehlen oder Command nicht vorhanden");
						}
					}
				}
				
				
				if (command.equals("UPPERCASE")) {
					
					if (arguments.isEmpty()) {
						sendError("es konnte leider kein Argument gefunden werden");
					}else {
						sendOkay(arguments.toUpperCase());
					}
					
					
				}else if (command.equals("LOWERCASE")) {

					if (arguments.isEmpty()) {
						sendError("es konnte leider kein Argument gefunden werden");
					}else {
						sendOkay(arguments.toLowerCase());
					}
					
					
				}else if (command.equals("REVERSE")) {
					
					if (arguments.isEmpty()) {
						sendError("es konnte leider kein Argument gefunden werden");
					}else {
						StringBuilder input1 = new StringBuilder(); 
				        input1.append(arguments);
				        input1 = input1.reverse(); 
				        sendOkay(input1.toString());
					}
					
					
				}else if (command.equals("SHUTDOWN")) {

					if (arguments.equals(passwort)) {
						sendOkay("SHUTDOWN");
						System.exit(-1);
					} else {
						sendError("Passwort stimmt nicht!");
					}
					
				}else {
					if (command.equals("BYE")) {
						sendError("es darf keine argumente geben");
					}else {
						sendError("UNKNOWN COMMAND");
					}
				}
				
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				printOut("Verbindung zu Client verloren");
				clientAlive = false;
				
			}
			
		}
		
	}

	
	
	private void sendError(String msg) throws IOException {
		PrintWriter out = new PrintWriter(client.getOutputStream(), true);
		out.println("ERROR \"" +  msg + "\"");
	}
	
	private void sendOkay(String msg) throws IOException {
		PrintWriter out = new PrintWriter(client.getOutputStream(), true);
		out.println("OK \"" + msg + "\"");
	}
	
	private void printOut(String msg) {
		System.out.println("[SERVER] \"" + msg + "\"");
	}
}
