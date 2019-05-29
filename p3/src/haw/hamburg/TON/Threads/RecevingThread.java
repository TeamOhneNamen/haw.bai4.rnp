package haw.hamburg.TON.Threads;

import java.io.IOException;

import haw.hamburg.TON.FileCopyClient;

public class RecevingThread extends Thread{

	FileCopyClient fileCopyClient;
	
	public RecevingThread(FileCopyClient fileCopyClient) {
		this.fileCopyClient = fileCopyClient;
	}
	
	/**
	 * receve the whole time Msges
	 */
	
	@Override
	public void run() {
		int windowPos = -1;
		while (windowPos<=fileCopyClient.getWindow().getSize()-1) {
			try {
				fileCopyClient.getWindow().revece();
				windowPos = fileCopyClient.getWindow().getWindowPos();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		fileCopyClient.testOut("RECEVE Thread beendet");
		System.out.println("RECEVE Thread beendet");
	}
	
}
