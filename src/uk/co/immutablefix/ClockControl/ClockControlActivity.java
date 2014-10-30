// Copyright 2012 Shaun Simpson shauns2029@gmail.com

package uk.co.immutablefix.ClockControl;

import uk.co.immutablefix.ClockControl.StationPickerActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ClockControlActivity extends Activity {
	private static final int GET_RADIO_STATION = 3007;
	
	String[] clocks = null;
	ArrayAdapter<String> clocksAdapter = null;

	String hostAddress = "";
	String weather = "";
	String radioStations = "";
	Boolean running = true;
	long backPressed = 0;
	int radioChannel = 0;

	DnssdDiscovery dns;
	TCPClient tcp;
	
	Button btnVolDown, btnVolUp;
	ListView lstClocks;
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
        
    	txtPlaying = (TextView) findViewById(R.id.txt_playing);	   
    	txtPlaying.setText("Seclect a clock form the selection below");
    	
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
			}
		});	
	
	    Button btnMeditation = (Button) findViewById(R.id.btn_meditation);
	    btnMeditation.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendCommand(44558, "CLOCK:MEDITATION");
			}
		});	

	    Button btnRadio = (Button) findViewById(R.id.btn_radio);
	    btnRadio.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				launchStationPicker();			
			}
		});	

	    Button btnPause = (Button) findViewById(R.id.btn_pause);
	    btnPause.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendCommand(44558, "CLOCK:PAUSE");
			}
		});	

	    lstClocks = (ListView) findViewById(R.id.lstClocks);

	    lstClocks.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				updatePlaying(position);
			}
		});
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
	    String reply, stationReply;
	    
		@Override
		public void run() {
			int time = 500;
			
			while (running) {
				if (hostAddress != "") {
					time = 500;
					
					if (radioStations == "") {
						stationReply = tcp.getMessage(getBaseContext(), hostAddress, 44558, "CLOCK:GET:RADIOSTATIONS");
		
						stationHandler.post(new Runnable() {
							@Override
							public void run() {
								if ((stationReply != null) && (stationReply.length() > 0)) 
									radioStations = stationReply;
							}
						});
					}
				
					reply = tcp.getMessage(getBaseContext(), hostAddress, 44558, "CLOCK:PLAYING");
	
					handler.post(new Runnable() {
						@Override
						public void run() {
							if ((reply != null) && (reply.length() > 0) && (!reply.equals(txtPlaying.getText()))) 
								txtPlaying.setText(reply);
						}
					});			
				} else {
					time = 1000;
					
					String[] newClocks = dns.getHostList();
					if (clocks != newClocks) {
						clocks = newClocks;
						clocksHandler.post(new Runnable() {
							@Override
							public void run() {
								if ((clocks != null) && (clocks.length > 0)) 
									updateClocksList();
							}
						});			
					}
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
/*    
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
*/  
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
    
	private void updateClocksList() {
		if (clocks != null) {
			clocksAdapter = new ArrayAdapter<String>(this,
			        android.R.layout.simple_list_item_1, clocks);
			lstClocks.setAdapter(clocksAdapter);
		}
	}
	
	private void updatePlaying(int index) {
		hostAddress = dns.getHostAddress(lstClocks.getItemAtPosition(index).toString());
		
    	running = false;
		thread.interrupt();
		radioStations = "";
		running = true;
		thread = new Thread(runnable);
		thread.start();

		txtPlaying.setText("");
	}
	
	private void sendCommand(final int port, final String command) {
	    Runnable runnable = new Runnable() {
			@Override
			public void run() {
				tcp.sendMessage(hostAddress, port, command);
				return;
			}
	    };
    	Thread thread = new Thread(runnable);
    	thread.start();	    
	}
}
