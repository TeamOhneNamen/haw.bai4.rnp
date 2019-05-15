package haw.hamburg.TON;

public class testIt {

	long avgTime;
	
	public static void main(String[] args) {
		
		
		
		try {
			FileCopyClient fcc = new FileCopyClient("localhost", "23000", "", "", "10", "10");
			fcc.runFileCopyClient();
			fcc.join();
			avgTime = fcc.startTime - fcc.endTime;
			
			Thread.currentThread().sleep(1000);
			
			FileCopyClient fcc1 = new FileCopyClient("localhost", "23000", "", "", "10", "10");
			fcc1.runFileCopyClient();
			fcc1.join();
			
			Thread.currentThread().sleep(1000);
			
			FileCopyClient fcc2 = new FileCopyClient("localhost", "23000", "", "", "10", "10");
			fcc2.runFileCopyClient();
			fcc2.join();

			Thread.currentThread().sleep(1000);
			
			FileCopyClient fcc3 = new FileCopyClient("localhost", "23000", "", "", "10", "10");
			fcc3.runFileCopyClient();
			fcc3.join();
			
			Thread.currentThread().sleep(1000);
			
			FileCopyClient fcc4 = new FileCopyClient("localhost", "23000", "", "", "10", "10");
			fcc4.runFileCopyClient();
			fcc4.join();
			
			Thread.currentThread().sleep(1000);
			
			FileCopyClient fcc5 = new FileCopyClient("localhost", "23000", "", "", "10", "10");
			fcc5.runFileCopyClient();
			fcc5.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
