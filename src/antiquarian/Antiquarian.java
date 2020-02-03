package antiquarian;

import com.etrade.etws.market.DetailFlag;
import com.etrade.etws.market.QuoteResponse;
import com.etrade.etws.oauth.sdk.client.IOAuthClient;
import com.etrade.etws.oauth.sdk.client.OAuthClientImpl;
import com.etrade.etws.oauth.sdk.common.Token;
import com.etrade.etws.sdk.client.ClientRequest;
import com.etrade.etws.sdk.client.Environment;
import com.etrade.etws.sdk.client.MarketClient;
import com.etrade.etws.sdk.common.ETWSException;

import java.io.IOException;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import java.util.concurrent.TimeUnit;
import java.util.ArrayList;


//Currently only buys 100 stock at a time
//Doesn't know which account to buy to
//What happens after hours (the second argument to getQuote)

public class Antiquarian
{
	//this is the company and data
	private static String companyTicker = "AMD"; 
	private static float stopPercent = (float)0.02; //how much you can drop before it is defined as a crash
	
	//Authorization Variables
	private static String consumerKey = "yourSecretKey";
	private static String consumerSecret = "yourConsumerSecret";
    private static String oauth_access_token = null;
    private static String oauth_access_token_secret = null;
	//Client Variables
	private static ClientRequest request;
	private static MarketClient markClient;
	
	//Object which graphs the data on the screen
	private static DataGrapher dataGraph;
	
	//Use this calculator to make the decisions
	private static KeltnerCalculator calculator;
	
	//The crashPreventer object, thread to detect "black swan" events
	private static CrashPreventer crashPrevent;
	
	//purchase manager to interact with ETRADE
	private static PurchaseManager purchaseManager;
	
	//account manager to hold your account number and ask user for which account to use
	private static AccountManager accountManager;
	
    public static void main(String[] args) throws IOException, ETWSException, InterruptedException
    {
    	oauthVerify(consumerKey, consumerSecret);
    	
    	initializeClientRequest();
    	initializeAccountManager();
    	initializeKeltnerCalculator();
    	initializeCrashPreventer();
    	initializeDataGrapher();
    	
    	runKeltnerSimulation();
    }
    
    /**Runs the simulation using the keltner calculator. 
     * Saves the data results into a txt file in StockData
     */
    private static void runKeltnerSimulation() {
    	for(;;) {
    		//begin infinite while
    		try {
        		//if 6 AM here, market is opening
        		if(CurrentTime.getHour() == 6) {
        			purchaseManager.setPurchase(true);
        		}
        		
        		//if you can make purchase, continue
				if(purchaseManager.getPurchase()) {
					
					PriceBar currentPrice = getMarketQuote();
					
					if(purchaseManager.getOwned()) {
						//if you own it, then you should look to sell
						if(calculator.checkSell(currentPrice))
							purchaseManager.simulateOrder(false, currentPrice);
					}
					else {
						//if you don't own the stock, look to buy
						if(calculator.checkBuy(currentPrice))
							purchaseManager.simulateOrder(true, currentPrice);
					}
					
	        		//stop trading within the last 30 minutes of market hours
	        		if(CurrentTime.getHour() == 13 && CurrentTime.getMinute() > 30) {
	        			purchaseManager.setPurchase(false);
	        			calculator.addData(currentPrice);
						dataGraph.editDataset(calculator.getStockPrices(), calculator.getMiddleChannel(),
								calculator.getUpperChannel(), calculator.getLowerChannel());
						dataGraph.repaint();
						
						FilePrinter.printData("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
						FilePrinter.printData("End of day.  New closing price is $" + currentPrice);
	        		}
				}

				//wait 15 minutes for next test
				TimeUnit.MINUTES.sleep(15);
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ETWSException e) {
				e.printStackTrace();
			}
    	}
    }
    
    /** Gets a quote from the market.
     * Uses the getQuote method from etrade's API
     * Returns the price of the last trade as a float
     * Currently only works for a single company
     */
    private static PriceBar getMarketQuote() throws IOException, ETWSException{
    	markClient = new MarketClient(request);
    	
    	//Insert the names of the companies into the arraylist as strings
    	ArrayList<String> companyList = new ArrayList<String>();
    	companyList.add(companyTicker);
    	
    	//Danger, second argument not documented, not sure what it does
    	QuoteResponse response = markClient.getQuote(companyList, false ,DetailFlag.ALL);
    	
    	float close = (float)(response.getQuoteData().get(0).getAll().getLastTrade());
    	
    	//gets the high and low for the last 10 minutes using the crash preventer
    	float[] extrema = crashPrevent.getLocalExtrema();
    	float high = extrema[1];
    	float low = extrema[0];
    	
    	return new PriceBar(high, low, close);	
    }
    
    /**Initializes keltner calculator.
     * Downloads and loads the ArrayList of data for the calculator.
     */
    private static void initializeKeltnerCalculator() {
    	System.out.println("Downloading historical data.");
		File fileSave = new File("StockData/Minute1BilCap/" + companyTicker + ".csv");
		fileSave.delete(); //remove the file so you can redownload more recent data.
		try {
			//redownload the data, this time it's more recent
			calculator = new KeltnerCalculator(HistoryDataReader.fillStockPrices(15, companyTicker));
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    /**Initializes the dataGrapher
     * It creates and makes the new graph show up on the screen
     */
    private static void initializeDataGrapher() {
    	
    	dataGraph = new DataGrapher(companyTicker);
		dataGraph.editDataset(calculator.getStockPrices(), calculator.getMiddleChannel(),
					calculator.getUpperChannel(), calculator.getLowerChannel());
		dataGraph.drawChart();
		dataGraph.pack();
		dataGraph.setVisible(true);
    }
    
    /**Initializes the crashPreventer.
     * Sets it up and starts the new thread.
     */
    private static void initializeCrashPreventer() {
    	
    	System.out.println("Initializing Crash Preventer");
    	purchaseManager = new PurchaseManager(companyTicker, request);
    	crashPrevent = new CrashPreventer(markClient, request, companyTicker, stopPercent, purchaseManager);
    	Thread crashThread = new Thread(crashPrevent);
    	crashThread.start();
    }
    
    /** Initializes the client request.
     *  Sets up the request to contain all of the relevant data.
     */
    private static void initializeClientRequest() {
    	System.out.println("Initializing Client Request.");
    	request = new ClientRequest();
    	
    	request.setEnv(Environment.LIVE); //change to SANDBOX
    	request.setConsumerKey(consumerKey);
    	request.setConsumerSecret(consumerSecret);
    	request.setToken(oauth_access_token);
    	request.setTokenSecret(oauth_access_token_secret);
    }
    
    /**Initializes the account manager.
     * Asks user which account to use.
     */
    private static void initializeAccountManager() {
    	accountManager = new AccountManager(request);
    	accountManager.setAccountNum();
    }
    
    public static String get_verification_code() {

        try{
            BufferedReader br =
            new BufferedReader(new InputStreamReader(System.in));

            String input;

            input=br.readLine();
            return input;


        }catch(IOException io){
            io.printStackTrace();
            return "";
        }
    }
    
    private static void oauthVerify(String key, String secret) throws IOException, ETWSException{

        String oauth_consumer_key           = key; // Your consumer key
        String oauth_consumer_secret        = secret; // Your consumer secret
        String oauth_request_token          = null; // Request token
        String oauth_request_token_secret   = null; // Request token secret
        String oauth_verify_code            = null;



        ClientRequest request       = new ClientRequest();
        IOAuthClient client         = OAuthClientImpl.getInstance(); // Instantiate IOAUthClient

	        // Instantiate ClientRequest
	        request.setEnv(Environment.LIVE); // change to SANDBOX
	        request.setConsumerKey(oauth_consumer_key); //Set consumer key
	        request.setConsumerSecret(oauth_consumer_secret);
	        Token token = client.getRequestToken(request); // Get request-token object
	
	        oauth_request_token         = token.getToken(); // Get token string
	        oauth_request_token_secret  = token.getSecret(); // Get token secret
	
	        request.setToken(oauth_request_token);
	        request.setTokenSecret(oauth_request_token_secret);
	
	        String authorizeURL = null;
	        authorizeURL = client.getAuthorizeUrl(request);
	        java.awt.Desktop.getDesktop().browse(java.net.URI.create(authorizeURL)); //Maybe this will be cleaner
	        System.out.println(authorizeURL);
	        System.out.println("Copy the verification code here.");
	        oauth_verify_code = get_verification_code();
	        //oauth_verify_code = Verification(client,request);
	
	        request.setVerifierCode(oauth_verify_code);
	        token = client.getAccessToken(request);
	        oauth_access_token = token.getToken();
	        oauth_access_token_secret = token.getSecret();
	
	        request.setToken(oauth_access_token);
	        request.setTokenSecret(oauth_access_token_secret);
    }

}