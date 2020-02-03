package antiquarian;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;

import com.etrade.etws.account.AccountBalanceResponse;
import com.etrade.etws.account.AccountListResponse;
import com.etrade.etws.account.AccountPositionsRequest;
import com.etrade.etws.account.AccountPositionsResponse;
import com.etrade.etws.sdk.client.AccountsClient;
import com.etrade.etws.sdk.client.ClientRequest;
import com.etrade.etws.sdk.common.ETWSException;


/**The class that will manage interactions with your ETRADE account.
 * @author James Sun
 */

public class AccountManager {
	
	//The user set account number to use
	private String accountNum;
	
	//Etrade variables
	private AccountsClient accountClient;

	public AccountManager(ClientRequest request) {
		accountClient = new AccountsClient(request);
		accountNum = "NONE";
	}
	
	/**Perform setup to decide which account to use.  
	 * Queries user and saves the result into accountNum.
	 */
	public void setAccountNum() {
		ArrayList<String> accountNums = new ArrayList<String>();
		try {

			AccountListResponse response = accountClient.getAccountList();
			
			for(int i = 0; i < response.getResponse().size(); i++) 
				accountNums.add(response.getResponse().get(i).getAccountId());
			
			//ask user which account to use
			do {
				System.out.println("\n\n\nPlease type the account number you want to use.");
				for(int i = 0; i < accountNums.size(); i++)
					System.out.println(accountNums.get(i));
				
	            BufferedReader br =
	            new BufferedReader(new InputStreamReader(System.in));
	
	            String input;
	            input=br.readLine();
	            boolean shouldBreak = false;
	            for(int i = 0; i < accountNums.size(); i++) {
	            	//if the account number is one of the ones you can use
	            	if(input.equals(accountNums.get(i))) {
	            		accountNum = input;
	            		System.out.println("Using account number: " + accountNum);
	            		shouldBreak = true;
	            		break;
	            	}
	            }
				
	            if(shouldBreak)
	            	break;
			} while(true);
		} catch (IOException e) {
			System.err.println("IOException in function setAccountNum()");
			e.printStackTrace();
		} catch (ETWSException e) {
			System.err.println("ETWSException in function setAccountNum()");
			e.printStackTrace();
		}

	}//end get account number
	
	/**This function finds the number of stocks you own of a certain ticker.
	 * Returns 0 if none are found in the first 25 stocks you own.
	 * @param ticker the ticker of the stock you want to find quantity of.
	 * @return the quantity of that stock you own.
	 */
	public int getNumStocks(String ticker) throws ETWSException, IOException{
		AccountPositionsResponse aprs = null;
		AccountPositionsRequest apr = new AccountPositionsRequest();
		apr.setCount("25"); // count is set to 25
		apr.setSymbol(ticker); // insert desired symbol
		apr.setTypeCode("EQ"); // set type code to EQ, OPTN, MF, or BOND
		aprs = accountClient.getAccountPositions(accountNum, apr);
		
		int numStocks = 0;
		for(int i = 0; i < aprs.getResponse().size(); i++) {
			if(ticker.equals(aprs.getResponse().get(i).getProductId().getSymbol())) {
				//when they have the same symbol
				numStocks = aprs.getResponse().get(i).getQty().intValue();
			}
		}
		
		return numStocks;
	}
	
	public BigDecimal getAccountBalance() throws ETWSException, IOException{
		AccountBalanceResponse balance = accountClient.getAccountBalance(accountNum);
		return balance.getCashAccountBalance().getCashAvailableForInvestment();
	}
	public String getAccountNum() {return accountNum;}
}
