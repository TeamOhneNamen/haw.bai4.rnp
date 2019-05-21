package haw.hamburg.TON.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import haw.hamburg.TON.FileCopyClient;
import haw.hamburg.TON.FileCopyServer;

public class FileCopyClientTest {

	private final String serverDomain = "localhost";
	private final String serverPort = "23000";

	private final String sourceText = "../p3v2/src/haw/hamburg/TON/test/source.txt";
	private final String sourceByte = "../p3v2/src/haw/hamburg/TON/test/source.pdf";

	private final String destinationText = "../p3v2/src/haw/hamburg/TON/test/destination.txt";
	private final String destinationByte = "../p3v2/src/haw/hamburg/TON/test/destination.pdf";

	@Test
	public void textFileTest() throws IOException, InterruptedException {
		FileCopyServer myServer = new FileCopyServer();
		myServer.runFileCopyServer();
		FileCopyClient myClient = new FileCopyClient(serverDomain, serverPort, sourceText, destinationText, "10", "0");
		myClient.start();
		myClient.join();
		assertTrue(sameContent(Paths.get(sourceText), Paths.get(destinationText)));
	}

	public void byteFileTest() throws IOException, InterruptedException {
		FileCopyServer myServer = new FileCopyServer();
		myServer.runFileCopyServer();
		FileCopyClient myClient = new FileCopyClient(serverDomain, serverPort, sourceByte, destinationByte, "10", "0");
		myClient.start();
		myClient.join();
		assertTrue(sameContent(Paths.get(sourceByte), Paths.get(destinationByte)));
	}

//	https://stackoverflow.com/questions/27379059/determine-if-two-files-store-the-same-content
	private boolean sameContent(Path file1, Path file2) throws IOException {
		final long size = Files.size(file1);
		if (size != Files.size(file2))
			return false;

		if (size < 4096)
			return Arrays.equals(Files.readAllBytes(file1), Files.readAllBytes(file2));

		try (InputStream is1 = Files.newInputStream(file1); InputStream is2 = Files.newInputStream(file2)) {
			// Compare byte-by-byte.
			// Note that this can be sped up drastically by reading large chunks
			// (e.g. 16 KBs) but care must be taken as InputStream.read(byte[])
			// does not neccessarily read a whole array!
			int data;
			while ((data = is1.read()) != -1) {
				if (data != is2.read()) {
					System.out.println(is1.read());
					return false;
				}

			}

		}
		return true;
	}
}
