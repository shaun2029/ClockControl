//
// Copyright 2012 Shaun Simpson
// shauns2029@gmail.com
//

package uk.co.immutablefix.ClockControl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class WeatherActivity extends Activity {
	String ipAddress = "192.168.1.101";
    TCPClient tcp;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle bundle = this.getIntent().getExtras();
        
        if(bundle != null)
        {
        	ipAddress = bundle.getString("ipAddress"); 
        }
        
        setContentView(R.layout.weather);

        tcp = new TCPClient();
        tcp.setTimeout(5000);
    } 
    
    @Override
    public void onStart(){
    	super.onStart();
		TextView txtWeather = (TextView) findViewById(R.id.txt_weather);	
		String reply;
		TextView txtReport;
		int[] txtId = {R.id.txt_1, R.id.txt_2, R.id.txt_3, R.id.txt_4, R.id.txt_5};
		int[] imgId = {R.id.img_1, R.id.img_2, R.id.img_3, R.id.img_4, R.id.img_5};
		
		ImageView imgWeather;
		Object content = null;
	
		try {
			reply = tcp.getMessage(getBaseContext(), ipAddress, 44558, "CLOCK:WEATHER");
			if (reply.length() == 0) return;
			
			txtWeather.setText(reply);
			
			reply = tcp.getMessage(getBaseContext(), ipAddress, 44558, "CLOCK:WEATHERIMAGE:0");			
			if (reply.length() == 0) return;

			URL url = new URL(reply);
		    content = url.getContent();

		    InputStream is = (InputStream)content;
		    Drawable image = Drawable.createFromStream(is, "src");
		    imgWeather = (ImageView) findViewById(R.id.img_0);
		    imgWeather.setImageDrawable(image);			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (int i = 0; i < 5; i++)
		{
			try {
				reply = tcp.getMessage(getBaseContext(), ipAddress, 44558, "CLOCK:WEATHER:" + Integer.toString(i));
				txtReport = (TextView) findViewById(txtId[i]);
				txtReport.setText(reply);

				reply = tcp.getMessage(getBaseContext(), ipAddress, 44558, "CLOCK:WEATHERIMAGE:" + Integer.toString(i));			
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
    }    
 }
