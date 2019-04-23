package haw.hamburg.TON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
	BufferedReader in;
	PrintWriter out;

	public BusinessThread(Socket client) throws UnsupportedEncodingException, IOException {
		this.client = client;

		commands.add("UPPERCASE");
		commands.add("LOWERCASE");
		commands.add("REVERSE");
		commands.add("SHUTDOWN");
		commands.add("BYE");
		
	}

	@Override
	public void run() {
		if (!Server.shutdowned) {
			clientAlive = true;
			while (clientAlive) {
				try {
					in = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8), 1000);
					out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true);
					client.setSoTimeout(30000);

					
					String serverResponse = in.readLine();
					Server.printOut(serverResponse);
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

						case -1:
							sendErrorUnknwnCom();
							break;
						default:
							break;
						}

					} else {
						sendError("STRING TOO LONG");
					}

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
				} finally {
					
				}

			}
		} else {
			try {
				client.close();
				in.close();
				clientAlive = false;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private String cutBlank(String msg) {
		return msg.substring(1);
	}

	private void commandSHUTDOWN(String serverResponse) throws IOException {
		if (serverResponse.equals(passwort)) {
			Server.close();
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

	public void sendOkay(String msg) throws IOException {
		String output = "OK " + msg;
		send(output);
	}

	public void send(String output) throws IOException {
		
		if (output.getBytes().length < 255) {
			out.println(output);
		} else {
			out.println("ERROR STRING TOO LONG");
		}

	}

	public void printOut(String msg) {
		System.out.println("[SERVER] \"" + msg + "\"");
	}

	public Socket getClient() {
		return client;
	}
}
