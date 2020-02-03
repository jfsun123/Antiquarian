package historicalSimulators;

import java.util.ArrayList;

public class EMA {

	/**This function will calculate the EMA using an ArrayList of stock prices.
	 * Used for first time calculation with massive array of data.
	 * @param numPeriods number of intervals
	 * @param data arraylist of floats
	 * @return EMA arraylist
	 */
	public static ArrayList<Float> calculateEMAFloat(int numPeriods, ArrayList<Float> data){
		
		if(numPeriods > data.size()) {
			//whatchu doin you fool
			return null;
		}
		ArrayList<Float> EMA = new ArrayList<Float>();
		
		float movAvg = 0;
		//first, calculate the moving average
		for(int i = 0; i < numPeriods; i++) {
			movAvg += data.get(i);
		}
		movAvg /= numPeriods;
		EMA.add(movAvg);
		//Added first moving average, the rest will be added using the simple EMA calculation
		float multiplier = (float) (2.0 / (numPeriods + 1));
		
		for(int i = numPeriods; i < data.size(); i++) {
			//EMA = (close - prevEMA) * mult + prevEMA
			float currentEMA = data.get(i) - EMA.get(EMA.size() - 1);
			currentEMA *= multiplier;
			currentEMA += EMA.get(EMA.size() - 1);
			
			//EMA calculated, save it
			EMA.add(currentEMA);
		}
		
		return EMA;
	}
	
	/**This function will calculate the EMA using an ArrayList of stock prices.
	 * Used for first time calculation with massive array of data.
	 * This function is overloaded so you can calculate EMA with float arrays too
	 * @param numPeriods number of intervals
	 * @param stockPrices arraylist of PriceBars
	 * @return EMA arraylist
	 */
	public static ArrayList<Float> calculateEMAPrice(int numPeriods, ArrayList<PriceBar> data){
		
		if(numPeriods > data.size()) {
			//whatchu doin you fool
			return null;
		}
		ArrayList<Float> EMA = new ArrayList<Float>();
		
		float movAvg = 0;
		//first, calculate the moving average
		for(int i = 0; i < numPeriods; i++) {
			movAvg += data.get(i).getClose();
		}
		movAvg /= numPeriods;
		EMA.add(movAvg);
		//Added first moving average, the rest will be added using the simple EMA calculation
		float multiplier = (float) (2.0 / (numPeriods + 1));
		
		for(int i = numPeriods; i < data.size(); i++) {
			//EMA = (close - prevEMA) * mult + prevEMA
			float currentEMA = data.get(i).getClose() - EMA.get(EMA.size() - 1);
			currentEMA *= multiplier;
			currentEMA += EMA.get(EMA.size() - 1);
			
			//EMA calculated, save it
			EMA.add(currentEMA);
		}
		
		return EMA;
	}
}
