package historicalSimulators;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.jfree.ui.RefineryUtilities;

/**This class will be used to calculate the Moving Average Convergence Divergence.
 * This calculator is going to calculate the MACD using (12, 26, 9) and
 * uses an period length of 2 hours.  Best used for long term trading.
 * Dependent on MACD class for calculations.
 * @author James Sun
 */

public class MACDSimulator {
	
	//private variables to decide on historical data
	private int INTERVAL_INT = 7200; // Interval (2 hours of time);
	private int PERIOD = 300; // Period (in days) basically as long as google will give
	private String ticker;
	
	//private variables to decide how much stock to buy and when to sell
	private float principle = 5000;
	
	//TODO remove static keyword and remove main
	//private ArrayLists to hold the data from calculations done
	private static ArrayList<Float> MACD;
	private static ArrayList<Float> EMA9;
	private static ArrayList<PriceBar> stockPrices;
	
	private static ArrayList<Integer> buyingPoints;
	private static ArrayList<Integer> sellingPoints;
	
	
	//default constructor
	public MACDSimulator (String ticker, float principle) throws IOException{
		this.ticker = ticker;
		this.principle = principle;
	}
	
	/**Runs a full sweep of historical simulation on all nasdaq stocks.
	 * Utilizes google's finance API.
	 */
	public void fullNasdaqSweep() {
		//first, read all the tickers from the CSV file
		ArrayList<String> tickers = HistoryDataReader.readCompanyTickers();
		ArrayList<Float> finalValues = new ArrayList<Float>();
		
		try {
			for(int i = 0; i < tickers.size(); i++) {
				ticker = tickers.get(i);
				
				float money = historySimulate();
				if(money == 0) {
					System.out.println("None or not enough stock data for " + ticker + ", continuing.");
				}else {
					finalValues.add(money);
					System.out.println("Completed " + i + " out of " + tickers.size() + " simulations.");
				}
				TimeUnit.SECONDS.sleep(1);
			}
		} catch (InterruptedException e) {
			System.err.println("InterruptedException in function fullNasdaqSweep()");
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		int numBelow50 = 0; int numAbove50 = 0;
		int numBelow25 = 0; int numAbove25 = 0;
		int numBelow10 = 0; int numAbove10 = 0;
		int numBelow5 = 0; int numAbove5 = 0;
		int numSlightLoss = 0; int numSlightGain = 0;
		
		for(int i = 0; i < finalValues.size(); i++) {
			if(finalValues.get(i) < (principle * 0.5)) {
				numBelow50++;
			}
			else if(finalValues.get(i) < (principle * 0.75)) {
				numBelow25++;
			}
			else if(finalValues.get(i) < (principle * 0.9)) {
				numBelow10++;
			}
			else if(finalValues.get(i) < (principle * 0.95)) {
				numBelow5++;
			}
			else if(finalValues.get(i) < principle) {
				numSlightLoss++;
			}
			else if(finalValues.get(i) < (principle * 1.05)) {
				numSlightGain++;
			}
			else if(finalValues.get(i) < (principle * 1.1)) {
				numAbove5++;
			}
			else if(finalValues.get(i) < (principle * 1.25)) {
				numAbove10++;
			}
			else if(finalValues.get(i) < (principle * 1.5)){
				numAbove25++;
			}
			else {
				numAbove50++;
			}
		}
		
		System.out.println("---------FINAL RESULTS---------------");
		System.out.println("50% loss or more: " + numBelow50);
		System.out.println("25% loss to 50%: " + numBelow25);
		System.out.println("10% loss to 25%: " + numBelow10);
		System.out.println("5% loss to 10%: " + numBelow5);
		System.out.println("Less than 5% loss: " + numSlightLoss);
		System.out.println("Less than 5% gain: " + numSlightGain);
		System.out.println("5% gain to 10%: " + numAbove5);
		System.out.println("10% gain to 25%: " + numAbove10);
		System.out.println("25% gain to 50%: " + numAbove25);
		System.out.println("50% gain or more: " + numAbove50);
	}
	
	/**Simulates the heuristic on historical data given by google.
	 * Saves the data into global variables so that it can be graphed by the grapher.
	 * THIS VERSION IS FOR THE NASDAQ SWEEP SIMULATION, returns earnings
	 */
	public float historySimulate() throws IOException, MalformedURLException{
		System.out.println("Running MACD simulation on historical data of stock " + ticker );
		//first, fill in a larger version of the stock prices
		ArrayList<PriceBar> stockPrices_lg = HistoryDataReader.fillStockPrices(INTERVAL_INT, PERIOD, ticker);
		if(stockPrices_lg.size() == 0)
			return 0;
		
		MACD calculator = new MACD(stockPrices_lg);
		calculator.calculateMACD();
		MACD = calculator.getMACD();
		EMA9 = calculator.getSignalLine();
		//then, make all arrays the same size
		stockPrices = new ArrayList<PriceBar>(
				stockPrices_lg.subList(stockPrices_lg.size() - EMA9.size(), stockPrices_lg.size()));
		MACD = new ArrayList<Float>(MACD.subList(MACD.size() - EMA9.size(), MACD.size()));
		
		//save more data into arrays for the grapher
		calculatePurchase();
		
		//then calculate profits
		return calculateEarnings();
	}
	
	/**Calculates how much money is earned from the buying and selling points.
	 * Same as the moving average earnings calculator.
	 */
	public float calculateEarnings() {
		float currentValue = principle;
		
		//calculate the number of shares that you can buy
		int numShares = (int)(principle / stockPrices.get(0).getClose());
		
		//each sell is when you make the money
		float earnings = 0;
		for(int i = 0; i < sellingPoints.size(); i++) {
			earnings = numShares * (stockPrices.get(sellingPoints.get(i)).getClose()
						- stockPrices.get(buyingPoints.get(i)).getClose()) - 14;
			currentValue += earnings;
			numShares = (int)(currentValue / stockPrices.get(sellingPoints.get(i)).getClose());
			
/*			System.out.println("--------------------------------");
			System.out.println(numShares);
			System.out.println(stockPrices.get(sellingPoints.get(i)));
			System.out.println(stockPrices.get(buyingPoints.get(i)));*/
			
		}
/*		System.out.println("Your earnings for last tick: " + (earnings));
		System.out.println("Total number of trades: " + (sellingPoints.size() * 2));
		System.out.println("Final value: " + currentValue);*/
		
		return currentValue;
	}
	
	/**Calculates the buying and selling points of the data.
	 * Uses the MACD and EMA9 heuristics to find all of the purchase points.
	 */
	public void calculatePurchase() {
		buyingPoints = new ArrayList<Integer>();
		sellingPoints = new ArrayList<Integer>();
		
		boolean isBought = false;
		
		for(int i = 1; i < MACD.size(); i++) {
			//run through to see all buying and selling points
			if(!isBought) {
				if(EMA9.get(i) - EMA9.get(i - 1) > 0.00) {
					buyingPoints.add(i);
					isBought = true;
				}
			}
			else {
				//search for selling points
				if(EMA9.get(i) - EMA9.get(i - 1) < 0) {
					sellingPoints.add(i);
					isBought = false;
				}
			}
		}
	}

	public static void main(String[] args) throws Throwable {
		String ticker = "AAPL";
		MACDSimulator calc = new MACDSimulator(ticker, 5000);
		calc.historySimulate();
		
		
		//this stuff is for the grapher
		DataGrapher dataGraph = new DataGrapher(ticker);
	    
		dataGraph.editDataset(stockPrices, MACD, EMA9); 
		dataGraph.drawChart();
		dataGraph.showPurchasePoints(buyingPoints, sellingPoints);
	    dataGraph.pack( );         
	    RefineryUtilities.positionFrameRandomly( dataGraph );      
	    dataGraph.setVisible( true );
	}
	
}
