package historicalSimulators;

import java.io.IOException;
import java.util.ArrayList;

import org.jfree.ui.RefineryUtilities;

/**This class will run simulations using the keltner channel heuristic.
 * The intervals are in 15 minute periods, and is still best used for day trading.
 * @author James Sun
 */
public class KeltnerChannelSimulator {

	private float principle = 5000;
	private String ticker;
	
	//Stock prices are needed to do this calculation
	private static ArrayList<PriceBar> stockPrices;
	
	//variables needed for the keltner channel
	private static ArrayList<Float> upperChannel;
	private static ArrayList<Float> lowerChannel;
	private static ArrayList<Float> middleChannel;
	
	private static ArrayList<Integer> buyingLongPoints;
	private static ArrayList<Integer> sellingLongPoints;
	private static ArrayList<Integer> buyingShortPoints;
	private static ArrayList<Integer> sellingShortPoints;
	
	//these are just so that calculating earnings is easier
	private ArrayList<Float> buyingLongPrices;
	private ArrayList<Float> sellingLongPrices;
	private ArrayList<Float> buyingShortPrices;
	private ArrayList<Float> sellingShortPrices;
	
	
	public KeltnerChannelSimulator(String ticker, float principle) {
		this.principle = principle;
		this.ticker = ticker;
	}
	
	/**Runs a full sweep of historical simulation on all nasdaq stocks.
	 * Utilizes google's finance API.
	 */
	public float fullNasdaqSweep(int constant) {
		
		int numBelow50 = 0; int numAbove50 = 0;
		int numBelow25 = 0; int numAbove25 = 0;
		int numBelow10 = 0; int numAbove10 = 0;
		int numBelow5 = 0; int numAbove5 = 0;
		int numSlightLoss = 0; int numSlightGain = 0;
		int numNoChange = 0;
		
		//first, read all the tickers from the CSV file, 10billion market cap or higher
		ArrayList<String> tickers = DownloadHistoricalData.readCompanyTickers(1, 'B');
		ArrayList<Float> finalValues = new ArrayList<Float>();
		ArrayList<String> kreygasm = new ArrayList<String>();
		
		for(int i = 0; i < tickers.size(); i++) {
			ticker = tickers.get(i);
			System.out.println("Running simulation on " + ticker);
			//so that I can pick which key to use
			float money = historySimulate(constant);
			if(money == 0) {
				System.out.println("None or not enough stock data for " + ticker + ", continuing.");
			}else {
				finalValues.add(money);
				if(money > 10000)
					kreygasm.add(ticker);
				System.out.println("Completed " + (i + 1) + " out of " + tickers.size() + " simulations.");
				
				if(money < (principle * 0.5)) {
					numBelow50++;
				}
				else if(money < (principle * 0.75)) {
					numBelow25++;
				}
				else if(money < (principle * 0.9)) {
					numBelow10++;
				}
				else if(money < (principle * 0.95)) {
					numBelow5++;
				}
				else if(money < principle) {
					numSlightLoss++;
				}
				else if(money == principle) {
					numNoChange++;
				}
				else if(money < (principle * 1.05)) {
					numSlightGain++;
				}
				else if(money < (principle * 1.1)) {
					numAbove5++;
				}
				else if(money < (principle * 1.25)) {
					numAbove10++;
				}
				else if(money < (principle * 1.5)){
					numAbove25++;
				}
				else {
					numAbove50++;
				}
			}
			
		}
		
		System.out.println("---------FINAL RESULTS---------------");
		System.out.println("50% loss or more: " + numBelow50);
		System.out.println("25% loss to 50%: " + numBelow25);
		System.out.println("10% loss to 25%: " + numBelow10);
		System.out.println("5% loss to 10%: " + numBelow5);
		System.out.println("Less than 5% loss: " + numSlightLoss);
		System.out.println("No trades: " + numNoChange);
		System.out.println("Less than 5% gain: " + numSlightGain);
		System.out.println("5% gain to 10%: " + numAbove5);
		System.out.println("10% gain to 25%: " + numAbove10);
		System.out.println("25% gain to 50%: " + numAbove25);
		System.out.println("50% gain or more: " + numAbove50);
		System.out.println("Mean: " + Statistics.mean(finalValues));
		System.out.println("Mean percent earned: " + (Statistics.mean(finalValues) / principle));
		System.out.println("Standard Deviation: " + Statistics.standardDev(finalValues));
System.out.println(kreygasm);
		
		return Statistics.mean(finalValues);
	}
	
	/**This function simulates the historical stock data from google.
	 * @return the amount of remaining money you have
	 */
	public float historySimulate(int constant) {
		
		try {
			stockPrices = HistoryDataReader.fillStockPrices(15, ticker);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		ArrayList<Float> ATR10;
		
		middleChannel = EMA.calculateEMAPrice(constant, stockPrices);
		ATR10 = ADX.calculateATR(10, stockPrices);
		
		if(ATR10 == null || middleChannel == null)
			return 0;
		//with everything calculated, make sure they're all the same size
		ATR10 = new ArrayList<Float>(ATR10.subList(ATR10.size() - middleChannel.size(), ATR10.size()));
		stockPrices = new ArrayList<PriceBar>(stockPrices.subList(
				stockPrices.size() - middleChannel.size(), stockPrices.size()));
		
		//calculate top and bottom channels
		lowerChannel = new ArrayList<Float>();
		upperChannel = new ArrayList<Float>();
		for(int i = 0; i < middleChannel.size(); i++) {
//			lowerChannel.add(middleChannel.get(i) - (float)(0.1 * multiplier * ATR10.get(i)));
			upperChannel.add(middleChannel.get(i) + (float)(2.6 * ATR10.get(i)));
		}
		
		//calculate selling points and buying points
		calculatePurchase();
		
		//then calculate earnings
		return calculatePastEarnings();
	}
	
	/**This function calculates the earnings and prints them.
	 * Calculates both longs and shorts
	 * Prints the earnings into the console.
	 */
	@SuppressWarnings("unused")
	private float calculatePastEarnings() {
		
		float currentValue = principle;
		
		//calculate the number of shares that you can buy
		int numShares = (int)(principle / stockPrices.get(0).getClose());
		//each sell is when you make the money
		float earnings = 0;
		ArrayList<Float> earningList = new  ArrayList<Float>();
		int numPositive100 = 0; int numPositive10 = 0; int numPositive1000 = 0, numPositiveGreater = 0; int numPositive500 = 0;
		int numNegative100 = 0, numNegative10 = 0, numNegative1000 = 0, numNegativeGreater = 0; 

		System.out.println("--------LONGS-----------");
		for(int i = 0; i < sellingLongPrices.size(); i++) {
			numShares = (int)(currentValue / buyingLongPrices.get(i)); //for longs
/*			earnings = numShares * (stockPrices.get(sellingLongPoints.get(i)).getClose()
					- stockPrices.get(buyingLongPoints.get(i)).getClose()) - 14;*/
			earnings = numShares * (sellingLongPrices.get(i) - buyingLongPrices.get(i)) - 10;
			earningList.add(earnings);
			if(earnings < -1000) {
				numNegativeGreater++;
			}
			else if(earnings < -100){
				numNegative1000++;
			}
			else if(earnings < -10) {
				numNegative100++;
			}
			else if(earnings < 0) {
				numNegative10++;
			}
			else if(earnings < 10) { 
				numPositive10++;
			}
			else if(earnings < 100) {
				numPositive100++;
			}
			else if(earnings < 500) {
				numPositive500++;
			}
			else if(earnings < 1000) {
				numPositive1000++;
			}
			else {
				numPositiveGreater++;
			}
			currentValue += earnings;
		}
		
		System.out.println("Total number of trades: " + ((sellingLongPoints.size() * 2)));
		System.out.println("Final value: " + currentValue);
		System.out.println(earningList);
		System.out.println("Greater than 1000 loss: " + numNegativeGreater);
		System.out.println("Less than 1000 loss: " + numNegative1000);
		System.out.println("Less than 100 loss: " + numNegative100);
		System.out.println("Less than 10 loss: " + numNegative10);
		System.out.println("Less than 10 gain: "+ numPositive10);
		System.out.println("Less than 100 gain: "+ numPositive100);
		System.out.println("Less than 500 gain: " + numPositive500);
		System.out.println("Less than 1000 gain: "+ numPositive1000);
		System.out.println("Greater than 1000 gain: " + numPositiveGreater);
		float min = Float.MAX_VALUE;
		for(int i = 0; i < earningList.size(); i++) {
			if(earningList.get(i) < min) {
				min = earningList.get(i);
			}
		}
		System.out.println("Worst trade: " + min);
		
		
		System.out.println("----------SHORTS-----------");
		earnings = 0;
		earningList = new  ArrayList<Float>();
		numPositive100 = 0;numPositive10 = 0; numPositive1000 = 0; numPositiveGreater = 0;numPositive500 = 0;
		numNegative100 = 0;numNegative10 = 0; numNegative1000 = 0; numNegativeGreater = 0; 
		for(int i = 0; i < sellingShortPoints.size(); i++) {
			numShares = (int)(currentValue / buyingShortPrices.get(i)); //for longs
			earnings = numShares * (buyingShortPrices.get(i) - sellingShortPrices.get(i)) - 10;
			earningList.add(earnings);
			if(earnings < -1000) {
				numNegativeGreater++;
			}
			else if(earnings < -100){
				numNegative1000++;
			}
			else if(earnings < -10) {
				numNegative100++;
			}
			else if(earnings < 0) {
				numNegative10++;
			}
			else if(earnings < 10) { 
				numPositive10++;
			}
			else if(earnings < 100) {
				numPositive100++;
			}
			else if(earnings < 500) {
				numPositive500++;
			}
			else if(earnings < 1000) {
				numPositive1000++;
			}
			else {
				numPositiveGreater++;
			}
			currentValue += earnings;
		}
		
		System.out.println("Total number of trades: " + ((sellingShortPoints.size() * 2)));
		System.out.println("Final value: " + currentValue);
		System.out.println(earningList);
		System.out.println("Greater than 1000 loss: " + numNegativeGreater);
		System.out.println("Less than 1000 loss: " + numNegative1000);
		System.out.println("Less than 100 loss: " + numNegative100);
		System.out.println("Less than 10 loss: " + numNegative10);
		System.out.println("Less than 10 gain: "+ numPositive10);
		System.out.println("Less than 100 gain: "+ numPositive100);
		System.out.println("Less than 500 gain: " + numPositive500);
		System.out.println("Less than 1000 gain: "+ numPositive1000);
		System.out.println("Greater than 1000 gain: " + numPositiveGreater);
		float max = 0;
		for(int i = 0; i < earningList.size(); i++) {
			if(earningList.get(i) > max) {
				max = earningList.get(i);
			}
		}
		System.out.println("Worst trade: " + min);
		
		return currentValue;
	}
	
	/**Calculates the buying and selling points for this simulation.
	 * Also calculates the shorting points.
	 * Stores the data into arrayLists.
	 */
	private void calculatePurchase() {
		buyingLongPoints = new ArrayList<Integer>();
		sellingLongPoints = new ArrayList<Integer>();
		buyingShortPoints = new ArrayList<Integer>();
		sellingShortPoints = new ArrayList<Integer>();
		buyingLongPrices = new ArrayList<Float>();
		buyingShortPrices = new ArrayList<Float>();
		sellingLongPrices = new ArrayList<Float>();
		sellingShortPrices = new ArrayList<Float>();
		
		//calculate buying and selling points for longs
		boolean isBoughtLong = false;
		boolean isBoughtShort = false;
		for(int i = 2; i < middleChannel.size(); i++) {
			if(!isBoughtLong && !isBoughtShort) {
				//check to buy
			if(middleChannel.get(i) - middleChannel.get(i - 1) > (0.00016 * stockPrices.get(i).getClose())) {
					if(stockPrices.get(i).getClose() > middleChannel.get(i)) {
						buyingLongPoints.add(i);
						buyingLongPrices.add(middleChannel.get(i));
						isBoughtLong = true;
					}
				}
				
				//check to short
/*			if(middleChannel.get(i) - middleChannel.get(i - 1) < (-0.015 * stockPrices.get(i).getClose()) && 
						stockPrices.get(i).getClose() < middleChannel.get(i)) {
					buyingShortPoints.add(i);
					buyingShortPrices.add(stockPrices.get(i).getClose());
					isBoughtShort = true;
				}*/
			}
			else if(isBoughtLong){
				if(stockPrices.get(i - 1).getClose() > middleChannel.get(i - 1) && 
						stockPrices.get(i).getClose() <= middleChannel.get(i)) {
					isBoughtLong = false;
					sellingLongPoints.add(i);
					sellingLongPrices.add(middleChannel.get(i));
				}
			}
			else if(isBoughtShort) {
				if(middleChannel.get(i - 1) < middleChannel.get(i)) {
					isBoughtShort = false;
					sellingShortPoints.add(i);
					sellingShortPrices.add(stockPrices.get(i).getClose());
				}
			}
			
		}
	}
	
	//quick main
	public static void main(String[] args) throws IOException{  
		String ticker = "AMD";
		KeltnerChannelSimulator dataSim = new KeltnerChannelSimulator(ticker, 5000); 

		ArrayList<Float> results = new ArrayList<Float>();
		float max = 0; int best = 0;
		
		for(int i = 10; i < 30; i++) {
			float current = dataSim.fullNasdaqSweep(i);
			results.add(current);
			if(current > max) {
				max = current;
				best = i;
			}
		}
		
		System.out.println("Best result: " + max);
		System.out.println("Best constant: " + (best));

		
		
/*	    DataGrapher dataGraph = new DataGrapher(ticker);
	    dataGraph.editDataset(stockPrices, middleChannel, upperChannel, lowerChannel);
		dataGraph.drawTimeChart();
		dataGraph.showPurchasePoints(buyingLongPoints, sellingLongPoints, buyingShortPoints, sellingShortPoints);
		dataGraph.pack( );         
		RefineryUtilities.positionFrameRandomly( dataGraph );      
		dataGraph.setVisible( true );*/
	}
}
