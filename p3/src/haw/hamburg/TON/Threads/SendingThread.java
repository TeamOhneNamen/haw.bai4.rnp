package haw.hamburg.TON.Threads;

import java.io.IOException;

import haw.hamburg.TON.FileCopyClient;

public class SendingThread extends Thread {

	int ammoundOfPack;
	FileCopyClient fileCopyClient;
	
	public SendingThread(FileCopyClient fileCopyClient) {
		this.fileCopyClient = fileCopyClient;
	}
	
	/**
	 * send the whole time Msges if Msges in the Window
	 */
	
	@Override
	public void run() {
		int sendUntil = -1;
		while (sendUntil<=fileCopyClient.getWindow().getSize()-2) {
			try {
				
				fileCopyClient.getWindow().send();
				sendUntil = fileCopyClient.getWindow().getSendUntil();
			} catch (IOException e) {
				
			}
		}
		fileCopyClient.testOut("Copy Thread Close");
		System.out.println("SEND Thread Close");
		
	}
	
}
