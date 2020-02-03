package antiquarian;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**This class is used to download historical data into a directory locally.
 * It will be able to download from carcharts.com, and google.
 * @author James Sun
 */
public class DownloadHistoricalData {
	private static String key1 = "c1defb646d300fdc28777471d3908c1c";
	private static String key2 = "b87964ac1d4ba360eab8f7b33fd00313";
	private static String key3 = "cd88f9663e5ce5b3a6dbbbda7021a993";
	
	private static int keyUses = 0;
	
	/**This function will read the csv file companylist.
	 * Then it will parse it for the strings of company tickers.
	 * min is the number for minimum cap, and cap is the B or M
	 * @return arraylist containing tickers for all companies in NASDAQ
	 */
	public static ArrayList<String> readCompanyTickers(float min, char cap){
		ArrayList<String> companyTickers = new ArrayList<String>();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader("StockData/companylist.csv"));
			reader.readLine(); //skip the first line because I don't need that
			String line;
			while((line = reader.readLine()) != null) {
				String ticker = line.split(",")[0];
				String marketCap = line.split("\"")[7];
				if(marketCap.equals("n/a"))
					continue;
				float capValue;
				//remove the "" signs around the actual ticker and marketCap
				ticker = ticker.substring(1, ticker.length() - 1);
				marketCap = marketCap.substring(1, marketCap.length());
				capValue = Float.valueOf(marketCap.substring(0, marketCap.length() - 1));
				
				if(marketCap.charAt(marketCap.length() - 1) == 'B' && cap == 'M') // you want million +, this is billion
					companyTickers.add(ticker);
				else if(cap == 'M' && marketCap.charAt(marketCap.length() - 1) == 'M' && capValue >= min) 
					companyTickers.add(ticker);
				else if(cap == 'B' && marketCap.charAt(marketCap.length() - 1) == 'B' && capValue >= min)
					companyTickers.add(ticker);
			}
			
			reader.close();
			
		} catch (FileNotFoundException e) {
			System.err.println("FileNotFoundException in function readCompanyTickers()");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("IOException in function readCompanyTickers()");
			e.printStackTrace();
		}

		return companyTickers;
	}
	
	/**This function is used in the fill stock prices method.
	 * @return the date of today 1 year ago
	 */
	private static String getYearDay() {
		
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
		Date date = new Date();
		String finalDay = "";
		String day = df.format(date);
		
		//gets the first 4 for the year number
		for(int i = 0; i < 4; i++) {
			finalDay += day.charAt(i);
		}
		int year = Integer.parseInt(finalDay);
		year--;
		finalDay = Integer.toString(year);
		for(int i = 4; i < day.length(); i++) {
			if(day.charAt(i) == '/') {
				continue;
			}
			finalDay += day.charAt(i);
		}
		//now finalDay should contain the day 1 year ago today.
		finalDay += "000000";
		return finalDay;
	}
	
	/**This function creates and returns the URL to use to get the data from.
	 * @param ticker the string ticker of the stock to get
	 * @param keyNum the number 1-3 for which key to use
	 * @return The url of the daily stock prices 
	 */
	private static String getURL(String ticker, int keyNum, boolean isDaily) {
		
		if(isDaily) {
			if(keyNum == 1) {
				return "http://marketdata.websol.barchart.com/getHistory.csv?key=" + key1
						+ "&symbol=" + ticker + "&type=daily&startDate=" + getYearDay();
			}
			else if(keyNum == 2) {
				return "http://marketdata.websol.barchart.com/getHistory.csv?key=" + key2
						+ "&symbol=" + ticker + "&type=daily&startDate=" + getYearDay();
			}
			else {
				return "http://marketdata.websol.barchart.com/getHistory.csv?key=" + key3
						+ "&symbol=" + ticker + "&type=daily&startDate=" + getYearDay();
			}
		}
		else {
			if(keyNum == 1) {
				return "http://marketdata.websol.barchart.com/getHistory.csv?key=" + key1
						+ "&symbol=" + ticker + "&type=minutes&startDate=" + getYearDay();
			}
			else if(keyNum == 2) {
				return "http://marketdata.websol.barchart.com/getHistory.csv?key=" + key2
						+ "&symbol=" + ticker + "&type=minutes&startDate=" + getYearDay();
			}
			else {
				return "http://marketdata.websol.barchart.com/getHistory.csv?key=" + key3
						+ "&symbol=" + ticker + "&type=minutes&startDate=" + getYearDay();
			}
		}
	}
	
	/**This function calculates which key you should be using in order to not
	 * get locked out of the system.
	 * @return the number of key which you should use to not get locked out
	 */
	private static int getKeyNum() {
		return (keyUses / 1300) + 1;
	}
	
	/**Downloads and creates a new file for the daily data for the given ticker.
	 * @throws IOException 
	 */
	public static void saveDailyData(String ticker) throws IOException {
		
		File fileSave = new File("StockData/Daily1BilCap/" + ticker + ".csv");
		if(fileSave.exists())
			return; //if it already exists, just return without changing anything
		
		System.out.println("Downloading data for " + ticker + ".");
		URL webURL = new URL(getURL(ticker, getKeyNum(), true));
		ReadableByteChannel rbc = Channels.newChannel(webURL.openStream());
		
		fileSave.getParentFile().mkdirs();
		fileSave.createNewFile(); //create a new file if the one doesn't already exist
		FileOutputStream fos = new FileOutputStream(fileSave);
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.close();
		keyUses++;
	}
	
	/**This function saves the data into a directory locally.
	 * @param ticker the ticker of the stock you want minute data of
	 * @throws IOException
	 */
	public static void saveMinuteData(String ticker) throws IOException {
		
		File fileSave = new File("StockData/Minute1BilCap/" + ticker + ".csv");
		if(fileSave.exists())
			return; //if it already exists, just return without changing anything
		
		System.out.println("Downloading minute data for " + ticker + " now.");
		URL webURL = new URL(getURL(ticker, getKeyNum(), false));
		ReadableByteChannel rbc = Channels.newChannel(webURL.openStream());
		
		fileSave.getParentFile().mkdirs();
		fileSave.createNewFile(); //create a new file if the one doesn't already exist
		FileOutputStream fos = new FileOutputStream(fileSave);
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.close();
		keyUses++;
	}
	
	public static void main(String[] args) throws IOException {
		saveDailyData("AMD");
	}
}
