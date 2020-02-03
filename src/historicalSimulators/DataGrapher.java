package historicalSimulators;

import java.awt.Color;
import java.awt.Toolkit;
import java.util.ArrayList;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.statistics.HistogramDataset;
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
	private DefaultXYDataset XYdataset;
	private HistogramDataset histDataset;
	private ChartPanel chartPanel;

	public DataGrapher(String ticker) {
		super("ULURUUUUU");
		this.ticker = ticker;
	}
	
	/**
	 * Repaints the chart lul.
	 */
	public void repaint() {
		plot.setDataset(XYdataset);
		chartPanel.repaint();
	}
	/**This function creates a histogram.
	 * PREREQS: MUST call createHistogramDataset()
	 * @param counts is the array of numbers of each bar
	 */
	public void drawHistogram(int[] counts) {
		//fill in the dataset first
		double[] histData = new double[11];
		for(int i = 0; i < 11; i++) {
			histData[i] = counts[i];
		}
		
		histDataset = new HistogramDataset();
		histDataset.addSeries("Hist", histData, 44);
		
		JFreeChart sweepHistogram = ChartFactory.createHistogram(
				"Full Nasdaq Sweep results", "percentiles", "counts", histDataset, 
				PlotOrientation.VERTICAL, true, false, false);
		
		chartPanel = new ChartPanel(sweepHistogram);
		chartPanel.setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize()); //hard coded "full screen"
		chartPanel.setMouseWheelEnabled( true );  
		this.setContentPane( chartPanel );
	}
	
	/**This function will create the graph that is displayed.
	 * It includes all the chart options to change chart color..
	 * PREREQS: MUST call createDataset()
	 * Adding data markers for buying/selling points is separately called
	 */
	public void drawTimeChart() {

		//convert the dataset into a chart
		JFreeChart stockChart = ChartFactory.createTimeSeriesChart(             
		         ticker + " Stock Chart", "Time", "Value", XYdataset,             
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
	public void showPurchasePoints(ArrayList<Integer> buyingPoints, ArrayList<Integer> sellingPoints) {
		
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
	
	/**Creates points of lines where to buy and purchase.
	 * This is the overloaded version of the function for the shorting points as well.
	 * Uses data from the buying points and selling points arrays to determine.
	 */
	public void showPurchasePoints(ArrayList<Integer> buyingLongPoints, ArrayList<Integer> sellingLongPoints,
			ArrayList<Integer> buyingShortPoints, ArrayList<Integer> sellingShortPoints) {
		
		//first, add the buying points
		for(int i = 0; i < buyingLongPoints.size(); i++) {
			Marker marker = new ValueMarker(buyingLongPoints.get(i));
			marker.setPaint(Color.GREEN);
			plot.addDomainMarker(marker);
		}
		
		//then, add the selling points
		for(int i = 0; i < sellingLongPoints.size(); i++) {
			Marker marker = new ValueMarker(sellingLongPoints.get(i));
			marker.setPaint(Color.RED);
			plot.addDomainMarker(marker);
		}
		
		for(int i = 0; i < buyingShortPoints.size(); i++) {
			Marker marker = new ValueMarker(buyingShortPoints.get(i));
			marker.setPaint(Color.BLACK);
			plot.addDomainMarker(marker);
		}
		
		for(int i = 0; i < sellingShortPoints.size(); i++) {
			Marker marker = new ValueMarker(sellingShortPoints.get(i));
			marker.setPaint(Color.MAGENTA);
			plot.addDomainMarker(marker);
		}
	}
	
	/** Creates an XY Dataset for the grapher.
	 * This function will work for any number of arguments
	 * @param stockPrices ArrayList of stock closing prices
	 * @param any number of arrayLists of floats which you want to graph
	 */
	public void editDataset(ArrayList<PriceBar> stockPrices, ArrayList<Float>... args) {
		
		XYdataset = new DefaultXYDataset();
		//begin with creating series for the stockPrices
		double[][] stockSeries = new double[2][stockPrices.size()];
	    for (int i = 0; i < stockPrices.size(); i++) {            
	         stockSeries[0][i] = (double)i;            
	         stockSeries[1][i] = (double) stockPrices.get(i).getClose();
	    }
	    XYdataset.addSeries("Stock Price", stockSeries);
	    
	    int index = 1;
	    for(ArrayList<Float> curr : args) {
	    	double[][] currentSeries = new double[2][curr.size()];
	    	for(int i = 0; i < curr.size(); i++) {
	    		currentSeries[0][i] = (double)i;
	    		currentSeries[1][i] = (double)curr.get(i);
	    	}
	    	XYdataset.addSeries("Series " + index, currentSeries);
	    	index++;
	    }
	}
}
