package haw.hamburg.TON.LogicThreads;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import haw.hamburg.TON.FileCopyClient;
import haw.hamburg.TON.UTIL.UDP;
import haw.hamburg.TON.UTIL.Window;

public class SendLogics extends Thread {

	boolean copieNotFinished = false;
	Window window;
	UDP udp;
	long sendetUntil = -1;
	FileCopyClient fileCopyClient;
	
	ReentrantLock lock;
	
	public SendLogics(Window window, UDP udp, FileCopyClient fileCopyClient, ReentrantLock lock) {
		this.window = window;
		this.udp = udp;
		this.fileCopyClient = fileCopyClient;
		this.lock = lock;
	}

	
	@Override
	public void run() {
		copieNotFinished = true;
		while (copieNotFinished) {
			
			lock.lock();
			
			for (int i = 0; i < fileCopyClient.getWindow().size(); i++) {
				if (fileCopyClient.getWindow().get(i).getSeqNum()>sendetUntil) {
					try {
						udp.send(fileCopyClient.getWindow().get(i));
						fileCopyClient.startTimer(fileCopyClient.getWindow().get(i));
						fileCopyClient.getWindow().get(i).setTimestamp(System.nanoTime());
						sendetUntil++;
//						System.out.println("wurde bis: " + sendetUntil + " gesendet.");

						System.out.println(window.toString());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			lock.unlock();
			
		}
	}
	
	public void setFinished() {
		copieNotFinished = false;
	}

}
