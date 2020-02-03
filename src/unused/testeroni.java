package unused;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class testeroni {
	
	public static void main(String[] args) {
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
		Date date = new Date();
		String finalDay = "";
		String day = df.format(date);
		
		//gets the first 4 for the year number
		for(int i = 0; i < 4; i++) {
			finalDay += day.charAt(i);
		}
		int year = Integer.parseInt(finalDay);
		year--;
		finalDay = Integer.toString(year);
		for(int i = 4; i < day.length(); i++) {
			if(day.charAt(i) == '/') {
				continue;
			}
			finalDay += day.charAt(i);
		}
		//now finalDay should contain the day 1 year ago today.
		finalDay += "000000";
		System.out.print(finalDay);
	}
}
