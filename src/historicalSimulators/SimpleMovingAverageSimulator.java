package historicalSimulators;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import org.jfree.ui.RefineryUtilities;

/** Data Simulator class
 *  Runs simulations on google's historical stock data using my heuristics
 *  Uses the 4 moving average, 5, 20, 39, 78 interval moving averages
 *  Uses an interval of 10 minutes, best used for day trading
 * @author James Sun
 */
public class SimpleMovingAverageSimulator{

	//some information on what data to take from google
	private int INTERVAL_INT = 600; // Interval (in seconds, 600 seconds = 10 minutes);
	private int PERIOD = 300; // Period (in days) basically as long as google will give
	private String ticker = "AMD";
	
	//TODO remove static keyword and remove main
	//save the points in order to plot on the chart
	private static ArrayList<PriceBar> stockPrices;
	
	//Moving average variables
	private static ArrayList<Float> avg5;
	private static ArrayList<Float> avg20;
	private static ArrayList<Float> avg39;
	private static ArrayList<Float> avg78;
	
	//ADX variables
	private static ArrayList<Float> ADX;
	private static ArrayList<Float> DIPlus;
	private static ArrayList<Float> DIMinus;
	
	private static ArrayList<Integer> buyingPoints;
	private static ArrayList<Integer> sellingPoints;
	
	private float principle;
	private float stopPercent = (float) 0.03; //stop losses when < 3% from max
	
	//default constructor
	public SimpleMovingAverageSimulator(String ticker, float principle, float stopPercent) throws IOException{
		this.ticker = ticker;
		this.principle = principle;
		this.stopPercent = stopPercent;
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
				}
				else {
					finalValues.add(money);
					System.out.println("Completed " + i + " out of " + tickers.size() + " simulations.");
				}
				TimeUnit.SECONDS.sleep(1);
			}
		} catch (MalformedURLException e) {
			System.err.println("MalformedURLException in function fullNasdaqSweep()");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("IOException in function fullNasdaqSweep()");
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.err.println("InterruptedException in function fullNasdaqSweep()");
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
	
	/**Simulates the algorithm on historical data
	 * Stores the data into the global variables to be graphed later by the grapher
	 * @return the final amount of money you have
	 */
	public float historySimulate() throws MalformedURLException, IOException{
		
		System.out.println("Running Moving Average Simulation on " + ticker);
		//save all the current stock prices
		ArrayList<PriceBar> stockPrices_lg = HistoryDataReader.fillStockPrices(INTERVAL_INT, PERIOD, ticker, 1);
		if(stockPrices_lg.size() < 79) {
			return 0;
		}
		
		//calculate the moving averages
		ArrayList<Float> avg5_lg = runSimulation(5, stockPrices_lg);
		ArrayList<Float> avg20_lg = runSimulation(20, stockPrices_lg);
		ArrayList<Float> avg39_lg = runSimulation(39, stockPrices_lg);
		avg78 = runSimulation(78, stockPrices_lg);
		if(avg5_lg == null || avg20_lg == null || avg39_lg == null || avg78 == null) {
			return 0;
		}
		//then calculate all the ADX values
		ADX calculator = new ADX(stockPrices_lg);
		calculator.calculateADX(14);
		if(calculator.getADX() == null || calculator.getDIMinus() == null || calculator.getDIPlus() == null) {
			return 0;
		}
		
		
		//ensure that all lists have the same size
		//SLOW O(N) PROCESS THAT COPIES, ONLY NEED TO DELETE A FEW NUMBERS
		avg5 = new ArrayList<Float>(avg5_lg.subList(avg5_lg.size() - avg78.size(), avg5_lg.size()));
		avg20 = new ArrayList<Float>(avg20_lg.subList(avg20_lg.size() - avg78.size(), avg20_lg.size()));
		avg39 = new ArrayList<Float>(avg39_lg.subList(avg39_lg.size()-avg78.size(), avg39_lg.size()));
		stockPrices = 
			new ArrayList<PriceBar>(stockPrices_lg.subList(stockPrices_lg.size() - avg78.size(), stockPrices_lg.size()));
		ADX = new ArrayList<Float>(calculator.getADX().subList
					(calculator.getADX().size() - avg78.size(), calculator.getADX().size()));
		DIPlus = new ArrayList<Float>(calculator.getDIPlus().subList
					(calculator.getDIPlus().size() - avg78.size(), calculator.getDIPlus().size()));
		DIMinus = new ArrayList<Float>(calculator.getDIMinus().subList
					(calculator.getDIMinus().size() - avg78.size(), calculator.getDIMinus().size()));
		
		
		//then, calculate the buying and selling points, stored into a static array
		calculatePurchase();
		
		//calculate the earnings and print it into the console
		return calculatePastEarnings();
	}//end historySimulate
	
	/**This function calculates the earnings and prints them.
	 * Prints the earnings into the console.
	 */
	@SuppressWarnings("unused")
	private float calculatePastEarnings() {
		
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
			
			System.out.println("--------------------------------");
			System.out.println(numShares);
			System.out.println(stockPrices.get(sellingPoints.get(i)));
			System.out.println(stockPrices.get(buyingPoints.get(i)));
			
		}
		System.out.println("Your earnings for last tick: " + (earnings));
		System.out.println("Total number of trades: " + (sellingPoints.size() * 2));
		System.out.println("Final value: " + currentValue);
		
		return currentValue;
	}
	
	/**Calculates the buying and selling points of the averages
	 * Stores the results into the array list buying and selling points
	 * Also has a stop loss to prevent catastrophe
	 */
	public void calculatePurchase() {
		
		//begin calculating selling and buying points
		buyingPoints = new ArrayList<Integer>();
		sellingPoints = new ArrayList<Integer>();
		
		float max = 0;
		boolean isBought = false;
		for(int i = 1; i < avg78.size(); i++) {
			
			if(!isBought) {
				//if it's a buying point
	    		if(avg78.get(i) - avg78.get(i - 1) >= 0 && avg39.get(i) - avg39.get(i - 1) > 0 
	    				&& avg20.get(i) - avg20.get(i - 1) > 0 && avg5.get(i) - avg5.get(i - 1)> 0 
	    				&& stockPrices.get(i).getClose() > avg78.get(i) && ADX.get(i) > 60 && DIPlus.get(i) > DIMinus.get(i)) {
	    			buyingPoints.add(i);
	    			max = stockPrices.get(i).getClose();
	    			isBought = true;
	    		}
			}
			else {
				//to prevent catastrophe
				if(stockPrices.get(i).getClose() < (max * (1 - stopPercent))) {
					sellingPoints.add(i);
					max = 0;
					isBought = false;
				}
				else if(avg78.get(i) - avg78.get(i - 1) <= 0 && avg39.get(i) - avg39.get(i - 1) < 0 
	    				&& avg20.get(i) - avg20.get(i - 1) < 0 && avg5.get(i) - avg5.get(i - 1) < 0 
	    				&& stockPrices.get(i).getClose() < avg78.get(i)) {
	    			sellingPoints.add(i);
	    			max = 0;
	    			isBought = false;
	    		}
			}

		}
	}
	
	/**Adds data to the dataset from the main class.
	 * Stores the data into global variables in this class.
	 * SLOPES -> [0] == 5, [1] == 20, [2] == 39, [3] == 78, 4] = 78MA
	 * calculates the new moving averages with the function arguments.
	 */
	public float[] addData (PriceBar currentPrice) {
		
		//return the data in this array
		float[] averages = new float[5];
		
		//if the array contains enough data, then delete the last element
		if(avg5.size() == 2000) {
			avg5.remove(0);
		}
		//calculate a new average
		float temp = avg5.get(avg5.size() - 1) * 5;
		temp -= stockPrices.get(stockPrices.size() - 5).getClose();
		temp += currentPrice.getClose();
		temp /= 5;
		avg5.add(temp);
		//then, add the slope into the returned arrays
		averages[0] = temp - avg5.get(avg5.size() - 2);
		
		if(avg20.size() == 2000) {
			avg20.remove(0);
		}
		temp = avg20.get(avg20.size() - 1) * 20;
		temp -= stockPrices.get(stockPrices.size() - 20).getClose();
		temp += currentPrice.getClose();
		temp /= 20;
		avg20.add(temp);
		averages[1] = temp - avg20.get(avg20.size() - 2);
		
		if(avg39.size() == 2000) {
			avg39.remove(0);
		}
		temp = avg39.get(avg39.size() - 1) * 39;
		temp -= stockPrices.get(stockPrices.size() - 39).getClose();
		temp += currentPrice.getClose();
		temp /= 39;
		avg39.add(temp);
		averages[2] = temp - avg39.get(avg39.size() - 2);
		
		if(avg78.size() == 2000) {
			avg78.remove(0);
		}
		temp = avg78.get(avg78.size() - 1) * 78;
		temp -= stockPrices.get(stockPrices.size() - 78).getClose();
		temp += currentPrice.getClose();
		temp /= 78;
		avg78.add(temp);
		averages[3] = temp - avg78.get(avg78.size() - 2);
		averages[4] = temp;
		
		if(stockPrices.size() == 2000) {
			stockPrices.remove(0);
		}
		stockPrices.add(currentPrice);
		
		return averages;
	}
	
	/**Runs the simulation on the inputted arraylist
	 * Returns data as an arrayList
	 * This function runs the simulation off of the given data
	 * Also calculates the ADX values and stores into a separate array
	 */
	public ArrayList<Float> runSimulation(int AVERAGE_AMOUNT, ArrayList<PriceBar> stockPrices){
		
		//local variables for this function
		ArrayList<Float> finalAverage = new ArrayList<Float>();
		LinkedList<Float> currentValues = new LinkedList<Float>();
		
		//calculate the first average
		float average = 0;
		for(int i = 0; i < AVERAGE_AMOUNT; i++) {
			average += stockPrices.get(i).getClose();
			currentValues.add(stockPrices.get(i).getClose());
		}
		
		average /= AVERAGE_AMOUNT;
		finalAverage.add(average);
		
		//continue for the averages to the rest of the array
		for(int i = AVERAGE_AMOUNT; i < stockPrices.size(); i++) {
			average *= AVERAGE_AMOUNT;
			average -= currentValues.removeFirst();
			average += stockPrices.get(i).getClose();
			average /= AVERAGE_AMOUNT;
			currentValues.add(stockPrices.get(i).getClose());
			finalAverage.add(average);
		}
		
		return finalAverage;
	}
	
	//setter methods for buying/ selling points
	public void addBuyingPoint() {buyingPoints.add(avg5.size() - 1);}
	public void addSellingPoint() {sellingPoints.add(avg5.size() - 1);}

	//Getter methods section
	public ArrayList<PriceBar> getPrices(){ return stockPrices;}
	public ArrayList<Float> getAvg5() {return avg5;}
	public ArrayList<Float> getAvg20() {return avg20;}
	public ArrayList<Float> getAvg39() {return avg39;}
	public ArrayList<Float> getAvg78() {return avg78;}
	public ArrayList<Float> getADX() {return ADX;}
	public ArrayList<Integer> getBuyingPoints() {return buyingPoints;}
	public ArrayList<Integer> getSellingPoints() {return sellingPoints;}
	
	//quick main
	public static void main(String[] args) throws IOException{  
		String ticker = "QQQ";
	    final SimpleMovingAverageSimulator dataSim = new SimpleMovingAverageSimulator(ticker, 40000, (float)0.02); 
	   	    
		dataSim.historySimulate();
		DataGrapher dataGraph = new DataGrapher(ticker);

		dataGraph.editDataset(ADX, stockPrices, avg5, avg20, avg39, avg78); 
		dataGraph.drawChart();
		dataGraph.showPurchasePoints(buyingPoints, sellingPoints);
	    dataGraph.pack( );         
	    RefineryUtilities.positionFrameRandomly( dataGraph );      
	    dataGraph.setVisible( true );
	}
}