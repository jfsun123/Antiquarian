package historicalSimulators;

import java.util.ArrayList;
import java.util.LinkedList;

public class Statistics {
	
	//calculates the avverage of an arraylist
	public static float mean(ArrayList<Float> values) {
		
		float sum = 0;
		for(int i = 0; i < values.size(); i++) {
			sum += values.get(i);
		}
		return sum / values.size();
	}
	
	public static float standardDev(ArrayList<Float> values) {
		float mean = mean(values);
		
		float sum = 0;
		for(int i = 0; i < values.size(); i++) {
			sum += Math.pow((values.get(i) - mean), 2);
		}
		sum /= values.size();
		return (float)Math.sqrt((double)sum);
	}
	
	/**This function finds the maxes of the arrayList stockPrices
	 * CARE THIS FUNCTION NOT OPTIMIZED, O(n^2)
	 * @param stockPrices the array of stock prices you want to get the interval day maxes of
	 * @return the array of how many interval day maxes
	 */
	public static ArrayList<Float> calculateMaxes(int intervals, ArrayList<PriceBar> stockPrices){
		if(stockPrices.size() < intervals)
			//mate you gotta give me data
			return null;
		
		float max = 0;
		LinkedList<Float> currentPrices = new LinkedList<Float>();
		for(int i = 0; i < intervals; i++) {
			currentPrices.add(stockPrices.get(i).getClose());
			if(stockPrices.get(i).getClose() > max)
				max = stockPrices.get(i).getClose();
		}
		
		ArrayList<Float> maxes = new ArrayList<Float>();
		maxes.add(max);
		
		for(int i = intervals; i < stockPrices.size(); i++) {
			currentPrices.removeFirst();
			currentPrices.addLast(stockPrices.get(i).getClose());
			max = 0;
			for(int j = 0; j < currentPrices.size(); j++) {
				if(currentPrices.get(j) > max)
					max = currentPrices.get(j);
			}
			maxes.add(max);
		}
		return maxes;
	}
	
	/**This function finds the mins of the arrayList stockPrices
	 * @param stockPrices the array of stock prices you want to get the interval day mins of
	 * @return the array of how many interval day mins
	 */
	public static ArrayList<Float> calculateMins(int intervals, ArrayList<PriceBar> stockPrices){
		if(stockPrices.size() < intervals)
			//mate you gotta give me data
			return null;
		
		float min = Float.MAX_VALUE;
		LinkedList<Float> currentPrices = new LinkedList<Float>();
		for(int i = 0; i < intervals; i++) {
			currentPrices.add(stockPrices.get(i).getClose());
			if(stockPrices.get(i).getClose() < min)
				min = stockPrices.get(i).getClose();
		}
		
		ArrayList<Float> mins = new ArrayList<Float>();
		mins.add(min);
		
		for(int i = intervals; i < stockPrices.size(); i++) {
			currentPrices.removeFirst();
			currentPrices.addLast(stockPrices.get(i).getClose());
			min = Float.MAX_VALUE;
			for(int j = 0; j < currentPrices.size(); j++) {
				if(currentPrices.get(j) < min)
					min = currentPrices.get(j);
			}
			mins.add(min);
		}
		return mins;
	}
}
