package haw.hamburg.TON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import haw.hamburg.TON.proxy.serverSide.Pop3ProxyServerSide;

public class ServerCommandLineThread extends Thread {

	BufferedReader commandIn = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
	String input;
	
	@Override
	public void run() {
		
		Pop3ProxyServerSide.send2ProxyConsole("CommandLineListener Gestartet");
		while (Pop3ProxyServer.serverAlive) {
			try {
				input = commandIn.readLine();
				Pop3ProxyServerSide.send2ProxyConsole("Verarbeite: " + input + "...");
				handleCommand(input);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void handleCommand(String input) {
		switch (input) {
		case "QUIT":
			Pop3ProxyServerSide.send2ProxyConsole("Beende Server");
			System.exit(-1);
			break;
			
		default:
			Pop3ProxyServerSide.send2ProxyConsole("Command konnte nicht gefunden werden");
			break;
		}
	}

}