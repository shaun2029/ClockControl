// Copyright 2012 Shaun Simpson shauns2029@gmail.com

package uk.co.immutablefix.ClockControl;
import uk.co.immutablefix.ClockControl.StationPickerActivity;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class ClockControlActivity extends Activity {
	private static final int GET_RADIO_STATION = 3007;
	
	String hosts = "";
	String[] clocks = null;	

	String hostname = "";
	String weather = "";
	String radioStations = "";
	Boolean running = true;
	long backPressed = 0;
	int radioChannel = 0;

	DnssdDiscovery dns;
	TCPClient tcp;
	
	Button btnVolDown, btnVolUp;
	//RadioButton rbtnClock1, rbtnClock2;
	TextView txtPlaying;
	SharedPreferences prefs = null;
	
	Thread thread = null;
	

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
       
        dns = DnssdDiscovery.getInstance(getBaseContext());
        
        tcp = new TCPClient();
        tcp.setTimeout(1000);
        
        //  Log.d("Events", "Starting ... ");
    	
    	txtPlaying = (TextView) findViewById(R.id.txt_playing);	    
        btnVolUp = (Button) findViewById(R.id.btn_volumeup);
	    btnVolUp.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendCommand(44558, "CLOCK:VOLUP");
			}
		});	

	    btnVolDown = (Button) findViewById(R.id.btn_volumedown);
	    btnVolDown.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendCommand(44558, "CLOCK:VOLDOWN");
			}
		});	
	
	    Button btnNext = (Button) findViewById(R.id.btn_next);
	    btnNext.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendCommand(44558, "CLOCK:NEXT");
			}
		});	
	
	    Button btnMusic = (Button) findViewById(R.id.btn_music);
	    btnMusic.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendCommand(44558, "CLOCK:MUSIC");
			}
		});	
	
	    Button btnSleep = (Button) findViewById(R.id.btn_sleep);
	    btnSleep.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendCommand(44558, "CLOCK:SLEEP");
				//  Log.d("Events", "Sleep");
			}
		});	
	
	    Button btnMeditation = (Button) findViewById(R.id.btn_meditation);
	    btnMeditation.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendCommand(44558, "CLOCK:MEDITATION");
				//  Log.d("Events", "Meditation");
			}
		});	

	    Button btnRadio = (Button) findViewById(R.id.btn_radio);
	    btnRadio.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				launchStationPicker();			
			}
		});	

	    Button btnDisplay = (Button) findViewById(R.id.btn_display);
	    btnDisplay.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendCommand(44558, "CLOCK:DISPLAY:TOGGLE");
				//  Log.d("Events", "Meditation");
			}
		});	
	    
	    Button btnPause = (Button) findViewById(R.id.btn_pause);
	    btnPause.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendCommand(44558, "CLOCK:PAUSE");
				//  Log.d("Events", "Sleep");

			}
		});	
/*	    
	    rbtnClock1 = (RadioButton) findViewById(R.id.radioButton1);
	    rbtnClock2 = (RadioButton) findViewById(R.id.radioButton2);	    
	    
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		rbtnClock1.setText(prefs.getString("clock1_name", (String) rbtnClock1.getText()));
		rbtnClock2.setText(prefs.getString("clock2_name", (String) rbtnClock2.getText()));
	
	    rbtnClock1.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				updatePlaying();
			}
		});			
	    
	    rbtnClock2.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				updatePlaying();
			}
		});	
*/
		//  Log.d("Events", "Starting ... Fin");
     }

	private void launchStationPicker() {
		if (radioStations != "") {
			Intent intent = new Intent(this, StationPickerActivity.class);
			intent.putExtra(StationPickerActivity.KEY_RADIO_STATIONS, radioStations);
			startActivityForResult(intent, GET_RADIO_STATION);
		} else {
			sendCommand(44558, "CLOCK:RADIO:TOGGLE" + String.valueOf(radioChannel));
		}
	}
/*
	private String[] getClocks() {
    	TCPClient tcp2;
        tcp2 = new TCPClient();
        tcp2.setTimeout(10000);

		Runnable runnable = new Runnable() {
		    String reply;
		    
			@Override
			public void run() {
					tcp.acquireMulticastLock(getBaseContext());
					reply = tcp.getMessage(getBaseContext(), "192.168.0.72", 44558, "CLOCK:DISCOVER");
					tcp.releaseMulticastLock();

					handler.post(new Runnable() {
						@Override
						public void run() {
							if ((reply != null) && (reply.length() > 0) && (!reply.equals(txtPlaying.getText()))) 
								hosts = reply;
						}
					});			
				
				return;
			}
		};
		
		runnable.run();
		try {
			runnable.wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return hosts.split(System.getProperty("line.separator"));
	}
*/	
	// Listen for results.
	@Override  
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
	    // See which child activity is calling us back.
	    switch (requestCode) {
	        case GET_RADIO_STATION:
	            // This is the standard resultCode that is sent back if the
	            // activity crashed or didn't doesn't supply an explicit result.
	        	if (resultCode == RESULT_OK){
	        		radioChannel = data.getIntExtra("station", 0);
	        		sendCommand(44558, "CLOCK:SET:RADIOSTATION:" + String.valueOf(radioChannel));
	        	}
	        default:
	            break;
	    }
	}		

    final Handler handler = new Handler();
    final Handler stationHandler = new Handler();
    final Handler clocksHandler = new Handler();
    Runnable runnable = new Runnable() {
	    String reply, stationReply, clocksReply;
	    
		@Override
		public void run() {
			int time = 500;
			String targetIP = "";
			
			while (running) {
				targetIP = getTargetIp();
				
				if (targetIP != "") {
					time = 500;
					
					if (radioStations == "") {
						stationReply = tcp.getMessage(getBaseContext(), targetIP, 44558, "CLOCK:GET:RADIOSTATIONS");
		
						stationHandler.post(new Runnable() {
							@Override
							public void run() {
								if ((stationReply != null) && (stationReply.length() > 0)) 
									radioStations = stationReply;
							}
						});
					}
				
					//  Log.d("TREAD", "Getting data ...");
					reply = tcp.getMessage(getBaseContext(), targetIP, 44558, "CLOCK:PLAYING");
	
					handler.post(new Runnable() {
						@Override
						public void run() {
							if ((reply != null) && (reply.length() > 0) && (!reply.equals(txtPlaying.getText()))) 
								txtPlaying.setText(reply);
						}
					});			
				} else {
					time = 3000;
					//  Log.d("TREAD", "Getting data ...");
					tcp.acquireMulticastLock(getBaseContext());
					clocksReply = tcp.getMessage(getBaseContext(), "192.168.255.255", 44558, "CLOCK:DISCOVER");
					tcp.releaseMulticastLock();
					
					clocksHandler.post(new Runnable() {
						@Override
						public void run() {
							if ((clocksReply != null) && (clocksReply.length() > 0)) 
								clocks = clocksReply.split(System.getProperty("line.separator"));
						}
					});			
				}
					
				try {
            		Thread.sleep(time);
                } catch (InterruptedException e1) {
                	// TODO Auto-generated catch block
                	e1.printStackTrace();
                }	
			}
			
			return;
		}
	};
    
    public void onStart()
    {
    	super.onStart();
/*
		rbtnClock1.setText(prefs.getString("clock1_name", (String) rbtnClock1.getText()));
		rbtnClock2.setText(prefs.getString("clock2_name", (String) rbtnClock2.getText()));
*/		
		radioStations = "";
		running = true;
		thread = new Thread(runnable);
		thread.start();
	}
    
	@Override
    public void onPause() {
    	super.onPause();    	
    	running = false;
		thread.interrupt();
    }
    
	@Override
    public void onRestart() {
    	super.onRestart();   	
    	running = true;
    	thread = new Thread(runnable);
    	thread.start();
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
    	switch(item.getItemId()) {
    	case R.id.mitmPreferences:
    		startActivity(new Intent(this, Preferences.class));
    		return true;
    	}
    	
    	return false;
    }    
  
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	running = false;
		thread.interrupt();
    }
	@Override
	public void onBackPressed()
	{
		if (backPressed < java.lang.System.currentTimeMillis()) {
			Toast.makeText(getApplicationContext(),
					"Press back again to exit", 
	    			Toast.LENGTH_SHORT).show();
			backPressed = java.lang.System.currentTimeMillis() + 5000;
		} else {
			finish();
		}
	}
    
	private String getTargetIp() {
/*
    	if (rbtnClock1.isChecked())
    		hostname = prefs.getString("clock1_address", hostname).toLowerCase(Locale.getDefault());
    	else
    		hostname = prefs.getString("clock2_address", hostname).toLowerCase(Locale.getDefault());
*/
		if (clocks != null)
		{
		   hostname = clocks[0];
		}

	   return dns.getHostAddress(hostname);
    }

	private void updatePlaying() {
		txtPlaying.setText("Updating ...");

    	running = false;
		thread.interrupt();
		radioStations = "";
		running = true;
		thread = new Thread(runnable);
		thread.start();
	}
	
	private void sendCommand(final int port, final String command) {
	    Runnable runnable = new Runnable() {
			@Override
			public void run() {
				tcp.sendMessage(getTargetIp(), port, command);
				return;
			}
	    };
    	Thread thread = new Thread(runnable);
    	thread.start();	    
	}
}
