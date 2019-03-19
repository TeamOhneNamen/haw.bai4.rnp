package haw.hamburg.TON;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

	static int port = 25615;
	static ServerSocket sServer;
	static int verbindungen;
	static int moeglicheVerbindungen;
	
	
	static ArrayList<Socket> clientList = new ArrayList<Socket>();
	
	public Server() {
		port = 25615;
	}

	public static void main(String[] args) {

		try {
			sServer = new ServerSocket(port);

		} catch (IOException e) {
			System.out.println("[Server] error to bind Port: " + port);
			System.exit(-1);
		}

		while (true) {

			try {

				System.out.println("[Server] wartet auf verbindung...");
				Socket client = sServer.accept();
				clientList.add(client);
				PrintWriter out = new PrintWriter(client.getOutputStream(), true);
				out.println("Verbindung hergestellt!");
				System.out.println("[Server] Client wurde verbunden!");
				
				BuissnesThread buisThread = new BuissnesThread(client);
				buisThread.start();
				
			} catch (IOException e) {
				// TODO
				System.out.println("Ein Fehler ist aufgetreten ");
				System.exit(-1);
			}

		}

	}
	
	public void shutdown() {
		System.exit(-1);

	}

}
