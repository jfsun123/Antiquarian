package historicalSimulators;

import java.util.ArrayList;

public class ADX {

	//these variables are necessary to calculate the ADX
	private ArrayList<Float> ADX;
	private ArrayList<Float> DMPlus;
	private ArrayList<Float> DMMinus;
	private ArrayList<Float> DIPlus;
	private ArrayList<Float> DIMinus;
	private ArrayList<Float> ATR;
	
	//also, a copy of the stock prices array is necessary
	private ArrayList<PriceBar> stockPrices;
	
	public ADX(ArrayList<PriceBar> stockPrices) {
		this.stockPrices = stockPrices;
	}
	
	/**Calculates the ADX at each point in the array stock prices.
	 * saves DM+, DM-, DI+, DI-, ADX and ATR in array lists
	 */
	public void calculateADX(int periodLen){
		
		//first, calculate DM
		calculateDM();
		
		//then calculate ATR
		calculateATR(periodLen);
		
		if(DMPlus.size() < 100 || ATR.size() < 100) {
			return;
		}
		
		// now calculate DI+
		DIPlus = calculateEMA(periodLen, DMPlus);
		for(int i = 1; i < ATR.size(); i++) {
			DIPlus.set(i -1, DIPlus.get(i - 1) * 100 / ATR.get(i));
		}
		
		//Now DI-
		DIMinus = calculateEMA(periodLen, DMMinus);
		for(int i = 1; i < ATR.size(); i++) {
			DIMinus.set(i -1, DIMinus.get(i - 1) * 100 / ATR.get(i));
		}
		
		//Finally, calculate the ADX
		ADX = new ArrayList<Float>();
		for(int i = 0; i < DIMinus.size(); i++) {
			float numer = DIPlus.get(i) - DIMinus.get(i);
			float denom = DIPlus.get(i) + DIMinus.get(i);
			float current = Math.abs(numer / denom);
			
			ADX.add(current);
		}
		//calculate the EMA of these values
		ADX = calculateEMA(periodLen, ADX);
		//then multiply all of them by 100
		for(int i = 0; i < ADX.size(); i++) {
			ADX.set(i, ADX.get(i) * 100);
		}
		
	}
	
	/**This function calculates the ATR based off of stock data in stockPrices.
	 * Stores the data into the ATR arrayList.
	 */
	public void calculateATR(int periodLen) {
		
		ATR = new ArrayList<Float>();
		
		float averageTrueRange = 0;
		for(int i = 1; i < periodLen; i ++) {
			float trueRange = Math.max(stockPrices.get(i).getHigh() - stockPrices.get(i).getLow()
					, Math.max(Math.abs(stockPrices.get(i).getHigh() - stockPrices.get(i - 1).getClose())
							, Math.abs(stockPrices.get(i).getLow() - stockPrices.get(i - 1).getClose())));
			averageTrueRange += trueRange;
		}
		averageTrueRange /= periodLen; //first ATR calculated
		ATR.add(averageTrueRange);
		//find the rest of the ATRs
		for(int i = periodLen; i < stockPrices.size(); i++) {
			float trueRange = stockPrices.get(i).getHigh() - stockPrices.get(i).getLow();
			averageTrueRange *= periodLen - 1;
			averageTrueRange += trueRange;
			averageTrueRange /= periodLen;
			
			//new ATR calculated, add to array
			ATR.add(averageTrueRange);
		}
	}
	
	/**This function calculates the ATR based off of stock data in stockPrices.
	 * Stores the data into the ATR arrayList.
	 * This is the overloaded version which returns the ATR
	 */
	public static ArrayList<Float> calculateATR(int periodLen, ArrayList<PriceBar> stockPrices) {
		
		if(stockPrices.size() <= periodLen) {
			return null;
		}
		ArrayList<Float> ATR = new ArrayList<Float>();
		
		float averageTrueRange = 0;
		for(int i = 1; i < periodLen; i ++) {
			float trueRange = Math.max(stockPrices.get(i).getHigh() - stockPrices.get(i).getLow()
					, Math.max(Math.abs(stockPrices.get(i).getHigh() - stockPrices.get(i - 1).getClose())
							, Math.abs(stockPrices.get(i).getLow() - stockPrices.get(i - 1).getClose())));
			averageTrueRange += trueRange;
		}
		averageTrueRange /= periodLen; //first ATR calculated
		ATR.add(averageTrueRange);
		//find the rest of the ATRs
		for(int i = periodLen; i < stockPrices.size(); i++) {
			float trueRange = stockPrices.get(i).getHigh() - stockPrices.get(i).getLow();
			averageTrueRange *= periodLen - 1;
			averageTrueRange += trueRange;
			averageTrueRange /= periodLen;
			
			//new ATR calculated, add to array
			ATR.add(averageTrueRange);
		}
		return ATR;
	}
	
	/**This function will calculate the EMA using an ArrayList of stock prices.
	 * Used for first time calculation with massive array of data.
	 * @param numPeriods number of intervals
	 * @param data arraylist of floats
	 * @return EMA arraylist
	 */
	public ArrayList<Float> calculateEMA(int numPeriods, ArrayList<Float> data){
		
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
	
	/**This function calculated the Directional Movement based off stockPrices.
	 * It saves the DM+ and DM- into the corresponding ArrayLists.
	 */
	private void calculateDM() {
		//first, calculate the DM+ and DM-
		DMPlus = new ArrayList<Float>();
		DMMinus = new ArrayList<Float>();
		
		for(int i = 1; i < stockPrices.size(); i++) {
			float upMove = stockPrices.get(i).getHigh() - stockPrices.get(i - 1).getHigh();
			float downMove = stockPrices.get(i - 1).getLow() - stockPrices.get(i).getLow();
					
			if(upMove > downMove && upMove > 0) {
				DMPlus.add(upMove);
			}
			else {
				DMPlus.add((float)0);
			}
			
			if(downMove > upMove && downMove > 0) {
				DMMinus.add(downMove);
			}
			else {
				DMMinus.add((float) 0);
			}
		}
	} //END DM CALCULATE
	
	//getter functions. After you calculate the ADX, you can get the arrays from these functions
	public ArrayList<Float> getADX(){return ADX;}
	public ArrayList<Float> getDIPlus(){return DIPlus;}
	public ArrayList<Float> getDIMinus(){return DIMinus;}
	public ArrayList<Float> getATR(){return ATR;}
}
