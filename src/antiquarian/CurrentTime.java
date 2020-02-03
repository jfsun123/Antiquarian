package antiquarian;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CurrentTime {

	public static int getHour() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH");
        return Integer.parseInt(sdf.format(cal.getTime()));
	}
	
	public static int getMinute() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("mm");
        return Integer.parseInt(sdf.format(cal.getTime()));
	}
	
	public static void main(String[] args) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HHmm");
        System.out.println( sdf.format(cal.getTime()) );

	}

}
