package haw.hamburg.TON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class ServerMAIL {
	
	static BufferedReader in;
	static PrintWriter out;
	static boolean alive = false;
	
	public static void main(String[] args) {
		alive = true;
		ServerSocket ss;
		int mailNR = 0;
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
						out.println("+OK 6 345");
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

					out.println("-ERR ende der nachrichten");
				
				} else if(input.startsWith("DELE 6")){

					out.println("-ERR ende der nachrichten");
					
				} else if (input.startsWith("RETR")){
					Random rand = new Random();
					mailNR++;
					out.println("+OK " + mailNR + " " + rand.nextInt(255));
					out.println(input);
					out.println(".");
				} else {
					out.println("+OK " + input);
				}
			}
		
			
		} catch (IOException e) {
			alive = false;
		}
	}
	
}
