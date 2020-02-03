package antiquarian;

import java.io.IOException;
import java.math.BigInteger;

import com.etrade.etws.order.EquityOrderAction;
import com.etrade.etws.order.EquityOrderRequest;
import com.etrade.etws.order.EquityOrderRoutingDestination;
import com.etrade.etws.order.EquityOrderTerm;
import com.etrade.etws.order.EquityPriceType;
import com.etrade.etws.order.MarketSession;
import com.etrade.etws.order.PlaceEquityOrder;
import com.etrade.etws.order.PlaceEquityOrderResponse;
import com.etrade.etws.sdk.client.ClientRequest;
import com.etrade.etws.sdk.client.OrderClient;
import com.etrade.etws.sdk.common.ETWSException;

/** PurchaseManager class.
 *  Manages all of the purchase requests sent to ETRADE.
 * 
 * @author James Sun
 */

public class PurchaseManager{
	
	//basic saved variables
	private String companyTicker;
	private int orderNumber;
	private float buyPrice; //to calculate profits
	private boolean canPurchase = true; //this is to make sure you only buy once per 15 minutes
	private boolean isOwned;
	private float currentMoney = 5000;
	private int numShares = 0;
	
	//Etrade variables
	private OrderClient ordClient;
	

	public PurchaseManager(String companyTicker, ClientRequest request) {
		ordClient = new OrderClient(request);
		orderNumber = 0;
		isOwned = false;
	}
    
	//this is for the simulation
	public void stopLoss(PriceBar price){
			//immediately place a sell
			try {
				FilePrinter.printData("Crash has occurred, selling stocks.");
			} catch (IOException e) {
				e.printStackTrace();
			}
			simulateOrder(false, price);
			
	} 
	
    /**This function is what happens when you get the signal from the crashPreventer.
     * When a crash has occurred, this triggers and you immediately sell
     */
	public void stopLoss(){
		try {
			//immediately place a sell
			System.out.println("Crash has occurred, selling stocks.");
			placeOrder(false);
		} catch (IOException e) {
			System.err.println("IOException in function: trigger()");
			e.printStackTrace();
		} catch (ETWSException e) {
			System.err.println("ETWSException in function: trigger()");
			e.printStackTrace();
		}
	} 
	
	//this is just for the real time simulation
	public void simulateOrder(boolean exchangeStatus, PriceBar currentPrice) {
		if(exchangeStatus) {
			buyPrice = currentPrice.getClose();
			numShares = (int) (currentMoney / buyPrice);
			currentMoney -= numShares * buyPrice;
			currentMoney -= 7; //trading price
			isOwned = true;
			canPurchase = false;
			try {
				FilePrinter.printBuy(buyPrice, numShares, currentMoney);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			currentMoney += numShares * currentPrice.getClose(); 
			currentMoney -= 7; //trading price
			isOwned = false;
			canPurchase = false;
			try {
				FilePrinter.printSell(currentPrice.getClose(), numShares, currentMoney);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
    /** Places orders on ETRADE.
     *  This method will place both buying and selling orders on the ETRADE server
     *  ExchangeStatus = true for buy
     *  ExchangeStatus = false for sell
     */
    public void placeOrder(boolean exchangeStatus) throws IOException, ETWSException {

    	orderNumber++; //has a unique order number each time
    	
    	PlaceEquityOrder orderRequest = new PlaceEquityOrder(); 
    	EquityOrderRequest eor = new EquityOrderRequest(); 
    	eor.setAccountId("83405188"); // TODO: Replace with whatever account you want to use
    	eor.setSymbol(companyTicker);
    	eor.setAllOrNone("FALSE"); 
    	eor.setClientOrderId(Integer.toString(orderNumber));
    	eor.setOrderTerm(EquityOrderTerm.GOOD_FOR_DAY); 
    	
    	//If exchangeStatus is true, you buy
    	if(exchangeStatus) {
    		eor.setOrderAction(EquityOrderAction.BUY);
    	}else {
    		//otherwise, sell
    		eor.setOrderAction(EquityOrderAction.SELL);
    	}
    	eor.setMarketSession(MarketSession.REGULAR); 
    	eor.setPriceType(EquityPriceType.MARKET); 
    	eor.setQuantity(new BigInteger("100"));  //TODO make sure this is the right number
    	eor.setRoutingDestination(EquityOrderRoutingDestination.AUTO.value()); 
    	eor.setReserveOrder("FALSE"); 
    	orderRequest.setEquityOrderRequest(eor); 
    	
    	PlaceEquityOrderResponse response = ordClient.placeEquityOrder(orderRequest);
    	
    	printOrder(exchangeStatus, response);
    }
    
    /**Prints the order out into the console for easy reading
     * @param exchangeStatus
     * @param response
     */
    private void printOrder(boolean exchangeStatus, PlaceEquityOrderResponse response) {
    	
    	System.out.print("**********************************");
    	System.out.println("Order Time: " + response.getEquityOrderResponse().getOrderTime());
    	System.out.println("Order Number: " + response.getEquityOrderResponse().getOrderNum());
    	if(exchangeStatus) {
    		System.out.println("BOUGHT " + response.getEquityOrderResponse().getQuantity() + 
    				" shares of " + response.getEquityOrderResponse().getSymbol());
    		isOwned = true;
    	}
    	else {
    		System.out.println("SOLD " + response.getEquityOrderResponse().getQuantity() + 
    				" shares of " + response.getEquityOrderResponse().getSymbol());
    		isOwned = false;
    	}
    	System.out.println("**********************************");
    } //end printOrder function
    
    //getter methods
    public boolean getPurchase() {return canPurchase;}
    public boolean getOwned() {return isOwned;}
    public float getMoney() {return currentMoney;}
    
    //setter methods
    public void setPurchase(boolean status) {canPurchase = status;}
}//end purchaseManager class
