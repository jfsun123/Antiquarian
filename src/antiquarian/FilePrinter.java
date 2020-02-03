package antiquarian;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**Temporary class which prints the data into a file.
 * DELETE THE FILE BETWEEN RUNS
 * @author James Sun
 *
 */
public class FilePrinter {

	private static File currentFile = new File("StockData/simResults.txt");
	
	public static void printData(String message) throws IOException {
		//if the file doesn't exist, make sure it does
		currentFile.getParentFile().mkdirs();
		currentFile.createNewFile();
		
		PrintWriter out = new PrintWriter(currentFile);
		out.println(message);
		out.close();
	}
	
	public static void printBuy(float price, int numShares, float currentMoney) throws IOException{
		//if the file doesn't exist, make sure it does
		currentFile.getParentFile().mkdirs();
		currentFile.createNewFile();
		
		//then, write the actual message
		PrintWriter out = new PrintWriter(currentFile);
		out.println("---- NEW BUY ORDER ----");
		out.println("Bought " + numShares + " at price: $" + price + ". $" + currentMoney +" remaining.");
		out.close();
	}
	
	public static void printSell (float price, int numShares, float currentMoney) throws IOException{
		//if the file doesn't exist, make sure it does
		currentFile.getParentFile().mkdirs();
		currentFile.createNewFile();
		
		//write the actual message
		PrintWriter out = new PrintWriter(currentFile);
		out.println("---- NEW SELL ORDER ----");
		out.println("Sold " + numShares + " at price: $" + price + ". $" + currentMoney +" remaining.");
		out.close();
	}
}
