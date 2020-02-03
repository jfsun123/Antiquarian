package historicalSimulators;

import java.util.ArrayList;

/**This class will be used to calculate the RSI of an arraylist of data.
 * @author James Sun
 */
public class RSI {
	
	/**The main calculation method for this class.
	 * Calculates RSI with an arraylist of prices
	 * @param stockPrices the array of price bars
	 * @return The RSI for the stock with given prices
	 */
	public static ArrayList<Float> calculate(ArrayList<PriceBar> stockPrices){
		
		if(stockPrices.size() < 15)
			return null; //not enough data
		
		ArrayList<Float> RSI = new ArrayList<Float>();
		
		//first, do the first 14 days
		float gain = 0;
		float loss = 0;
		for(int i = 1; i < 14; i++) {
			//if the last 2 points had a gain, add it
			if(stockPrices.get(i).getClose() > stockPrices.get(i - 1).getClose()) {
				gain += stockPrices.get(i).getClose() - stockPrices.get(i - 1).getClose();
			}
			else { //if the last 2 points had a loss
				loss += stockPrices.get(i - 1).getClose() - stockPrices.get(i).getClose();
			}
		}
		gain /= 14;
		loss /= 14; //make both of these the average version
		float RS = gain / loss; // RS = avg Gain / avg Loss
		RSI.add(100 - (100 / (1 + RS))); // this is the first RSI value
		
		//now, continue for the rest of the array
		for(int i = 15; i < stockPrices.size(); i++) {
			//last 2 points had a gain
			if(stockPrices.get(i).getClose() > stockPrices.get(i - 1).getClose()) {
				gain = ((gain * 13) + (stockPrices.get(i).getClose() - stockPrices.get(i - 1).getClose())) / 14;
				loss = loss * 13 / 14;
			}
			else { //if the last 2 points had a loss
				loss = ((loss * 13) + (stockPrices.get(i - 1).getClose() - stockPrices.get(i).getClose())) / 14;
				gain = gain * 13 / 14;
			}
			//now, calculate RS and add into RSI array
			RS = gain / loss;
			RSI.add(100 - (100 / (1 + RS)));
		}
		return RSI;
	}
}
