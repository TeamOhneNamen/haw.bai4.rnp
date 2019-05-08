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

import haw.hamburg.TON.Exceptions.SeqNrNotInWindowException;
import haw.hamburg.TON.LogicThreads.ReceveLogics;
import haw.hamburg.TON.LogicThreads.SendLogics;
import haw.hamburg.TON.UTIL.FC_Timer;
import haw.hamburg.TON.UTIL.FCpacket;
import haw.hamburg.TON.UTIL.UDP;
import haw.hamburg.TON.UTIL.Window;

public class FileCopyClient extends Thread {

	// -------- Constants
	public final static boolean TEST_OUTPUT_MODE = true;

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
	private long timeoutValue = 15 * 1000000;
	private long expRtt = 15 * 1000000;
	private long jitter = 20;
	private double x = 0.25;
	private double y = x / 2;

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
			testOut("" + new String(firstPackCpacket.getData()));
			window.add(firstPackCpacket);

			anzahlDerPackete = ((fileContent.length() / UDP_PACKET_SIZE) + 1);
			testOut("" + anzahlDerPackete);

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
			testOut("Packet " + seqNr + "/" + anzahlDerPackete + " is now in Window");
			if (this.fileContent.equals("")) {
				allSendet = true;
				break;
			} else if (this.fileContent.length() >= UDP_PACKET_SIZE) {
				String neuesStueck = this.fileContent.substring(0, UDP_PACKET_SIZE);
				window.add(new FCpacket(seqNr, neuesStueck.getBytes(), UDP_PACKET_SIZE));
				// System.out.println("PaketInhalt: " + neuesStueck);
				this.fileContent = this.fileContent.substring(UDP_PACKET_SIZE + 1, this.fileContent.length());

			} else {

//				System.out.println(fileContent.length());
				window.add(new FCpacket(seqNr, this.fileContent.getBytes(), this.fileContent.length()));
				this.fileContent = "";
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
	public synchronized void startTimer(FCpacket packet) {
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

		testOut("Timer for Packet " + seqNum + " timeouted");
		
		
		setTimeoutValue(getTimeoutValue() * 2);
		
		

		testOut("rtt is now: " + getTimeoutValue() + " after miss");
		sendAgain(seqNum);


		// TODO
	}

	private synchronized void sendAgain(long seqNum) {

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
	public synchronized void computeTimeoutValue(long sampleRTT) {

		System.out.println(sampleRTT);
		
		if (expRtt < 0) {
			expRtt = sampleRTT;
        }
		
		
		System.out.println("(1.0 - y) = " + (1.0 - y));
		System.out.println("expRtt = " + expRtt);
		System.out.println("y * sampleRTT = " + y * sampleRTT);
		System.out.println("(1.0 - y) * expRtt + y * sampleRTT = " + (long) (1.0 - y) * expRtt + y * sampleRTT);
		
		// expRTTValue errechnen FOLIE 57
		expRtt = (long) ((1.0 - y) * expRtt + y * sampleRTT);

		System.out.println("(1.0 - x) = " + (1.0 - x));
		System.out.println("jitter = " + jitter);
		System.out.println("Math.abs(sampleRTT - expRtt) = " + Math.abs(sampleRTT - expRtt));
		System.out.println("x * Math.abs(sampleRTT - expRtt) = " + (long) x *  Math.abs(sampleRTT - expRtt));
		System.out.println("(1.0 - x) * jitter + x * Math.abs(sampleRTT - expRtt) = " + (long) (1.0 - x) * jitter + x * Math.abs(sampleRTT - expRtt));
		
		// jitter errechnen FOLIE 57
		jitter = (long) ((1.0 - x) * jitter + x * Math.abs(sampleRTT - expRtt));

		System.out.println("4 * jitter = " + 4 * jitter);
		System.out.println("expRtt = " + expRtt);
		System.out.println("expRtt + 4 * jitter = " + expRtt + 4 * jitter);
		
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

	public static void main(String argv[]) throws Exception {
		FileCopyClient myClient = new FileCopyClient(argv[0], argv[1], argv[2], argv[3], argv[4]);
		myClient.runFileCopyClient();
	}

}