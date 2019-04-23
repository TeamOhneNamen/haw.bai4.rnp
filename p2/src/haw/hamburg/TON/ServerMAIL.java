package haw.hamburg.TON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ServerMAIL {
	
	static BufferedReader in;
	static PrintWriter out;
	static boolean alive = false;
	
	public static void main(String[] args) {
		alive = true;
		ServerSocket ss;
		try {
			ss = new ServerSocket(12345);
			System.out.println("[SERVER] gestartet auf: 12345");
			Socket client = ss.accept();
			System.out.println("[SERVER] neue Verbindung");

			in = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
			out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true);
			while (alive) {
				System.out.println("[SERVER] warte auf antwort!");
				String input = in.readLine();
				System.out.println("[SERVER] mach was mit:" + input);
				if(input.startsWith("STAT")){
						out.println("+OK 5 345");
				} else if (input.startsWith("USER")) {
					out.println("+OK Der User exestiert");
				} else if (input.startsWith("PASS")) {
					out.println("+OK Das Passwort ist Korrekt");
				} else if (input.startsWith("LIST 0")) {
					out.println("+OK 0 200");
				} else if (input.startsWith("LIST 1")) {
					out.println("+OK 1 240");
				} else if (input.startsWith("LIST 2")) {
					out.println("+OK 2 245");
				} else  if (input.startsWith("LIST 3")) {
					out.println("+OK 3 223");
				} else  if (input.startsWith("LIST 4")) {
					out.println("+OK 4 262");
				} else  if (input.startsWith("LIST 5")) {
					out.println("+OK 5 2453");
				} else if (input.startsWith("RETR 6")) {

					out.println("+OK 6 240");
					out.println("ich binn es");
					out.println("ich binn es");
					out.println("ich binn es");
					out.println("ich binn es");
					out.println(".");
				
				} else if (input.startsWith("RETR 2")) {

					out.println("+OK 2 245");
					out.println("ich binn es");
					out.println("ich binn es");
					out.println("ich binn es");
					out.println("ich binn es");
					out.println("ich binn es");
					out.println(".");
				
				} else if (input.startsWith("RETR 3")) {

					out.println("+OK 3 223");
					out.println("ich binn es");
					out.println("ich binn es");
					out.println("ich binn es");
					out.println(".");
				
				} else if (input.startsWith("RETR 4")) {
					
					out.println("+OK 4 262");
					out.println("ich binn es");
					out.println(".");
				
				} else if(input.startsWith("RETR 5")){

					out.println("+OK 5 2453");
					out.println("ich binn es");
					out.println(".");
					
				} else if (input.startsWith("RETR")){
					out.println("-ERR " + input);
				} else if (input.startsWith("LIST")){
					out.println("+OK 5 messages (3423 octets)");
					out.println("2 245");
					out.println("3 223");
					out.println("4 262");
					out.println("5 2453");
					out.println("6 240");
					out.println(".");
				} else {
					out.println("-ERR " + input);
				}
			}
		
			
		} catch (IOException e) {
			alive = false;
		}
	}
	
}
