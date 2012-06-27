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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ClockControlActivity extends Activity {
	String ipAddress = "192.168.1.101";
	String weather = "";
	Boolean running = true, paused = true;

	UDP udp;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        udp = new UDP();
        
        Log.d("Events", "Starting ... ");
       
    	
    	final TextView txtPlaying = (TextView) findViewById(R.id.txt_playing);	    

        Button btnVolUp = (Button) findViewById(R.id.btn_volup);
	    btnVolUp.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					udp.sendUDPMessage(ipAddress, 44558, "CLOCK:VOLUP");
					Log.d("Events", "Next");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});	

	    Button btnVolDown = (Button) findViewById(R.id.btn_voldown);
	    btnVolDown.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					udp.sendUDPMessage(ipAddress, 44558, "CLOCK:VOLDOWN");
					Log.d("Events", "Next");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});	
	
	    Button btnNext = (Button) findViewById(R.id.btn_next);
	    btnNext.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					udp.sendUDPMessage(ipAddress, 44558, "CLOCK:NEXT");
					Log.d("Events", "Next");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});	
	
	    Button btnMusic = (Button) findViewById(R.id.btn_music);
	    btnMusic.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					udp.sendUDPMessage(ipAddress, 44558, "CLOCK:MUSIC");
					Log.d("Events", "Music");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});	
	
	    Button btnSleep = (Button) findViewById(R.id.btn_sleep);
	    btnSleep.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					udp.sendUDPMessage(ipAddress, 44558, "CLOCK:SLEEP");
					Log.d("Events", "Sleep");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});	
	
	    Button btnPause = (Button) findViewById(R.id.btn_pause);
	    btnPause.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					udp.sendUDPMessage(ipAddress, 44558, "CLOCK:PAUSE");
					Log.d("Events", "Sleep");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});	

	    final Handler handler = new Handler();
	    
		Runnable runnable = new Runnable() {
		    String reply;
		    
			@Override
			public void run() {
				int time = 2000;
				
				while (running) {
			    	
					try {
						Thread.sleep(time);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					if (!paused) {
						try {
							Log.d("TREAD", "Getting data ...");
							reply = udp.getUDPMessage(ipAddress, 44558, "CLOCK:PLAYING");
							
							// If no reply sleep longer
							if (reply == ""){
								time = 30000;
							} else {
								time = 2000;
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					handler.post(new Runnable() {
						@Override
						public void run() {
							if ((reply != "") && (reply != txtPlaying.getText())) txtPlaying.setText(reply);
						}
					});
				}
			}
		};
		new Thread(runnable).start();

		Log.d("Events", "Starting ... Fin");
    }
    
    public void onStart()
    {
    	super.onStart();
    	
		Log.d("Events", "onStart");

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		ipAddress = prefs.getString("clock_address", ipAddress);
		
		Log.d("Preferences", ipAddress);
		
		paused = false;
	}
    
    public void onPause()
    {
    	super.onPause();    	
    	paused = true;
    }
    
    public void onReStart()
    {
    	super.onRestart();   	
    	paused = false;
    }
    
    //Creates menus
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.menu, menu);
    	return true;
    }
    
    //Handles menu clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Log.d("MENUS", "MENUS");
    	switch(item.getItemId()) {
    	case R.id.mitmWeather:
    		startActivity(new Intent(this, WeatherActivity.class));
    		return true;
    	case R.id.mitmPreferences:
    		startActivity(new Intent(this, Preferences.class));
    		return true;
    	case R.id.mitmQuit:
    		finish();
    		break;
    	}
    	
    	return false;
    }    
  
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	running = false;
    	udp.close();
    }
}
