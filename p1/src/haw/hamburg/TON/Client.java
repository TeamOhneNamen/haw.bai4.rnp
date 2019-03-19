package haw.hamburg.TON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

	public static void main(String[] args) throws IOException {
		Socket server = null;
			try {
				server = new Socket ("192.168.178.23", 25615);
				BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
				String serverResponse = in.readLine();
				System.out.println("[Server sagt] " +serverResponse);
				
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
			
			while (true) {
				
				Scanner sc = new Scanner(System.in); 
				String befehl = sc.nextLine();
				
				if (befehl == "bye") {
					server.close();
					sc.close();
				}
				
				if (befehl.length()<20 && befehl.length()>0 ) {
					
					PrintWriter out = new PrintWriter(server.getOutputStream(), true);
					out.println(befehl);
					BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
					String serverResponse = in.readLine();
					if (serverResponse.equals("OK SHUTDOWN")) {
						System.exit(-1);
					}
					System.out.println("[Server sagt] " +serverResponse);
					
				}
				
				
				
			}
			
	}
	
}
