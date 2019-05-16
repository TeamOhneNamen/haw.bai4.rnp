package haw.hamburg.TON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import haw.hamburg.TON.proxy.clinetSide.Pop3ProxyClientSide;

/**
 * 
 * @author Ferdinand Trendelenburg AND Thorben Schomacker
 *
 * Manage the Input in the Console from the User:
 * commands: 
 * CLINETS = shows how many Client-Slost are left.
 * QUIT = stop The server
 *
 */
public class ServerCommandLineThread extends Thread {

	BufferedReader commandIn = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
	String input;
	
	/**
	 * runns the Thread to overwach the Console
	 */
	@Override
	public void run() {
		
		Pop3ProxyServer.send2ProxyConsole("CommandLineListener Gestartet");
		while (Pop3ProxyServer.serverAlive) {
			try {
				input = commandIn.readLine();
				Pop3ProxyServer.send2ProxyConsole("Verarbeite: " + input + "...");
				handleCommand(input);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * handle Command typed in the Console
	 * @param input -> Command
	 */
	private void handleCommand(String input) {
		switch (input) {
		case "QUIT":
			Pop3ProxyServer.send2ProxyConsole("Beende Server");
			System.exit(-1);
			break;
		case "CLINETS":
			Pop3ProxyServer.send2ProxyConsole((Pop3ProxyServer.maxVerbindungen - Pop3ProxyClientSide.buisThreadList.manyConnections()) + " Clients frei");
			break;
			
		default:
			Pop3ProxyServer.send2ProxyConsole("Command konnte nicht gefunden werden");
			break;
		}
	}

}
