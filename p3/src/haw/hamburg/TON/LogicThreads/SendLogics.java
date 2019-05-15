package haw.hamburg.TON.LogicThreads;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import haw.hamburg.TON.*;

public class SendLogics extends Thread {

	boolean copieFinished = false;
	long sendetUntil = -1;
	FileCopyClient fileCopyClient;

	ReentrantLock lock;

	public SendLogics(FileCopyClient fileCopyClient) {
		this.fileCopyClient = fileCopyClient;
	}

	@Override
	public void run() {

		while (!copieFinished) {

			fileCopyClient.getLock().lock();
			for (int i = 0; i < fileCopyClient.getWindow().size(); i++) {
				if (fileCopyClient.getWindow().get(i).getSeqNum() > sendetUntil) {
					try {
						fileCopyClient.getUDP().send(fileCopyClient.getWindow().get(i));
						FileCopyClient.sends++;
						fileCopyClient.testOut("Paket: " + fileCopyClient.getWindow().get(i).getSeqNum() + " gesendet");
						fileCopyClient.startTimer(fileCopyClient.getWindow().get(i));
						fileCopyClient.getWindow().get(i).setTimestamp(System.nanoTime());
						sendetUntil = fileCopyClient.getWindow().get(i).getSeqNum();
						System.out.println(sendetUntil +" "+ fileCopyClient.anzahlDerPackete);
						if (sendetUntil>=fileCopyClient.anzahlDerPackete) {
							copyfinished();
						}
					} catch (IOException e) {
						System.out.println(e.getMessage());
						e.printStackTrace();
						fileCopyClient.getLock().unlock();
					}
				}
			}

			fileCopyClient.getLock().unlock();
		}
		
		if (fileCopyClient.getLock().isLocked()) {
			fileCopyClient.getLock().unlock();			
		}

	}

	public void copyfinished() {
		copieFinished = true;
		System.out.println("SENDL BEENDET");
	}

}
