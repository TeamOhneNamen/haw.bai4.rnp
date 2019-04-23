package haw.hamburg.TON;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Server {

	
	public static boolean shutdowned = false;
	
	// port festlegen
	
	static ServerSocket sServer;
	private final static int MAXVERBUNDUNGEN = 3;
	private final static int PORT = 25615;
	
	static PrintWriter out;
	
	static BusinessThreadList buisThreadList = new BusinessThreadList();
	
	//Constructor 
	public Server() {
	}
	
	public static void main(String[] args) throws UnsupportedEncodingException, IOException {

		for (int i = 0; i < MAXVERBUNDUNGEN; i++) {
			buisThreadList.add(new BusinessThread(null));
		}
		
		
		// versuche den Server auf dem port zu starten
		try {
			sServer = new ServerSocket(PORT);
			printOut("Server wurde auf Port " + PORT + " gestartet!");
		} catch (IOException e) {
			printOut("error to bind Port: " +PORT);
			System.exit(-1);
		}
		
		// warte auf eingehende VErbindungen
		while (!shutdowned) {
			
			try {
				
				//blockiert bis neue Verbindung eintrifft
				Socket client = sServer.accept(); // fehler
				
				int freeThread = buisThreadList.getFree();
				
				
				if (freeThread!=-1) {
					
					BusinessThread tempClient = new BusinessThread(client);
					
					//startet einen behandelnden Thread
					buisThreadList.set(freeThread, tempClient) ;
					tempClient.start();

					//sende Clienten best�tigung über die Verbindung
					sendOkay("Verbindung zu " + sServer.getInetAddress().getHostAddress() + ":" + sServer.getLocalPort() + " hergestellt!", client);

					
					//Serverausgaben
					printOut("Client wurde mit Thread " + freeThread +" verbunden!");
//					printOut(buisThreadList.manyConnections() + "/" +maxVerbindungen);
					
				}else {
					
					//sende Clienten Fehler �ber die Verbindung
					sendError("NO MORE CLIENT POSSIBLE", client);
					client.close();
					
					//Serverausgaben
					printOut("Client wurde nicht verbunden!");
					
				}
				
			} catch (IOException e) {
				printOut("Ein Fehler ist aufgetreten: " + e.getMessage());
			}

		}
		buisThreadList.joinAll();
		printOut("Server wurde gestoppt.");

	}
	
	public static void close() throws IOException {
		shutdowned = true;
		//verhindert weiteres verbinden auf den Server
		sServer.close();
		printOut("Server wird gestoppt.");
	}
	
	private static void sendError(String msg, Socket client) throws IOException {
		String output = "ERROR " + msg;
		send(output, client);
	}

	private static void sendOkay(String msg, Socket client) throws IOException {
		String output = "OK " + msg;
		send(output, client);
	}
	
	private static void send(String output, Socket client) throws IOException {
		out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true);
		if (output.getBytes().length < 255) {
			out.println(output);
		} else {
			out.println("ERROR STRING TOO LONG");
		}

	}
	
	public static void printOut(String msg) {
		System.out.println("[SERVER] \"" + msg + "\"");
	}
	
}
