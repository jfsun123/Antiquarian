package historicalSimulators;

/**PriceBar class.
 * Stores the high, low and closing price of the stock at a certain point.
 * Includes relevant functions.
 * @author James Sun
 */

public class PriceBar {
	
	private float high;
	private float low;
	private float close;
	
	public PriceBar(float high, float low, float close) {
		this.high = high;
		this.low = low;
		this.close = close;
	}
	
	public String toString() {
		return "High: " + high + " Low: " + low + " Close: " + close;
	}
	
	public float getHigh() {return high;}
	public float getLow() {return low;}
	public float getClose() {return close;}
}
