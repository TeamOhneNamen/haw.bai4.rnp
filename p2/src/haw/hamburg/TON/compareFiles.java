package haw.hamburg.TON;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class compareFiles {

	static String from = "C:\\1from\\mg-01.pdf";
	static String to = "C:\\2to\\dest.pdf";
//	static String from = "C:\\1from\\testdoc.txt";
//	static String to = "C:\\2to\\testdoccopy.txt";

	public static void main(String[] args) {

		try {

			File fileFrom = new File(from);
			File fileTo = new File(to);

			BufferedReader brFrom = new BufferedReader(new FileReader(fileFrom));
			BufferedReader brTo = new BufferedReader(new FileReader(fileTo));

			ArrayList<String> contenListFileFrom = new ArrayList<String>();

			ArrayList<String> contenListFileTo = new ArrayList<String>();
			
			String line = "";

			line = brFrom.readLine();

			while (line != null) {

				contenListFileFrom.add(line);
				line = brFrom.readLine();
			}
			
			line = "";
			line = brTo.readLine();

			while (line != null) {

				contenListFileTo.add(line);
				line = brTo.readLine();
			}
			
			
			if (contenListFileFrom.size()==contenListFileFrom.size()) {

				for (int i = 0; i < contenListFileFrom.size(); i++) {
					if (contenListFileFrom.get(i).equals(contenListFileTo.get(i))) {
						
					}else {
						System.out.println(false);
						System.out.println(contenListFileFrom.get(i) + "!=" + contenListFileTo.get(i));
						break;
					}
					
				}
				System.out.println(true);
				
			}else {
				System.out.println(false);	
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
