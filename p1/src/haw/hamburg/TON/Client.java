package haw.hamburg.TON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {

	public static void main(String[] args) throws IOException {
		Socket connect2server = null;
		try {
			connect2server = new Socket("localhost", 25615);
			BufferedReader in = new BufferedReader(new InputStreamReader(connect2server.getInputStream(), "UTF-8"));
			String serverResponse = in.readLine();
			System.out.println(serverResponse);

			while (true) {
				
				Scanner sc = new Scanner(System.in);
				String befehl = sc.nextLine();

				if (befehl.getBytes().length < 255 && befehl.getBytes().length > 0) {

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
					System.out.println("ERROR STRING TOO LONG");
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
