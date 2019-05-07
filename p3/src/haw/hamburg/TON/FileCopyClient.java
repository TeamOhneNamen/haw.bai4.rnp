package haw.hamburg.TON;

/* FileCopyClient.java
Version 0.1 - Muss ergaenzt werden!!
Praktikum 3 Rechnernetze BAI4 HAW Hamburg
Autoren:
*/

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class FileCopyClient extends Thread {

	// -------- Constants
	public final static boolean TEST_OUTPUT_MODE = false;

	public final int SERVER_PORT = 23000;

	public final int UDP_PACKET_SIZE = 1008;

	// -------- Public parms
	public String servername;

	public String sourcePath;

	public String destPath;

	public int windowSize;

	public long serverErrorRate;
	
	ReentrantLock lock = new ReentrantLock();
	
	// -------- Variables
	// current default timeout in nanoseconds
	private long timeoutValue = 100000000L;
	private long rTTValue = 0;
	private long expRTTValue = 0;
	private long jitterValue = 0;
	private long x = 250;
	private long y = x/2;

	private Window window; 
	
	private boolean allSendet = false;
	
	String fileContent;

	int anzahlDerPackete;

	long seqNr = 1;
	
	BufferedReader inFromFile;

	UDP udp;

	SendLogics sendL;
	ReceveLogics receveL;
	
	// TODO

	// Constructor
	public FileCopyClient(String serverArg, String sourcePathArg, String destPathArg, String windowSizeArg,
			String errorRateArg) {
		servername = serverArg;
		sourcePath = sourcePathArg;
		destPath = destPathArg;
		windowSize = Integer.parseInt(windowSizeArg);
		serverErrorRate = Long.parseLong(errorRateArg);

	}

	public void runFileCopyClient() {

		try {
			

			window = new Window(windowSize);
			
			
			udp = new UDP(InetAddress.getLocalHost(), SERVER_PORT, UDP_PACKET_SIZE);

			File file = new File(sourcePath);
			inFromFile = new BufferedReader(new FileReader(file));
			
			fileContent = makeAString();
			
			FCpacket firstPackCpacket = makeControlPacket();
			System.out.println(new String(firstPackCpacket.getData()));
			window.add(firstPackCpacket);

			anzahlDerPackete = ((fileContent.length()/UDP_PACKET_SIZE)+1);
			
			feedTheWindow();
			
			
			sendL = new SendLogics(window, udp, this, lock);
			sendL.start();
			receveL = new ReceveLogics(window, udp, this, lock);
			receveL.start();
			
			
			inFromFile.close();
			
			
			
			
			
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO

	}

	public synchronized void feedTheWindow() {
		lock.lock();
		while (!window.isFull() && !allSendet) {
//			System.out.println("Paket: " + seqNr + " von " + anzahlDerPackete + " wurde in Window geworfen");
			if (fileContent.length()>=UDP_PACKET_SIZE) {
				String neuesStueck = fileContent.substring(0, UDP_PACKET_SIZE);
				window.add(new FCpacket(seqNr, neuesStueck.getBytes(), UDP_PACKET_SIZE));
				//System.out.println("PaketInhalt: " + neuesStueck);
				fileContent = fileContent.substring(UDP_PACKET_SIZE+1, fileContent.length());
				
			} else if(fileContent.equals("")){
				allSendet = true;
				break;
			} else {
				
//				System.out.println(fileContent.length());
				window.add(new FCpacket(seqNr, fileContent.getBytes(), fileContent.length()));
				fileContent = "";
				sendL.setFinished();
				
			}
			seqNr++;
		}
		lock.unlock();
	}
	
	public synchronized long getTimeoutValue() {
		return timeoutValue;
	}
	
	public synchronized Window getWindow() {
		return window;
	}
	
	public synchronized void setWindow(Window window) {
		this.window = window;
	}
	
	private String makeAString() throws IOException {
		
		
		
		String output = "";
		
		ArrayList<String> lines = new ArrayList<String>();
		
		String newLine = inFromFile.readLine();
		while (newLine !=null) {
			lines.add(newLine + "\n");
			newLine = inFromFile.readLine();
		}
		
		for (int i = 0; i < lines.size(); i++) {
			output = output + lines.get(i);
		}
		
		inFromFile.close();
		
		return output;
		
	}

	/**
	 *
	 * Timer Operations
	 */
	public void startTimer(FCpacket packet) {
		/* Create, save and start timer for the given FCpacket */
		FC_Timer timer = new FC_Timer(timeoutValue, this, packet.getSeqNum());
		packet.setTimer(timer);
		timer.start();
	}

	public void cancelTimer(FCpacket packet) {
		/* Cancel timer for the given FCpacket */
		testOut("Cancel Timer for packet" + packet.getSeqNum());

		if (packet.getTimer() != null) {
			packet.getTimer().interrupt();
		}
	}

	/**
	 * Implementation specific task performed at timeout
	 */
	public void timeoutTask(long seqNum) {
		
		timeoutValue = timeoutValue *2;
		
		sendAgain(seqNum);
		
		// TODO
	}

	private void sendAgain(long seqNum) {
		
		
		FCpacket packet;
		try {
			packet = window.getBySeqNr(seqNum);
			udp.send(packet);
			startTimer(packet);
		} catch (SeqNrNotInWindowException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 *
	 * Computes the current timeout value (in nanoseconds)
	 */
	public void computeTimeoutValue(long sampleRTT) {

		// expRTTValue errechnen FOLIE 57
		expRTTValue = (1-y) * expRTTValue + y * sampleRTT;
		// jitter errechnen FOLIE 57
		jitterValue = (1-x) * jitterValue + x * Math.abs(rTTValue - expRTTValue);
		// timeout errechnen FOLIE 57
		timeoutValue = expRTTValue + 4 * jitterValue;
		
	}
	
	/**
	 *
	 * Return value: FCPacket with (0 destPath;windowSize;errorRate)
	 */
	public FCpacket makeControlPacket() {
		/*
		 * Create first packet with seq num 0. Return value: FCPacket with (0 destPath ;
		 * windowSize ; errorRate)
		 */
		String sendString = destPath + ";" + windowSize + ";" + serverErrorRate;
		byte[] sendData = null;
		try {
			sendData = sendString.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return new FCpacket(0, sendData, sendData.length);
	}

	public void testOut(String out) {
		if (TEST_OUTPUT_MODE) {
			System.err.printf("%,d %s: %s\n", System.nanoTime(), Thread.currentThread().getName(), out);
		}
	}

	public static void main(String argv[]) throws Exception {
		FileCopyClient myClient = new FileCopyClient(argv[0], argv[1], argv[2], argv[3], argv[4]);
		myClient.runFileCopyClient();
	}

}