package historicalSimulators;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class HistoryDataReader {
	
	/**This function will fill the arrayList stockPrices.
	 * Takes data from BarChart OnDemand, parses data.
	 * Currently can only do daily
	 * @param INTERVAL_INT how long should the interval be in minutes
	 * @param PERIOD how many intervals for each period
	 * @param ticker what stock to run on
	 * @param which key to use
	 * @return The arraylist of price bars with data from that stock
	 */
	public static ArrayList<PriceBar> fillStockPrices(int INTERVAL_INT, String ticker) 
			throws IOException{
		
		ArrayList<PriceBar> stockPrices = new ArrayList<PriceBar>();
		
		//download the data off the web before you read it
		DownloadHistoricalData.saveMinuteData(ticker);

		File currentRead = new File("StockData/Minute1BilCap/" + ticker + ".csv");
		Scanner sc = new Scanner(currentRead);
		sc.nextLine(); //skip the first line
		while(sc.hasNextLine()) {

			String[] values = sc.nextLine().split(",");
			
			PriceBar current = new PriceBar(Float.valueOf(removeQuote(values[4])), 
					Float.valueOf(removeQuote(values[5])), Float.valueOf(removeQuote(values[6])));
			
			stockPrices.add(current);
			
			//skip the next few lines
			for(int i = 0; i < INTERVAL_INT; i++) {
				if(sc.hasNextLine()) {
					sc.nextLine();
				}
				else {
					break;
				}
			}
		}
		sc.close();
		return stockPrices;
	}
	
	/**This function will fill the arrayList stockPrices.
	 * Takes data from BarChart OnDemand, parses data.
	 * Currently can only do daily
	 * @param INTERVAL_INT how long should the interval be in minutes
	 * @param PERIOD how many intervals for each period
	 * @param ticker what stock to run on
	 * @param which key to use
	 * @return The arraylist of price bars with data from that stock
	 */
	public static ArrayList<PriceBar> fillStockPrices(String ticker) throws IOException{
		
		ArrayList<PriceBar> stockPrices = new ArrayList<PriceBar>();

		DownloadHistoricalData.saveDailyData(ticker);
		
		File currentRead = new File("StockData/Daily1BilCap/" + ticker + ".csv");
		Scanner sc = new Scanner(currentRead);
		sc.nextLine(); //skip the first line
		while(sc.hasNextLine()) {
			String[] values = sc.nextLine().split(",");
			
			PriceBar current = new PriceBar(Float.valueOf(removeQuote(values[4])), 
					Float.valueOf(removeQuote(values[5])), Float.valueOf(removeQuote(values[6])));
			
			stockPrices.add(current);
		}
		sc.close();
		return stockPrices;
	}
	
	private static String removeQuote(String value) {
		String withoutQuote = "";
		for(int i = 0; i < value.length();i ++) {
			if(value.charAt(i) == '"') {
				continue;
			}
			withoutQuote += value.charAt(i);
		}
		return withoutQuote;
	}
	
	
	/**This function will fill the arraylist stockPrices
	 * It takes data off of google's financial page and then parses data.
	 * I've been blacklisted from google finance, using other websites now.
	 */
	public static ArrayList<PriceBar> fillStockPricesGoogle(int INTERVAL_INT, int PERIOD, String ticker) {
		
		ArrayList<PriceBar> stockPrices_lg = new ArrayList<PriceBar>();
		
		//Go to google finance's historical data
		InputStream input = null;

		int tryNumber = 1;
		do {
			try {
				input = new URL("http://finance.google.com/finance/getprices?i=" + 
						INTERVAL_INT + "&p=" + PERIOD + "d&f=d,o,h,l,c,v&df=cpct&q=" + ticker).openStream();
			} catch(IOException e) {
				try {
					System.err.println("Error in input URL, trying again");
					TimeUnit.SECONDS.sleep(tryNumber * tryNumber);
					tryNumber++;
					e.printStackTrace();
					continue;
				} catch(InterruptedException e1) {
					e1.printStackTrace();
				}
			}
			if(input != null) {
				break;
			}
		}while(true);

				
		Scanner sc = new Scanner(input);
		//skip the first 7 lines, they're not useful
		for(int i = 0; i < 7; i++) {
			if(!sc.hasNextLine()) {
				//break early
				sc.close();
				return stockPrices_lg;
			}
			sc.nextLine();
		}
				
		//Now, begin actual data collection
		while(sc.hasNextLine()) {
			
			if(INTERVAL_INT == 7200) {
				//if you want 2 hour data, skip a line
				sc.nextLine(); //skip a line to get 2 hour data
				if(!sc.hasNextLine()) {
					break;
				}
			}
			String currLine = sc.nextLine();
			String[] values = currLine.split(",");
			
			PriceBar current = new PriceBar(Float.valueOf(values[2]), 
					Float.valueOf(values[3]), Float.valueOf(values[1]));
			
			stockPrices_lg.add(current);
		}
		
		sc.close();
		return stockPrices_lg;
		
	}
}
