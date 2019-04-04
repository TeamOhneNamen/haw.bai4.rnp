package haw.hamburg.TON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {

	public static Scanner sc = new Scanner(System.in);
	
	private static final int MAXBIT = 255;
	private static final int MINBIT = 0;
	private static final int PORT = 25615;
	private static final String IP = "localhost";
	
	public static void main(String[] args) throws IOException {
		
		Socket connect2server = null;
		try {
			connect2server = new Socket(IP, PORT);
			
			BufferedReader in = new BufferedReader(new InputStreamReader(connect2server.getInputStream(), "UTF-8"));
			String serverResponse = in.readLine();
			System.out.println(serverResponse);
			
			while (true) {
				
				while (!sc.hasNext()) {
					
					if (Server.shutdowned) {
						System.exit(-1);
					}
					
				}
				String befehl = sc.nextLine(); // fehler
				

				if (befehl.getBytes().length < MAXBIT) {
					if (befehl.getBytes().length > MINBIT) {
						PrintWriter out = new PrintWriter(new OutputStreamWriter(connect2server.getOutputStream(), StandardCharsets.UTF_8), true);
						out.println(befehl);
					
						
						serverResponse = in.readLine();
						System.out.println(serverResponse);
						if (serverResponse.equals("OK BYE")) {
							sc.close();
							System.exit(-1);
						} else if (serverResponse.equals("OK SHUTDOWN")) {
							System.exit(-1);
						}else if (serverResponse.equals("ERROR NO MORE CLIENT POSSIBLE")) {
							System.exit(-1);
						}
					} else {
						System.out.println("ERROR STRING TOO SHORT");
					}
				} else {
					System.out.println("ERROR STRING TOO LONG");
				}

			}

		} catch (ConnectException e) {
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void closeScanner() {
		sc.close();
	}
	
}
