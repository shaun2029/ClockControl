//
// Copyright 2012 Shaun Simpson
// shauns2029@gmail.com
//

package uk.co.immutablefix.ClockControl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ImageView;
import android.widget.TextView;

public class WeatherActivity extends Activity {
	String ipAddress = "192.168.1.101";
    UDP udp;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather);

        udp = new UDP();
        
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());		
		ipAddress = prefs.getString("clock_address", ipAddress);
    } 
    
    @Override
    public void onStart(){
    	super.onStart();
		TextView txtWeather = (TextView) findViewById(R.id.txt_weather);	
	
		try {
			String reply = udp.getUDPMessage(getBaseContext(), ipAddress, 44558, "CLOCK:WEATHER");
			
			txtWeather.setText(reply);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String reply;
		TextView txtReport;
		int[] txtId = {R.id.txt_1, R.id.txt_2, R.id.txt_3, R.id.txt_4, R.id.txt_5};
		int[] imgId = {R.id.img_1, R.id.img_2, R.id.img_3, R.id.img_4, R.id.img_5};
		
		ImageView imgWeather;
		Object content = null;
		
		for (int i = 0; i < 5; i++)
		{
			try {
				reply = udp.getUDPMessage(getBaseContext(), ipAddress, 44558, "CLOCK:WEATHER:" + Integer.toString(i));
				txtReport = (TextView) findViewById(txtId[i]);
				txtReport.setText(reply);

				reply = udp.getUDPMessage(getBaseContext(), ipAddress, 44558, "CLOCK:WEATHERIMAGE:" + Integer.toString(i));			
				URL url = new URL(reply);
			    content = url.getContent();

			    InputStream is = (InputStream)content;
			    Drawable image = Drawable.createFromStream(is, "src");
			    imgWeather = (ImageView) findViewById(imgId[i]);
			    imgWeather.setImageDrawable(image);			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
    }
    
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	udp.close();
    }
    
 }
