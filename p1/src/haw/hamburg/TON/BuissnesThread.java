package haw.hamburg.TON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class BuissnesThread extends Thread{

	Socket client;
	Server server;
	static String passwort = "LOLOKOPTER";
	
	public BuissnesThread(Socket client) {
		this.client = client;
	}
	
	@Override
	public void run() {
		
		while (true) {
			BufferedReader in;
			try {
				in = new BufferedReader(new InputStreamReader(client.getInputStream()));
				String serverResponse = in.readLine();
				System.out.println("[Client fragt] " +serverResponse);
				String command = serverResponse.substring(0, serverResponse.indexOf(" "));
				String arguments = serverResponse.substring(serverResponse.indexOf(" "), serverResponse.length());
				
				if (command.equals("UPPER")) {

					PrintWriter out = new PrintWriter(client.getOutputStream(), true);
					out.println("OK " + arguments.toUpperCase());
					
				}else if (command.equals("LOWER")) {

					PrintWriter out = new PrintWriter(client.getOutputStream(), true);
					out.println("OK " + arguments.toLowerCase());
					
				}else if (command.equals("REVERSE")) {

					PrintWriter out = new PrintWriter(client.getOutputStream(), true);
					StringBuilder input1 = new StringBuilder(); 
			        input1.append(arguments);
			        input1 = input1.reverse(); 
					out.println("OK " + input1);
					
				}else if (command.equals("SHUTDOWN")) {

					PrintWriter out = new PrintWriter(client.getOutputStream(), true); 
					out.println("OK SHUTDOWN");
//					throw new NewException();
					
				}
				
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}

	
}

//public class NewException() extends Exception {
//	
//};
