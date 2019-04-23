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
	private static final int TIMEOUT = 30000;
	
	// erstelle eine neue "BusinessThreadList"(BTL) zum verwalten der ClientThreads
	static BusinessThreadList buisThreadList = new BusinessThreadList();
	
	//Constructor 
	public Server() {
	}
	
	public static void main(String[] args) throws UnsupportedEncodingException, IOException {

		// füge in die BTL so viele leere BusinessThreads(BT) ein, wie es verbindungen geben soll
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
		
		// handle solange neue, eingehende Verbindungen, bis der Server den Zustand "shutdowned" hat
		while (!shutdowned) {
			
			try {
				
				//blockiert bis neue Verbindung eintrifft
				Socket client = sServer.accept();
				
				// suche einen freien Thread aus der BTL
				int freeThread = buisThreadList.getFree();
				
				// wenn es einen freien Thread gibt:
				if (freeThread!=-1) {
					
					//startet einen behandelnden Thread
					BusinessThread tempClient = new BusinessThread(client);
					buisThreadList.set(freeThread, tempClient) ;
					tempClient.start();

					//sende Clienten bestaetigung über die Verbindung
					sendOkay("Verbindung zu " + sServer.getInetAddress().getHostAddress() + ":" + sServer.getLocalPort() + " hergestellt!", client);

					//Serverausgaben
					printOut("Client wurde mit Thread " + freeThread +" verbunden!");

				// wenn es keinen freien Thread gibt:
				}else {
					
					//sende Clienten Fehler ueber die Verbindung
					sendError("NO MORE CLIENT POSSIBLE", client);
					client.close();
					
					//Serverausgaben
					printOut("Client wurde nicht verbunden!");
					
				}
				
			} catch (IOException e) {
				printOut("Ein Fehler ist aufgetreten: " + e.getMessage());
			}

		}
		
		// wenn der Server den status "shutdowned" hat, so warte, 
		// bis alle BT aus der BTL gestoppt wurden
		buisThreadList.joinAll();
		printOut("Server wurde gestoppt.");

	}
	
	//schließt den Server(setzt den status des Servers auf "shutdowned")
	public static void close() throws IOException {
		//gebe allen Verbundenen clienten einen SoTimeout(default=30sec)
		buisThreadList.setAllSoTimeout(TIMEOUT);
		shutdowned = true;
		//verhindert weiteres verbinden auf den Server
		sServer.close();
		
		printOut("Server wird gestoppt.");
	}
	
	//errorAusgabe
	private static void sendError(String msg, Socket client) throws IOException {
		String output = "ERROR " + msg;
		send(output, client);
	}

	//okAusgabe
	private static void sendOkay(String msg, Socket client) throws IOException {
		String output = "OK " + msg;
		send(output, client);
	}
	
	//send an Client
	private static void send(String output, Socket client) throws IOException {
		out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true);
		if (output.getBytes().length < 255) {
			out.println(output);
		} else {
			out.println("ERROR STRING TOO LONG");
		}

	}
	
	// sende an die Console
	public static void printOut(String msg) {
		System.out.println("[SERVER] \"" + msg + "\"");
	}
	
}
