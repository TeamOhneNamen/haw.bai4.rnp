package haw.hamburg.TON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class Client {
	
	private static final int MAXBIT = 255;
	private static final int MINBIT = 0;
	private static final int PORT = 25615;
	private static final String IP = "localhost";
	
	public static void main(String[] args) throws IOException {
		
		
		Socket connect2server = null;
		try {
			
			// Stelle eine verbindung zum Server her
			connect2server = new Socket(IP, PORT);

			// lese aus BufferedReader die nachricht vom Server(Begrüßung)
			BufferedReader serverInput = new BufferedReader(new InputStreamReader(connect2server.getInputStream(), "UTF-8"), MAXBIT);
			String serverResponse = serverInput.readLine();
			System.out.println(serverResponse);
			
			BufferedReader readFromConsol = new BufferedReader(new InputStreamReader(System.in));
			
			while (true) {
				
				//bis die Konsolennachrich ferig ist(/n(Enter)) auf den Server hören 
				while (!readFromConsol.ready()) {
					// auf den Server hören
					if (serverInput.ready()) {
						
						serverResponse = serverInput.readLine();
						System.out.println(serverResponse);
						if (serverResponse.equals("OK BYE")) {
							readFromConsol.close();
							System.exit(-1);
						} else if (serverResponse.equals("OK SHUTDOWN")) {
							System.exit(-1);
						}else if (serverResponse.equals("ERROR NO MORE CLIENT POSSIBLE")) {
							System.exit(-1);
						}
					}
					
				}
				//von Console lesen
				String befehl = readFromConsol.readLine();
				
				//HAT DIE NACHRICHT DIE RICHTIGE LÄNGE?
				if (befehl.getBytes().length < MAXBIT) {
					if (befehl.getBytes().length > MINBIT) {
						
						//Sende nachricht an server (UTF_8 codiert)
						PrintWriter out = new PrintWriter(new OutputStreamWriter(connect2server.getOutputStream(), StandardCharsets.UTF_8), true);
						out.println(befehl);
					
					} else {
						System.out.println("ERROR STRING TOO SHORT");
					}
				} else {
					System.out.println("ERROR STRING TOO LONG");
				}

			}

		} catch (SocketException e) {
			System.out.println("OK VERBINDUNG GESCHLOSSEN");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
