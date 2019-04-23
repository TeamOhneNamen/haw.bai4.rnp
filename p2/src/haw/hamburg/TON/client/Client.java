package haw.hamburg.TON.client;

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
	private static final int PORT2 = 1300;
	private static final String IP = "localhost";

	public static void main(String[] args) throws IOException {

		Socket connect2server = null;
		try {
			connect2server = new Socket(IP, PORT2);

			BufferedReader readFromConsol = new BufferedReader(new InputStreamReader(System.in));

			BufferedReader serverInput = new BufferedReader(
					new InputStreamReader(connect2server.getInputStream(), "UTF-8"));
			String serverResponse;
			
			PrintWriter out = new PrintWriter(
					new OutputStreamWriter(connect2server.getOutputStream(), StandardCharsets.UTF_8), true);			
			
			while (true) {

				while (!readFromConsol.ready()) {
					if (serverInput.ready()) {
						serverResponse = serverInput.readLine();
						System.out.println(serverResponse);
					}

				}
				String befehl = readFromConsol.readLine();

				out.println(befehl);

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
