package haw.hamburg.TON;

/* FileCopyClient.java
Version 0.1 - Muss ergaenzt werden!!
Praktikum 3 Rechnernetze BAI4 HAW Hamburg
Autoren:
*/
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

import haw.hamburg.TON.Exceptions.SeqNrNotInWindowException;
import haw.hamburg.TON.Threads.RecevingThread;
import haw.hamburg.TON.Threads.SendingThread;
import haw.hamburg.TON.UTIL.CSVUtils;
import haw.hamburg.TON.UTIL.FC_Timer;
import haw.hamburg.TON.UTIL.FCpacket;
import haw.hamburg.TON.UTIL.UDP;
import haw.hamburg.TON.UTIL.Window;

public class FileCopyClient extends Thread {

	// -------- Constants
	public final static boolean TEST_OUTPUT_MODE = false;
	public final static boolean TEST_OUTPUT_MODE_FILE = false;
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
	private long timeoutValue = 15 * 1000000L;
	private long expRtt = 15 * 1000000L;
	private long jitter = 20;
	private double x = 0.25;
	private double y = x / 2;

	// stats
	public static int sends = 0;
	public static int errRate = 0;
	public static long avgRtt = 0;
	public static long startTime;
	public static long endTime;

	//others
	ReentrantLock windowLock;
	Window window;
	boolean allSendet = false;
	ArrayList<String> fileContent = new ArrayList<String>();
	public int anzahlDerPackete;
	long seqNr;
	BufferedReader inFromFile;
	UDP udp;

	// Threads

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
		windowLock = new ReentrantLock();

	}
	// Getter & Setter
	
	public UDP getUDP() {
		return udp;
	}
	
	public ReentrantLock getWindowLock() {
		return windowLock;
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

			window = new Window(makeControlPacket(), windowSize, this);
			
			File file = new File(sourcePath);
			inFromFile = new BufferedReader(new FileReader(file), 1000000);
			fileContent = makeAString();
			window.setupTheWindow(fileContent);
			
			anzahlDerPackete = fileContent.size()+1;
			
			udp = new UDP(InetAddress.getByName(servername), serverPort, UDP_PACKET_SIZE);
			
			SendingThread sThread = new SendingThread(this);
			sThread.start();
			RecevingThread rThread = new RecevingThread(this);
			rThread.start();
			
			sThread.join();
			rThread.join();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

	}

	/**
	 * insert until the Buffer(Window) is full
	 */

	
	/**
	 * makes a String from the FileContent by using readLind() from BufferedReader
	 * 
	 * @return output
	 * @throws IOException
	 */
	
	private ArrayList<String> makeAString() throws IOException {
		ArrayList<String> lines = new ArrayList<String>();
		
		String line;
		String inList = "";
		
		while ((line = inFromFile.readLine()) != null) {
            line=line+"\n";
            if (inList.length()+line.length()>=(UDP_PACKET_SIZE-8)) {
				int toLong = inList.length()+line.length()-(UDP_PACKET_SIZE-8);
				fileOut(""+inList.length());
				fileOut(""+line.length());
				fileOut(""+toLong);
				int spaceLeft = line.length()-toLong;
				fileOut(""+spaceLeft);
				String firstSS = line.substring(0, spaceLeft);
				fileOut(firstSS);
				inList = inList + firstSS;
				lines.add(inList);
				inList = line.substring(spaceLeft, line.length());
				fileOut(inList);
			}else {
				inList = inList + line;
			}
            
            
        }
		
		if (inList!=null && inList!="") {
			lines.add(inList);
		}
		
		for (int j = 0; j < lines.size(); j++) {
			fileOut(lines.get(j));
			fileOut(""+lines.get(j).length());
		}

		inFromFile.close();

		return lines;

	}

	private void fileOut(String inList) {
		if (TEST_OUTPUT_MODE_FILE) {
			System.out.println(inList);
		}
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

		//set timeoutValue to timeoutValue*2
		setTimeoutValue(getTimeoutValue() * 2);

		sendAgain(seqNum);
	}

	private void sendAgain(long seqNum) {

		windowLock.lock();
		try {
			FCpacket fcp = getWindow().getBySeqNum(seqNum);
			udp.send(fcp);
			startTimer(fcp);
			sends++;
		} catch (SeqNrNotInWindowException e) {
			System.out.println(e.getSeqNr() + " is not in window: " + getWindow().toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		windowLock.unlock();
		
	}

	/**
	 *
	 * Computes the current timeout value (in nanoseconds)
	 */
	public void computeTimeoutValue(long sampleRTT) {


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
			getWindowLock().lock();
			System.out.println(getWindow().toString());
			getWindowLock().unlock();
		}
	}
	
	public static void main(String argv[]) throws Exception {
		FileCopyClient myClient;
		if(argv.length == 0) {
		myClient = new FileCopyClient("localhost","23000",
		        "C:\\Users\\Thorben\\git\\haw.bai4.rnp\\p3\\src\\haw\\hamburg\\TON\\testdoc.txt",
		        "C:\\Users\\Thorben\\git\\haw.bai4.rnp\\p3\\src\\haw\\hamburg\\TON\\dest\\testdoccopy.txt", 
		        "10",
		        "0");}
		else {
			myClient = new FileCopyClient(argv[0], argv[1], argv[2], argv[3], argv[4], argv[5]);
		}
		startTime = System.nanoTime();
		myClient.start();
		myClient.join();
        endTime = System.nanoTime();
        System.out.println(FileCopyServer.csvColumns.get(0) + (endTime - startTime) + "ns");
        System.out.println(FileCopyServer.csvColumns.get(0) + errRate/sends);
        System.out.println(FileCopyServer.csvColumns.get(0) + errRate);
        System.out.println(FileCopyServer.csvColumns.get(0) + sends);
        System.out.println(FileCopyServer.csvColumns.get(0) + avgRtt / (sends-errRate) + "ns");
        FileWriter writer = new FileWriter(FileCopyServer.csvFilePath, true);
        CSVUtils.writeLine(writer, 
        		Arrays.asList(
        				String.valueOf((endTime - startTime) + "ns"), 
        				String.valueOf(errRate/sends), 
        				String.valueOf(errRate), 
        				String.valueOf(sends),
        				String.valueOf(avgRtt / (sends-errRate) + "ns")));
        writer.flush();
        writer.close();
		
	}
	

}