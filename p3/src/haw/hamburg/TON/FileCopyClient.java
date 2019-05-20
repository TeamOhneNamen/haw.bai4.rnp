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
	public final static boolean TEST_OUTPUT_MODE = true;
	public final static boolean TEST_OUTPUT_MODE_WINDOW = false;

	public int serverPort = 23000;

	public final int UDP_PACKET_SIZE = 1008;

	// -------- Public parms
	public String servername;

	public String sourcePath;

	public String destPath;

	public int windowSize;

	public long serverErrorRate;

	// -------- Variables
	// current default timeout in nanoseconds
	private long timeoutValue = 150 * 1000000;
	private long expRtt = 15 * 1000000;
	private long jitter = 20;
	private double x = 0.25;
	private double y = x / 2;

	// stats
	public static int sends = 0;
	public static int errRate = 0;
	public static long startTime;
	public static long endTime;

	//others
	ReentrantLock lock;
	Window window;
	boolean allSendet = false;
	String fileContent;
	public int anzahlDerPackete;
	long seqNr;
	BufferedReader inFromFile;
	UDP udp;

	// Threads
	SendLogics sendL;
	ReceveLogics receveL;

	@Override
	public void run() {
		runFileCopyClient();
	}
	
	// Constructor
	public FileCopyClient(String serverArg, String port, String sourcePathArg, String destPathArg, String windowSizeArg,
			String errorRateArg) {

		servername = serverArg;
		serverPort = Integer.parseInt(port);
		sourcePath = sourcePathArg;
		destPath = destPathArg;
		windowSize = Integer.parseInt(windowSizeArg);
		serverErrorRate = Long.parseLong(errorRateArg);
		seqNr = 1;
		lock = new ReentrantLock();

	}
	// Getter & Setter
	
	public UDP getUDP() {
		return udp;
	}
	
	public ReentrantLock getLock() {
		return lock;
	}
	
	public long getTimeoutValue() {
		return timeoutValue;
	}

	public void setTimeoutValue(long timeoutValue) {
		this.timeoutValue = timeoutValue;
	}

	public Window getWindow() {
		return window;
	}

	public void setWindow(Window window) {
		this.window = window;
	}
	
	
	/**
	 * Copy the File 
	 */
	public void runFileCopyClient() {

		try {

			// create a buffer(Window)
			setWindow(new Window(windowSize));
			
			// cretes A UDP connection
			udp = new UDP(InetAddress.getByName(servername), serverPort, UDP_PACKET_SIZE);

			// get the file and setup a buffered reader 
			File file = new File(sourcePath);
			inFromFile = new BufferedReader(new FileReader(file), 1000000);
			
			fileContent = makeAString();

			// add the ControllPackage to the Window
			FCpacket firstPackCpacket = makeControlPacket();
			window.add(firstPackCpacket);

			
			anzahlDerPackete = ((fileContent.length() / UDP_PACKET_SIZE) + 1);
			
			testOut("ammound of Packete: " + anzahlDerPackete);

			// insert new Packages to the Wondow until the Window is fulll
			feedTheWindow();
			
			//start threads "sendLogics" and "receveLogics"
			sendL = new SendLogics(this);
			sendL.start();
			receveL = new ReceveLogics(this);
			receveL.start();
			
			sendL.join();
			receveL.join();

			inFromFile.close();

		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}

	}

	/**
	 * insert until the Buffer(Window) is full
	 */
	public void feedTheWindow() {
		/**
		 *  need to lock, because The ReceveLogics and SendLogics use 
		 *  the Data maby in the same moment
		 */
		getLock().lock();
		
		while (!getWindow().isFull() && !allSendet) {
			
			testOut("Packet " + seqNr + "/" + anzahlDerPackete + " is now in Window");
			
			if (this.fileContent.isEmpty()) {
				allSendet = true;
				
			} else if (this.fileContent.length() >= UDP_PACKET_SIZE) {
				String neuesStueck = this.fileContent.substring(0, UDP_PACKET_SIZE);
				getWindow().add(new FCpacket(seqNr, neuesStueck.getBytes(), UDP_PACKET_SIZE));
				this.fileContent = this.fileContent.substring(UDP_PACKET_SIZE + 1, this.fileContent.length());
				testOutWindow();
				seqNr++;

			} else {
				
				getWindow().add(new FCpacket(seqNr, this.fileContent.getBytes(), this.fileContent.length()));
				this.fileContent = "";
				seqNr++;

			}

		}
		getLock().unlock();
	}

	
	/**
	 * makes a String from the FileContent by using readLind() from BufferedReader
	 * 
	 * @return output
	 * @throws IOException
	 */
	
	private String makeAString() throws IOException {

		char[] receiveData = new char[UDP_PACKET_SIZE];
		String output = "";

		ArrayList<String> lines = new ArrayList<String>();
		inFromFile.read(receiveData, 0, UDP_PACKET_SIZE);
		String newLine = new String(receiveData);
		while (newLine != null) {
			lines.add(newLine);
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
		FC_Timer timer = new FC_Timer(getTimeoutValue(), this, packet.getSeqNum());
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
	public synchronized void timeoutTask(long seqNum) {

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
			testOut(seqNum + " alreaddy deleted");
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
			getLock().lock();
			System.out.println(getWindow().toString());
			getLock().unlock();
		}
	}
	
	public static void main(String argv[]) throws Exception {
		FileCopyClient myClient = new FileCopyClient(argv[0], argv[1], argv[2], argv[3], argv[4], argv[5]);
		long startTime = System.currentTimeMillis();
		myClient.start();
		myClient.join();
        long endTime = System.currentTimeMillis();
        System.out.println("Bearbeitungs-Zeit: " + (endTime - startTime) + " ms");
        System.out.println("Fehlerrate: " + errRate/sends);
		
	}
	

}