package haw.hamburg.TON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class BusinessThread extends Thread {
	
	boolean shutdowned = false;
	public boolean clientAlive = false;
	Socket client = null;
	static String passwort = "1234";
	ArrayList<String> commands = new ArrayList<String>();

	public BusinessThread(Socket client) {
		this.client = client;

		// hinzufügen aller Commands
		commands.add("UPPERCASE");
		commands.add("LOWERCASE");
		commands.add("REVERSE");
		commands.add("SHUTDOWN");
		commands.add("BYE");
	}

	@Override
	public void run() {
		// solange der Server status nicht "shutdowned" ist arbeite!
		if (!Server.shutdowned) {
			clientAlive = true;
			while (clientAlive) {
				BufferedReader in;
				try {

					in = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));

					String serverResponse = in.readLine();
					
					// laenge der nachricht
					if (serverResponse.getBytes().length <= 255) {
						if (serverResponse.getBytes().length > 0) {
							
							// default no msg found
							int command = -1;
							String tempMsg = "";

							// finde den Command
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
							// UPPERCASE
							case 0:

								if (serverResponse.startsWith(commands.get(command) + " ")) {
									if (serverResponse.length() > commands.get(command).length() + 1) {
										if (!cutBlank(tempMsg).toUpperCase().equals("SHUTDOWN")) {
											commandUPPERCASE(cutBlank(tempMsg));
										}
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
							// LOWERCASE
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
							// REVERSE
							case 2:
								if (serverResponse.startsWith(commands.get(command) + " ")) {
									if (serverResponse.length() > commands.get(command).length() + 1) {
										if (!cutBlank(tempMsg).equals("NWODTUHS")) {
											commandREVERSE(cutBlank(tempMsg));
										}
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
							// SHUTDOWN
							case 3:
								if (serverResponse.length() > commands.get(command).length() + 1) {
									commandSHUTDOWN(cutBlank(tempMsg));
								} else {
									sendErrorNoArgs("PASSWORD IS MISSING");
								}
								break;
							// BYE
							case 4:
								if (serverResponse.equals("BYE")) {
									commandBYE(client);
								} else {
									sendError("NO ARGUMENT EXCEPTED");
								}
								break;

							// kein Command erkannt
							case -1:
								sendErrorUnknwnCom();
								break;
							default:
								break;
							}
						} else {
							sendError("STRING TOO SHORT");
						}
					} else {
						sendError("STRING TOO LONG");
					}

					// -------------------------------------- EXCEPTIONS
					// -------------------------------------
					// Kommt nach X Sekunden Keine Nachricht mehr:
				} catch (SocketTimeoutException e) {
					if (Server.shutdowned) {
						try {
							sendOkay("SHUTDOWN");
							Server.sServer.close();
							client.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}

						clientAlive = false;
					}

					// Nachricht vom Reader zu lang
				} catch (OutOfMemoryError e) {

					Server.printOut("MESSAGE TOO LONG [client]");
					Server.printOut(e.getLocalizedMessage());
					clientAlive = false;

				} catch (NullPointerException e) {

					Thread.currentThread().interrupt();
					printOut("CONNECTION LOST");
					clientAlive = false;

				} catch (IOException e) {

					printOut("CONNECTION LOST");
					clientAlive = false;

				} catch (Exception e) {
					printOut("ERROR: " + e);
				}

			}
		} else {
			try {
				client.close();
				clientAlive = false;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	// getter Client
	public Socket getClient() {
		return client;
	}

	// schneidet das Blanc nach nach dem Command ab
	private String cutBlank(String msg) {
		return msg.substring(1);
	}

	// ------------------ METHODEN FUER COMMANDS -----------------------

	// commandSHUTDOWN
	private void commandSHUTDOWN(String serverResponse) throws IOException {
		if (serverResponse.equals(passwort)) {
			sendOkay("Server IS SHUTDOWNING");
			Server.close();
		} else {
			sendError("NOT THE RIGHT PASSWORD");
		}
	}

	// commandBYE
	private void commandBYE(Socket client) throws IOException {
		sendOkay("BYE");
		client.close();
	}

	// commandREVERSE
	private void commandREVERSE(String serverResponse) throws IOException {
		// dreht nachricht um und sendet an en Client
		StringBuilder input1 = new StringBuilder();
		input1.append(serverResponse);
		input1 = input1.reverse();
		sendOkay(input1.toString());
	}

	// commandLOWERCASE
	private void commandLOWERCASE(String serverResponse) throws IOException {
		sendOkay(serverResponse.toLowerCase());
	}

	// commandUPPERCASE
	private void commandUPPERCASE(String serverResponse) throws IOException {
		sendOkay(serverResponse.toUpperCase());
	}

	// ------------------ AUSGABEN -----------------------
	// ERRORS
	public void sendErrorNoArgs() throws IOException {
		sendError("SYNTAX ERROR NO ARGUMENT FOUND");
	}

	public void sendErrorNoArgs(String string) throws IOException {
		sendError("SYNTAX ERROR " + string);
	}

	public void sendErrorUnknwnCom() throws IOException {
		sendError("UNKNOWN COMMAND");
	}

	public void sendError(String msg) throws IOException {
		String output = "ERROR " + msg;
		send(output);
	}

	// OK's
	public void sendOkay(String msg) throws IOException {
		String output = "OK " + msg;
		send(output);
	}

	// send to client
	public void send(String output) throws IOException {
		PrintWriter out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8),
				true);
		if (output.getBytes().length < 255) {
			if (output.getBytes().length > 0) {
				out.println(output);
			} else {
				out.println("ERROR STRING TOO SHORT");
			}
		} else {
			out.println("ERROR STRING TOO LONG");
		}

	}

	public void printOut(String msg) {
		Server.printOut(msg);
	}
}
