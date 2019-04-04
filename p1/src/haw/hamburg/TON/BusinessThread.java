package haw.hamburg.TON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class BusinessThread extends Thread {

	public boolean clientAlive = false;
	Socket client = null;
	static String passwort = "1234";
	ArrayList<String> commands = new ArrayList<String>();

	public BusinessThread(Socket client) {
		this.client = client;

		commands.add("UPPERCASE");
		commands.add("LOWERCASE");
		commands.add("REVERSE");
		commands.add("SHUTDOWN");
		commands.add("BYE");
	}

	@Override
	public void run() {
		clientAlive = true;
		while (clientAlive) {
			BufferedReader in;
			try {
				in = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));

				String serverResponse = in.readLine();
				
				if (serverResponse.getBytes().length <= 255) {
					int command = -1;
					String tempMsg = "";

					for (int i = 0; i < commands.size(); i++) {
						if (commands.get(i).length() <= serverResponse.length()) {
							if (serverResponse.startsWith(commands.get(i))) {
								tempMsg = serverResponse.substring(commands.get(i).length());
								command = i;
								break;
							}
						}

					}

					switch (command) {
					case 0:

						if (serverResponse.startsWith(commands.get(command) + " ")) {
							if (serverResponse.length() > commands.get(command).length() + 1) {
								commandUPPERCASE(cutBlank(tempMsg));
							} else {
								sendErrorNoArgs();
							}
						} else {
							if (serverResponse.length() == commands.get(command).length()) {
								sendErrorNoArgs();
							} else {
								sendErrorUnknwnCom();
							}
						}

						break;
					case 1:

						if (serverResponse.startsWith(commands.get(command) + " ")) {
							if (serverResponse.length() > commands.get(command).length() + 1) {
								commandLOWERCASE(cutBlank(tempMsg));
							} else {
								sendErrorNoArgs();
							}
						} else {
							if (serverResponse.length() == commands.get(command).length()) {
								sendErrorNoArgs();
							} else {
								sendErrorUnknwnCom();
							}
						}
						break;
					case 2:
						if (serverResponse.startsWith(commands.get(command) + " ")) {
							if (serverResponse.length() > commands.get(command).length() + 1) {
								commandREVERSE(cutBlank(tempMsg));
							} else {
								sendErrorNoArgs();
							}
						} else {
							if (serverResponse.length() == commands.get(command).length()) {
								sendErrorNoArgs();
							} else {
								sendErrorUnknwnCom();
							}
						}

						break;

					case 3:
						if (serverResponse.length() > commands.get(command).length() + 1) {
							commandSHUTDOWN(cutBlank(tempMsg));
						}else {
							sendErrorNoArgs("PASSWORD IS MISSING");
						}
						break;

					case 4:
						if (serverResponse.equals("BYE")) {
							commandBYE(client);
						} else {
							sendError("NO ARGUMENT EXCEPTED");
						}
						break;

					case -1:
						sendErrorUnknwnCom();
						break;
					default:
						break;
					}

				} else {
					sendError("STRING TOO LONG");
				}				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				printOut("CONNECRION LOST");
				clientAlive = false;

			}

		}

	}

	private String cutBlank(String msg) {
		return msg.substring(1);
	}

	private void commandSHUTDOWN(String serverResponse) throws IOException {
		if (serverResponse.equals(passwort)) {
			sendOkay("SHUTDOWN");
			printOut("Server wird gesoppt.");
			System.exit(-1);
		} else {
			sendError("Passwort stimmt nicht!");
		}
	}

	private void commandBYE(Socket client) throws IOException {
		sendOkay("BYE");
		client.close();
	}

	private void commandREVERSE(String serverResponse) throws IOException {
		StringBuilder input1 = new StringBuilder();
		input1.append(serverResponse);
		input1 = input1.reverse();
		sendOkay(input1.toString());
	}

	private void commandLOWERCASE(String serverResponse) throws IOException {
		sendOkay(serverResponse.toLowerCase());
	}

	private void commandUPPERCASE(String serverResponse) throws IOException {
		sendOkay(serverResponse.toUpperCase());
	}

	private void sendErrorNoArgs() throws IOException {
		sendError("SYNTAX ERROR NO ARGUMENT FOUND");
	}

	private void sendErrorNoArgs(String string) throws IOException {
		sendError("SYNTAX ERROR " + string);
	}
	
	private void sendErrorUnknwnCom() throws IOException {
		sendError("UNKNOWN COMMAND");
	}

	private void sendError(String msg) throws IOException {
		String output = "ERROR " + msg;
		send(output);
	}

	private void sendOkay(String msg) throws IOException {
		String output = "OK " + msg;
		send(output);
	}
	
	private void send(String output) throws IOException {
		PrintWriter out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true);
		if (output.getBytes().length < 255) {
			out.println(output);
		} else {
			out.println("ERROR STRING TOO LONG");
		}

	}

	private void printOut(String msg) {
		System.out.println("[SERVER] \"" + msg + "\"");
	}
}
