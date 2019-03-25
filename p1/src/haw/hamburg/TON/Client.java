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
		Socket connect2server = null;
		try {
			connect2server = new Socket("192.168.178.23", 25615);
			BufferedReader in = new BufferedReader(new InputStreamReader(connect2server.getInputStream()));
			String serverResponse = in.readLine();
			System.out.println(serverResponse);

			while (true) {

				Scanner sc = new Scanner(System.in);
				String befehl = sc.nextLine();

				if (befehl == "BYE") {
					PrintWriter out = new PrintWriter(connect2server.getOutputStream(), true);
					out.println(befehl);
					sc.close();
					System.exit(-1);
					
				}

				if (befehl.length() < 255 && befehl.length() > 0) {

					PrintWriter out = new PrintWriter(connect2server.getOutputStream(), true);
					out.println(befehl);
					
					serverResponse = in.readLine();
					System.out.println(serverResponse);
					if (serverResponse.equals("OK \"SHUTDOWN\"")) {
						System.exit(-1);
					}
				}

			}

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
