//
// Copyright 2012 Shaun Simpson
// shauns2029@gmail.com
//

package uk.co.immutablefix.ClockControl;

import java.io.IOException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

import uk.co.immutablefix.ClockControl.Preferences;


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

		TextView txtWeather = (TextView) findViewById(R.id.txt_weather);	

		try {
			String reply = udp.getUDPMessage(ipAddress, 44558, "CLOCK:WEATHER");
			
			if ((reply != "") && (reply != txtWeather.getText())) txtWeather.setText(reply);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    } 
    
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	udp.close();
    }
    
 }
