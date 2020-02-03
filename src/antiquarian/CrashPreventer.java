package antiquarian;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.etrade.etws.market.DetailFlag;
import com.etrade.etws.market.QuoteResponse;
import com.etrade.etws.sdk.client.ClientRequest;
import com.etrade.etws.sdk.client.MarketClient;
import com.etrade.etws.sdk.common.ETWSException;

/**The CrashPreventer class constantly asks ETRADE for new stock data.
 * It makes sure that the price of the stock does not suddenly drop.
 * @author James Sun
 */

public class CrashPreventer implements Runnable{
	
	//Global variable to identify company
	private String companyTicker = "AMD";
	private float stopPercent = (float)0.02;

	//Global variables for this class
	private float overallHigh = 0;
	private float localHigh = 0;
	private float localLow = 0;
	
	//Global variables to store the marketClient and request to ask ETRADE
	private MarketClient marketClient;
	private ClientRequest request;
	
	//The listener that has it's event called when you want to trigger a sell from here
	private PurchaseManager purchaseManager;
	
	public CrashPreventer (MarketClient marketClient, ClientRequest request, 
			String companyTicker, float stopPercent, PurchaseManager purchaseManager) {
		overallHigh = 0;
		localHigh = 0;
		localLow = Float.MAX_VALUE; //so that we can use the comparisons
		
		this.marketClient = marketClient;
		this.request = request;
		
		this.companyTicker = companyTicker;
		this.stopPercent = stopPercent;
		this.purchaseManager = purchaseManager;
	}
	
	/**The thread task to prevent crashes.
	 * This function will constantly be running in the background and updating the high and lows.
	 * If the closing price of the stock goes below a certain percent below overallHigh, stock is sold.
	 * Should only be running when stock is owned.  Stop after selling stock.
	 * CURRENTLY IN SIMULATION MODE
	 */
	@Override()
	public void run() {
		for(;;) {
			try {
				if(!purchaseManager.getOwned()) {
					//if you don't own the stock, skip execution
					System.out.println("Stock not owned, skipping crash prevention.");
					TimeUnit.SECONDS.sleep(10);
					continue;
				}
				
				PriceBar currentPrice = getMarketQuote();
				
				//then, reset highs and lows if needed
				if(currentPrice.getClose() < localLow) {
					//this only checks when you actually own the stock
					if(purchaseManager.getOwned() && (currentPrice.getClose() < overallHigh * (1 - stopPercent))) {
						//when crash occurs, trigger
						//TODO this is for the simulation
						purchaseManager.stopLoss(currentPrice);
					}
					localLow = currentPrice.getClose();
				}
				if(currentPrice.getClose() > overallHigh) {
					overallHigh = currentPrice.getClose();
				}
				if(currentPrice.getClose() > localHigh) {
					localHigh = currentPrice.getClose();
				}
				
				//then, wait a second for the next call
				TimeUnit.SECONDS.sleep(1);
				
			} catch (IOException e) {
				System.err.println("IOException in function: run(), CrashPreventer multithread");
				e.printStackTrace();
			} catch (ETWSException e) {
				System.err.println("ETWSException in function: run(), CrashPreventer multithread");
				e.printStackTrace();
			} catch (InterruptedException e) {
				System.err.println("InterruptedException in function: run(), CrashPreventer multithread");
				e.printStackTrace();
			}
			
		}
	}
	
    /** Gets a quote from the market.
     * Uses the getQuote method from etrade's API
     * Returns the price of the last trade as a float
     * Currently only works for a single company
     */
    private PriceBar getMarketQuote() throws IOException, ETWSException{
    	marketClient = new MarketClient(request);
    	
    	//Insert the names of the companies into the arraylist as strings
    	ArrayList<String> companyList = new ArrayList<String>();
    	companyList.add(companyTicker);
    	
    	//Danger, second argument not documented, not sure what it does
    	QuoteResponse response = marketClient.getQuote(companyList, false ,DetailFlag.ALL);
    	
    	float close = (float)(response.getQuoteData().get(0).getAll().getLastTrade());
    	//I can't see a way to find this, so this is temporary
    	float high = 0;
    	float low = 0;
    	
    	return new PriceBar(high, low, close);	
    }
	
    /**Returns the local high and local low.
     * This resets the values to their original values.
     * 
     * Returns the values in a float array, [0] == low, [1] == high
     * @return float[2]
     * 
     */
    public float[] getLocalExtrema() {
    	float[] extrema = new float[2];
    	
    	extrema[0] = localLow;
    	extrema[1] = localHigh;
    	localLow = Float.MAX_VALUE;
    	localHigh = 0;
    	
    	return extrema;
    }
    
}

