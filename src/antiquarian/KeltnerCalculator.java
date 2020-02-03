package antiquarian;

import java.util.ArrayList;
import java.util.LinkedList;

/**This class will be used to calculate the buying and selling points of a certain stock.
 * It uses the keltner channel, uses historical data to simulate and then takes 15 minute data to make decisions.
 * @author James Sun
 */
public class KeltnerCalculator {
	
	//this is the array list of closing prices
	private LinkedList<PriceBar> stockPrices;
	
	//these are the channel lines
//	private LinkedList<Float> upperChannel;
//	private LinkedList<Float> lowerChannel;
	private LinkedList<Float> middleChannel;
	//some stuff to help calculations
//	private float prevATR;
	
	public KeltnerCalculator(ArrayList<PriceBar> stockPricesArray) {
		stockPrices = new LinkedList<PriceBar>(stockPricesArray);
		calculateChannels();
	}
	
	/**This function will be called right after instantiating the stockPrices.
	 * It calculates all 3 channels for the historical data.
	 */
	private void calculateChannels() {
		//first, calculate the middle channel
		middleChannel = EMA.calculateEMAPrice(20, stockPrices);
//		ArrayList<Float> ATR10 = ADX.calculateATR(10, stockPrices);
		
		//now, make sure all arrays are the same size
//		ATR10 = new ArrayList<Float>(ATR10.subList(ATR10.size() - middleChannel.size(), ATR10.size()));
		stockPrices = new LinkedList<PriceBar>(stockPrices.subList(
				stockPrices.size() - middleChannel.size(), stockPrices.size()));
		
		//calculate top and bottom channels
/*		lowerChannel = new LinkedList<Float>();
		upperChannel = new LinkedList<Float>();
		for(int i = 0; i < middleChannel.size(); i++) {
			lowerChannel.add(middleChannel.get(i) - (1 * ATR10.get(i)));
			upperChannel.add(middleChannel.get(i) + (2 * ATR10.get(i)));
		}*/
		
//		prevATR = ATR10.get(ATR10.size() - 1);
	}
	
	/**This function will update the channels to include the price of the current day.
	 * @param currentPrice the closing price of the day
	 */
	public void addData(PriceBar currentPrice) {
		
		float newEMA = middleChannel.get(middleChannel.size() - 1);
		float multiplier = (float) (2.0 / (21));
		newEMA = currentPrice.getClose() - newEMA;
		newEMA *= multiplier;
		newEMA += middleChannel.get(middleChannel.size() - 1);
		
		//finished the calculation, add it to the middle channel.
		if(middleChannel.size() == 2000) {
			middleChannel.removeFirst(); //make sure no overflow on max memory
//			upperChannel.removeFirst();
	//		lowerChannel.removeFirst();
			stockPrices.removeFirst();
		}
		middleChannel.add(newEMA);
		//calculate the new ATR, use it to find the other channels
/*		float trueRange = Math.max(currentPrice.getHigh() - currentPrice.getLow()
				, Math.max(Math.abs(currentPrice.getHigh() - stockPrices.get(stockPrices.size() - 1).getClose())
						, Math.abs(currentPrice.getLow() - stockPrices.get(stockPrices.size() - 1).getClose())));
		float newATR = ((prevATR * 9) + trueRange) / 10;
		upperChannel.add(newEMA - newATR);
		lowerChannel.add(newEMA + (2 * newATR));
		
		prevATR = newATR;*/
		stockPrices.add(currentPrice);
	}
	
	/**This function checks the current price against existing data to determine whether
	 *  or not you should purchase the stock.
	 * @param currentPrice the current stock price
	 * @return to buy, or not to buy that is the question
	 */
	public boolean checkBuy(PriceBar currentPrice) {
		
		//first, calculate the new EMA based off new data
		float newEMA = middleChannel.get(middleChannel.size() - 1);
		float multiplier = (float) (2.0 / (21));
		newEMA = currentPrice.getClose() - newEMA;
		newEMA *= multiplier;
		newEMA += middleChannel.get(middleChannel.size() - 1);
		
		if(newEMA - middleChannel.get(middleChannel.size() - 1) > (0.00016 * currentPrice.getClose())
				&& currentPrice.getClose() > newEMA) {
			return true;
		}//otherwise, you shouldn't buy
		return false;
	}
	
	/**This function checks if you should sell the stock at the current price.
	 * @param currentPrice current stock price
	 * @return to sell, or not to sell, that is the question
	 */
	public boolean checkSell(PriceBar currentPrice) {
		
		//first, calculate the new EMA based off new data
		float newEMA = middleChannel.get(middleChannel.size() - 1);
		float multiplier = (float) (2.0 / (21));
		newEMA = currentPrice.getClose() - newEMA;
		newEMA *= multiplier;
		newEMA += middleChannel.get(middleChannel.size() - 1);
		
		if(currentPrice.getClose() < newEMA && 
				stockPrices.getLast().getClose() > middleChannel.getLast()) {
			return true;
		}
		//otherwise, you shouldn't sell
		return false;
	}
	
	//getters methods
	public LinkedList<PriceBar> getStockPrices(){ return stockPrices;}
	public LinkedList<Float> getMiddleChannel() {return middleChannel;}
//	public LinkedList<Float> getUpperChannel() {return upperChannel;}
//	public LinkedList<Float> getLowerChannel() {return lowerChannel;}
}
