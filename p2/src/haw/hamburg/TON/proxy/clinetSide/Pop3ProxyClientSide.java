package haw.hamburg.TON.proxy.clinetSide;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import haw.hamburg.TON.Pop3ProxyServer;

/**
 * 
 * @author Ferdinand Trendelenburg AND Thorben Schomacker
 * Pop3ProxyClientSide starts the ClientSide-Routine
 *
 */
public class Pop3ProxyClientSide extends Thread {

	private static int port;
	private static ServerSocket server;

	// erstelle eine neue "BusinessThreadList"(BTL) zum verwalten der ClientThreads
	public static BusinessThreadList buisThreadList = new BusinessThreadList();
	
	/**
	 * Constructor
	 * @param emailClinetPort = set the Port to bind to start the Server
	 */
	public Pop3ProxyClientSide(int emailClinetPort) {
		port = emailClinetPort;
	}

	/**
	 * starts The ServerSide Thread:
	 * Starts a Server so Clients can connect with
	 * if new Connection: start a new BusinessThread
	 * and put it in the BusinessThreadList (BLT)
	 * if no Slot free in the BTL: refuse Connection.
	 */
	@Override
	public void run() {

		Pop3ProxyServer.send2ProxyConsole("Pop3ProxyClientSide gestartet");

		try {

			for (int i = 0; i < Pop3ProxyServer.maxVerbindungen; i++) {
				buisThreadList.add(new RoutineThreadClientSide(new Socket(), 0));
			}
			server = new ServerSocket(port);
			send2ProxyConsole("Server wurde auf port: " + port + " gestartet.");

			while (Pop3ProxyServer.serverAlive) {

				send2ProxyConsole("Warte auf eingehende Verbundungen...");

				Socket tempClient = server.accept();

				int free = buisThreadList.getFree();
				if (free != -1) {
					send2ProxyConsole("Neue Verbindung mit Client: " + tempClient.getInetAddress().getHostAddress()
							+ ":" + tempClient.getPort() + " eingegangen.");
					RoutineThreadClientSide btc = new RoutineThreadClientSide(tempClient, Pop3ProxyServer.timeout);
					buisThreadList.set(free, btc);
					btc.start();
				} else {
					send2ProxyConsole("client " + tempClient.getInetAddress().getHostAddress()
							+ " kann nicht verbinden (TO MANNY CONNECTIONS)");
					PrintWriter out2Client = new PrintWriter(
							new OutputStreamWriter(tempClient.getOutputStream(), StandardCharsets.UTF_8), true);
					out2Client.println("-ERR TO MANY CONNECTION");
				}

			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Sends a Message to the Consol in Format: "[ProxyServer |ClientSide|]: " + Message
	 * @param msg = Message
	 */
	public static void send2ProxyConsole(String msg) {
		System.out.println("[ProxyServer <ClientSide>]: " + msg);
	}

}
