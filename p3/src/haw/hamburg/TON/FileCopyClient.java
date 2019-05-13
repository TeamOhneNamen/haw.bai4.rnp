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

import haw.hamburg.TON.Exceptions.*;
import haw.hamburg.TON.LogicThreads.*;
import haw.hamburg.TON.UTIL.*;

public class FileCopyClient extends Thread {

	// -------- Constants
	public final static boolean TEST_OUTPUT_MODE = false;
	public final static boolean TEST_OUTPUT_MODE_WINDOW = true;

	public int serverPort = 23000;

	public final int UDP_PACKET_SIZE = 1008;

	// -------- Public parms
	public String servername;

	public String sourcePath;

	public String destPath;

	public int windowSize;

	public long serverErrorRate;

	public ReentrantLock lock = new ReentrantLock();

	// -------- Variables
	// current default timeout in nanoseconds
	private long timeoutValue = 150 * 1000000;
	private long expRtt = 15 * 1000000;
	private long jitter = 20;
	private double x = 0.25;
	private double y = x / 2;

	public static int sends = 0;
	static int errRate = 0;

	private Window window;

	private boolean allSendet = false;

	String fileContent;

	public int anzahlDerPackete;

	long seqNr = 1;

	BufferedReader inFromFile;

	UDP udp;

	SendLogics sendL;
	ReceveLogics receveL;

	// TODO

	// Constructor
	public FileCopyClient(String serverArg, String port, String sourcePathArg, String destPathArg, String windowSizeArg,
			String errorRateArg) {

		servername = serverArg;
		serverPort = Integer.parseInt(port);
		sourcePath = sourcePathArg;
		destPath = destPathArg;
		windowSize = Integer.parseInt(windowSizeArg);
		serverErrorRate = Long.parseLong(errorRateArg);

	}

	public void runFileCopyClient() {

		try {

			window = new Window(windowSize);

			udp = new UDP(InetAddress.getByName(servername), serverPort, UDP_PACKET_SIZE);

			File file = new File(sourcePath);
			inFromFile = new BufferedReader(new FileReader(file));

			fileContent = makeAString();

			FCpacket firstPackCpacket = makeControlPacket();
			testOut(new String(firstPackCpacket.getData()));
			window.add(firstPackCpacket);

			anzahlDerPackete = ((fileContent.length() / UDP_PACKET_SIZE) + 1);
			testOut("" + anzahlDerPackete);

			feedTheWindow();

			sendL = new SendLogics(udp, this, lock);
			sendL.start();
			receveL = new ReceveLogics(udp, this, lock);
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
			testOut("Packet " + seqNr + "/" + anzahlDerPackete + " is now in Window");
			if (this.fileContent.isEmpty()) {
				allSendet = true;
				break;
			} else if (this.fileContent.length() >= UDP_PACKET_SIZE) {
				String neuesStueck = this.fileContent.substring(0, UDP_PACKET_SIZE);
				window.add(new FCpacket(seqNr, neuesStueck.getBytes(), UDP_PACKET_SIZE));
				this.fileContent = this.fileContent.substring(UDP_PACKET_SIZE + 1, this.fileContent.length());
				testOutWindow();

			} else {
				
				window.add(new FCpacket(seqNr, this.fileContent.getBytes(), this.fileContent.length()));
				this.fileContent = "";
				sendL.copyfinished();

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

	public synchronized void setTimeoutValue(long timeoutValue) {
		this.timeoutValue = timeoutValue;
	}

	private String makeAString() throws IOException {

		String output = "";

		ArrayList<String> lines = new ArrayList<String>();

		String newLine = inFromFile.readLine();
		while (newLine != null) {
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
		if (packet.getTimer() != null) {
			packet.getTimer().interrupt();
		}
	}

	/**
	 * Try to send Packet again and multiply the Timeout with 2
	 */
	public void timeoutTask(long seqNum) {

		errRate++;

		testOut("Timer for Packet " + seqNum + " timeouted");

		setTimeoutValue(getTimeoutValue() * 2);

		sendAgain(seqNum);

		// TODO
	}

	private synchronized void sendAgain(long seqNum) {

		FCpacket packet;
		try {
			packet = getWindow().getBySeqNr(seqNum);
			udp.send(packet);
			sends++;
			startTimer(packet);
		} catch (SeqNrNotInWindowException e) {
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 *
	 * Computes the current timeout value (in nanoseconds)
	 */
	public synchronized void computeTimeoutValue(long sampleRTT) {

//		System.out.println(sampleRTT);

		if (expRtt < 0) {
			expRtt = sampleRTT;
		}

		// expRTTValue errechnen FOLIE 57
		expRtt = (long) ((1.0 - y) * expRtt + y * sampleRTT);

		// jitter errechnen FOLIE 57
		jitter = (long) ((1.0 - x) * jitter + x * Math.abs(sampleRTT - expRtt));

		// timeout errechnen FOLIE 57

		setTimeoutValue(expRtt + 4 * jitter);

	}

	/**
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

	public void testOutWindow() {
		if (TEST_OUTPUT_MODE_WINDOW) {
			System.out.println(getWindow().toString());
		}
	}
	
	public static void main(String argv[]) throws Exception {
		FileCopyClient myClient = new FileCopyClient(argv[0], argv[1], argv[2], argv[3], argv[4], argv[5]);
		myClient.runFileCopyClient();
	}

}