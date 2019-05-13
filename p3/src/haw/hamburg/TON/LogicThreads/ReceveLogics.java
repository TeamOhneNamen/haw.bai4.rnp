package haw.hamburg.TON.LogicThreads;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import haw.hamburg.TON.*;
import haw.hamburg.TON.Exceptions.*;
import haw.hamburg.TON.UTIL.*;

public class ReceveLogics extends Thread {

	boolean copieFinished = false;
	FileCopyClient fileCopyClient;

	ReentrantLock lock;
	
	public ReceveLogics(FileCopyClient fileCopyClient) {
		this.fileCopyClient = fileCopyClient;
	}

	@Override
	public void run() {


		copieFinished = false;
		
		
		while (!copieFinished) {

			try {
				//empfangen eines neuen Packets von Server
				FCpacket newPack = fileCopyClient.getUDP().receve();
				//ermittle SeqNr von packet
				long seq = newPack.getSeqNum();
				// timer beenden
				fileCopyClient.cancelTimer(fileCopyClient.getWindow().getBySeqNr(seq));
				// ausgaben
				fileCopyClient.testOut("Paket: " + seq + " empfangen");

				fileCopyClient.getLock().lock();
				
				//wenn das packet in der Range des Windows liegt, wird es verarbeitet - sonst nicht 
				if (seq >= fileCopyClient.getWindow().get(0).getSeqNum() && seq <= fileCopyClient.getWindow().get(fileCopyClient.getWindow().size()-1).getSeqNum()) {
					
					long duration = System.nanoTime() - newPack.getTimestamp();
					// breche den Timer für das Packet ab
					fileCopyClient.cancelTimer(fileCopyClient.getWindow().getBySeqNr(seq));
					// setze es auf ValidACK
					fileCopyClient.getWindow().getBySeqNr(seq).setValidACK(true);
					// errechne die neue RTT mit der dauer der übertragung
					fileCopyClient.computeTimeoutValue(duration);
				}

				fileCopyClient.getLock().unlock();
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SeqNrNotInWindowException e) {
				System.err.println("SeqNr: " + e.getSeqNr() + " ausserhalb des Windows");

			}
			

			fileCopyClient.getLock().lock();
			
			for (int i = 0; i < fileCopyClient.getWindow().size(); i++) {
				if (fileCopyClient.getWindow().get(0).isValidACK()) {
					fileCopyClient.testOut(fileCopyClient.getWindow().get(0).getSeqNum() + " removed");
//					fileCopyClient.testOut(fileCopyClient.getWindow().toString());
					fileCopyClient.getWindow().remove(0);
					fileCopyClient.testOutWindow();
				}
			}

			if (fileCopyClient.getWindow().isEmpty()) {
				copyfinished();
			}
			
			fileCopyClient.getLock().unlock();
			
			fileCopyClient.feedTheWindow();
			
			
			
		}

	}
	
	public void copyfinished() {
		copieFinished = true;
	}

}
