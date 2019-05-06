package haw.hamburg.TON;

/* FileCopyClient.java
Version 0.1 - Muss ergaenzt werden!!
Praktikum 3 Rechnernetze BAI4 HAW Hamburg
Autoren:
*/

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.Arrays;

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
	
	// -------- Variables
	// current default timeout in nanoseconds
	private long timeoutValue = 100000000L;
	private long rTTValue = 0;
	private long expRTTValue = 0;
	private long jitterValue = 0;
	private long x = 250;
	private long y = x/2;

	private byte[] receiveData;
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
			FCpacket firstPackCpacket = makeControlPacket();

			for (int j = 0; j < firstPackCpacket.getData().length; j++) {
				System.out.print(firstPackCpacket.getData()[j] + " ");
			}

			File file = new File(sourcePath);

			byte[] fileContent = Files.readAllBytes(file.toPath());

			System.out.println(new String(fileContent));
			
			System.out.print(new String(firstPackCpacket.getData()));

			DatagramSocket udp_Socket = new DatagramSocket();
			udp_Socket.connect(InetAddress.getLocalHost(), SERVER_PORT);
			udp_Socket.send(new DatagramPacket(firstPackCpacket.getData(), firstPackCpacket.getLen()));
			
			
		    DatagramPacket udpReceivePacket;

		    receiveData = new byte[UDP_PACKET_SIZE];
			udpReceivePacket = new DatagramPacket(receiveData, UDP_PACKET_SIZE);
	        // Wait for data packet
			udp_Socket.receive(udpReceivePacket);
			
			System.out.print(new String(udpReceivePacket.getData()));
			
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
		
		
		// TODO
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
		String sendString = "0;" + destPath + ";" + windowSize + ";" + serverErrorRate;
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