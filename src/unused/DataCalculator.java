package antiquarian;

import java.io.IOException;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.LinkedList;

public class DataCalculator {

	private static int INTERVAL_INT = 600; // Interval (in seconds, 600 seconds = 10 minutes);
	private static int PERIOD = 10; // Period (in days);
	private static String STOCK_TICKER = "AMD"; // Stock ticker
	private static int AVERAGE_AMOUNT = 50; // Average over x data points
	private static float currentMovingAverage;
	private static LinkedList<Float> dataPoints;
	
	// Create a new DataCalculator object that pulls results from Google
	public DataCalculator(int INTERVAL_INT_, int PERIOD_, String STOCK_TICKER_, int AVERAGE_AMOUNT_) {
		dataPoints = new LinkedList<Float>();
		INTERVAL_INT = INTERVAL_INT_;
		PERIOD = PERIOD_;
		STOCK_TICKER = STOCK_TICKER_;
		AVERAGE_AMOUNT = AVERAGE_AMOUNT_;
	}
	
	/**This function will put a new number into the numbers of the moving average.
	 * It calculates the new moving average, and returns the slope of the line
	 */
	public float moveAverage(float currentPrice) {
		//first, calculate the new moving average
		float finalMovingAverage = currentMovingAverage * AVERAGE_AMOUNT;
		finalMovingAverage -= dataPoints.removeFirst();
		finalMovingAverage += currentPrice;
		finalMovingAverage /= AVERAGE_AMOUNT;
		
		//replace the value in the queue
		dataPoints.add(currentPrice);
		
		float slope = finalMovingAverage - currentMovingAverage;
		currentMovingAverage = finalMovingAverage;
		
		return slope;
	}
	
	// After calling constructor, calculates historical moving averages for starting a new day
	public void CalculateHistoricalMA() {	
		InputStream input = null;
		try {
			input = new URL("http://finance.google.com/finance/getprices?i=" + 
					INTERVAL_INT + "&p=" + PERIOD + "d&f=d,o,h,l,c,v&df=cpct&q=" + STOCK_TICKER).openStream();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Scanner sc = new Scanner(input);

		// Skip first line (EXCHANGE%3DNASDAQ)
		sc.nextLine();
		
		// skip next the market open and close time
		sc.nextLine();
		sc.nextLine();
		
		// skip interval
		sc.nextLine();

		// Skip the next three lines, don't need this data
		sc.nextLine();
		sc.nextLine();
		sc.nextLine();
		
		int i = 0;
		// Begin data collection
		while (sc.hasNextLine()) {
			i++;
			sc.nextLine();
		}
		
		sc.close();
		
		if (i < AVERAGE_AMOUNT + 3) {
			System.err.println("Too few data points!");
		} else {
			input = null;
			try {
				input = new URL("http://finance.google.com/finance/getprices?i=" + 
						INTERVAL_INT + "&p=" + PERIOD + "d&f=d,o,h,l,c,v&df=cpct&q=" + STOCK_TICKER).openStream();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Scanner newsc = new Scanner(input);
			int j = 0;
			
			// Skip first 7 lines
			for (j = 0; j < 7; j++) {
				newsc.nextLine();
			}
			
			j = 0;
			
			while (j < i - (AVERAGE_AMOUNT)) {
				j++;
				newsc.nextLine();
			}
			
			float currentTotal = 0;
			
			for (int m = 0; m < AVERAGE_AMOUNT; m++) {
				String currLine = newsc.nextLine();
				String[] values = currLine.split(",");

				dataPoints.add(Float.valueOf(values[1]));
				currentTotal = currentTotal + Float.valueOf(values[1]);
			}
			
			currentMovingAverage = currentTotal / AVERAGE_AMOUNT;
			newsc.close();
		}
	}
	
	public float getMovingAverage() {
		return currentMovingAverage;
	}
}
