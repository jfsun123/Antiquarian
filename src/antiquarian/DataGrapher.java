package antiquarian;

import java.awt.Color;
import java.awt.Toolkit;
import java.util.LinkedList;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.ui.ApplicationFrame;

/**Separate class that graphs the data given by DataSimulator.
 * Uses the JFreeChart library.
 * @author James Sun
 */

@SuppressWarnings("serial")
public class DataGrapher extends ApplicationFrame{
	
	//Some other stuff to keep the graph nice
	private String ticker;
	
	//global variables to edit the plot
	private XYPlot plot;
	private DefaultXYDataset dataset;
	private ChartPanel chartPanel;

	public DataGrapher(String ticker) {
		super("ULURUUUUU");
		this.ticker = ticker;
	}
	
	/**
	 * Repaints the chart lul.
	 */
	public void repaint() {
		plot.setDataset(dataset);
		chartPanel.repaint();
	}
	
	/**This function will create the graph that is displayed.
	 * It includes all the chart options to change chart color..
	 * PREREQS: MUST call createDataset()
	 * Adding data markers for buying/selling points is separately called
	 */
	public void drawChart() {

		//convert the dataset into a chart
		JFreeChart stockChart = ChartFactory.createTimeSeriesChart(             
		         ticker + " Stock Chart", "Time", "Value", dataset,             
		         true, false, false);
		
		NumberAxis xAxis = new NumberAxis();
		xAxis.setAutoTickUnitSelection(true);
		xAxis.setLabel("Time");
		
		plot = (XYPlot)stockChart.getPlot();
		plot.setDomainAxis(xAxis);
		plot.setBackgroundPaint(Color.WHITE); //uncomment if you want the background to be white
		
        //make sure that the chart actually appears on the screen
        chartPanel = new ChartPanel(stockChart);
		chartPanel.setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize()); //hard coded "full screen"
		chartPanel.setMouseWheelEnabled( true );  
		this.setContentPane( chartPanel );
	}
	
	/**Creates points of lines where to buy and purchase
	 * Uses data from the buying points and selling points arrays to determine.
	 */
	public void showPurchasePoints(LinkedList<Integer> buyingPoints, LinkedList<Integer> sellingPoints) {
		
		//first, add the buying points
		for(int i = 0; i < buyingPoints.size(); i++) {
			Marker marker = new ValueMarker(buyingPoints.get(i));
			marker.setPaint(Color.GREEN);
			plot.addDomainMarker(marker);
		}
		
		//then, add the selling points
		for(int i = 0; i < sellingPoints.size(); i++) {
			Marker marker = new ValueMarker(sellingPoints.get(i));
			marker.setPaint(Color.RED);
			plot.addDomainMarker(marker);
		}
	}
	
	/** Creates an XY Dataset for the grapher.
	 * This function is taylored to keltner channels.
	 * @param stockPrices ArrayList of stock closing prices
	 * @param MACD ArrayList of MACD values
	 * @param EMA9 ArrayList of EMA 9 period of MACD
	 */
	public void editDataset(LinkedList<PriceBar> stockPrices, LinkedList<Float> middleChannel, 
			LinkedList<Float> upperChannel, LinkedList<Float> lowerChannel) {
		
		//begin with creating series for the stockPrices
		double[][] stockSeries = new double[2][stockPrices.size()];
	    for (int i = 0; i < stockPrices.size(); i++) {            
	         stockSeries[0][i] = (double)i;            
	         stockSeries[1][i] = (double) stockPrices.get(i).getClose();
	    }
	    
	    double[][] middleSeries = new double[2][middleChannel.size()];
	    for (int i = 0; i < middleChannel.size(); i++) {            
	         middleSeries[0][i] = (double)i;            
	         middleSeries[1][i] = (double) middleChannel.get(i);
	    }
	    
	    double[][] upperSeries = new double[2][upperChannel.size()];
	    for (int i = 0; i < upperChannel.size(); i++) {            
	         upperSeries[0][i] = (double)i;            
	         upperSeries[1][i] = (double) upperChannel.get(i);
	    }
	    
	    double[][] lowerSeries = new double[2][lowerChannel.size()];
	    for(int i = 0; i < lowerChannel.size(); i++) {
	    	lowerSeries[0][i] = (double)i;
	    	lowerSeries[1][i] = (double)lowerChannel.get(i);
	    }
	    
	    //then put them all into one dataset
	    dataset = new DefaultXYDataset();
	    dataset.addSeries("Stock Price", stockSeries);
	    dataset.addSeries("Middle Channel", middleSeries);
	    dataset.addSeries("Upper hannel", upperSeries);
	    dataset.addSeries("Lower Channel", lowerSeries);
	}
}
