package haw.hamburg.TON;

public class testIt {

	static final int WAIT_DURATION = 3000;

	static long avgTime;

	public static void main(String[] args) {
		doIt("C:\\1 from\\testdoc.txt", "C:\\2 to\\testdoccopy.txt", 10, 10);
		doIt("C:\\1 from\\testdoc.txt", "C:\\2 to\\testdoccopy.txt", 100, 10);
		doIt("C:\\1 from\\testdoc.txt", "C:\\2 to\\testdoccopy.txt", 1000, 10);
		System.out.println("now ErrRate");

		doIt("C:\\1 from\\testdoc.txt", "C:\\2 to\\testdoccopy.txt", 10, 0);
		doIt("C:\\1 from\\testdoc.txt", "C:\\2 to\\testdoccopy.txt", 10, 2);
		doIt("C:\\1 from\\testdoc.txt", "C:\\2 to\\testdoccopy.txt", 10, 5);
		doIt("C:\\1 from\\testdoc.txt", "C:\\2 to\\testdoccopy.txt", 10, 10);
		doIt("C:\\1 from\\testdoc.txt", "C:\\2 to\\testdoccopy.txt", 10, 100);
		doIt("C:\\1 from\\testdoc.txt", "C:\\2 to\\testdoccopy.txt", 10, 1000);
	}

	private static void doIt(String from, String to, int windowSize, int errRate) {

		try {

			FileCopyClient fcc = new FileCopyClient("localhost", "23000", from,
					to, "" + windowSize, "" + errRate);
			long startTime = System.nanoTime();
			fcc.runFileCopyClient();
			fcc.join();
			long endTime = System.nanoTime();
			avgTime = (avgTime + endTime - startTime);
			System.out.println(avgTime);

			Thread.sleep(WAIT_DURATION);

			FileCopyClient fcc1 = new FileCopyClient("localhost", "23000", "C:\\1 from\\testdoc.txt",
					"C:\\2 to\\testdoccopy.txt", "10", "10");
			startTime = System.nanoTime();
			fcc1.runFileCopyClient();
			fcc1.join();
			endTime = System.nanoTime();
			avgTime = (avgTime + endTime - startTime);
			System.out.println(avgTime / 2);

			Thread.sleep(WAIT_DURATION);

			FileCopyClient fcc2 = new FileCopyClient("localhost", "23000", "C:\\1 from\\testdoc.txt",
					"C:\\2 to\\testdoccopy.txt", "10", "10");
			startTime = System.nanoTime();
			fcc2.runFileCopyClient();
			fcc2.join();
			endTime = System.nanoTime();
			avgTime = (avgTime + endTime - startTime);
			System.out.println(avgTime / 3);

			Thread.sleep(WAIT_DURATION);

			FileCopyClient fcc3 = new FileCopyClient("localhost", "23000", "C:\\1 from\\testdoc.txt",
					"C:\\2 to\\testdoccopy.txt", "10", "10");
			startTime = System.nanoTime();
			fcc3.runFileCopyClient();
			fcc3.join();
			endTime = System.nanoTime();
			avgTime = (avgTime + endTime - startTime);
			System.out.println(avgTime / 4);

			Thread.sleep(WAIT_DURATION);

			FileCopyClient fcc4 = new FileCopyClient("localhost", "23000", "C:\\1 from\\testdoc.txt",
					"C:\\2 to\\testdoccopy.txt", "10", "10");
			startTime = System.nanoTime();
			fcc4.runFileCopyClient();
			fcc4.join();
			endTime = System.nanoTime();
			avgTime = (avgTime + endTime - startTime);
			System.out.println(avgTime / 5);

			Thread.sleep(WAIT_DURATION);

			FileCopyClient fcc5 = new FileCopyClient("localhost", "23000", "C:\\1 from\\testdoc.txt",
					"C:\\2 to\\testdoccopy.txt", "10", "10");
			startTime = System.nanoTime();
			fcc5.runFileCopyClient();
			fcc5.join();
			endTime = System.nanoTime();
			avgTime = (avgTime + endTime - startTime);

			System.out.println("ErrRate: 10, WindowSize: 10");
			System.out.println("AVGTime: " + avgTime / 6);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
