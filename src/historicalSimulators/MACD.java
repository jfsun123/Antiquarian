package historicalSimulators;

import java.util.ArrayList;

public class MACD {

	//these variables are necessary to calculate the MACD
	private ArrayList<Float> MACD;
	private ArrayList<Float> signalLine;
	
	//the stock prices are necessary to calculate the first variables
	private ArrayList<PriceBar> stockPrices;
	
	public MACD(ArrayList<PriceBar> stockPrices) {
		this.stockPrices = stockPrices;
	}
	
	/**Calculates MACD from given data in the constructor.
	 * Must construct the class from valid stockPrices arrayList.
	 */
	public void calculateMACD() {
		//calculate the two EMAs
		ArrayList<Float> EMA12_lg = calculateEMAPrice(12, stockPrices);
		ArrayList<Float> EMA26_lg = calculateEMAPrice(26, stockPrices);
				
		if(EMA12_lg == null || EMA26_lg == null)
			return; //break early if invalid data
				
		//then, calculate the MACD
		MACD = new ArrayList<Float>();
		for(int i = EMA12_lg.size() - EMA26_lg.size(); i < EMA12_lg.size(); i++) {
			float current = EMA12_lg.get(i) - EMA26_lg.get(i - (EMA12_lg.size() - EMA26_lg.size()));
			MACD.add(current);
		}
				
		//finally, calculate the 9 EMA from MACD
		signalLine = calculateEMAFloat(9, MACD);
		if(signalLine == null)
			return;
	}
	
	/**This function will calculate the EMA using an ArrayList of stock prices.
	 * Used for first time calculation with massive array of data.
	 * This function is overloaded so you can calculate EMA with float arrays too
	 * @param numPeriods number of intervals
	 * @param stockPrices arraylist of PriceBars
	 * @return EMA arraylist
	 */
	public ArrayList<Float> calculateEMAPrice(int numPeriods, ArrayList<PriceBar> data){
		
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
	
	/**This function will calculate the EMA using an ArrayList of stock prices.
	 * Used for first time calculation with massive array of data.
	 * This function is overloaded so you can calculate EMA with PriceBar arrays too
	 * @param numPeriods number of intervals
	 * @param stockPrices arraylist of float
	 * @return EMA arraylist
	 */
	public ArrayList<Float> calculateEMAFloat(int numPeriods, ArrayList<Float> data){
		
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
	
	//getter methods
	public ArrayList<Float> getMACD(){return MACD;}
	public ArrayList<Float> getSignalLine(){return signalLine;}
}
