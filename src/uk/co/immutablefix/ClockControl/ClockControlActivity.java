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
	
	String hostname = "";
	String weather = "";
	Boolean running = true;
	long backPressed = 0;
	int radioChannel = 0;

	DnssdDiscovery dns;
	TCPClient tcp;
	
	Button btnVolDown, btnVolUp;
	RadioButton rbtnClock1, rbtnClock2;
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
        tcp.setTimeout(2000);
        
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
				/*
				radioChannel += 1;
				if (radioChannel < 1) radioChannel = 1;   
				if (radioChannel > 9) radioChannel = 1;
				sendCommand(44558, "CLOCK:RADIO:" + String.valueOf(radioChannel));
				*/
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

		//  Log.d("Events", "Starting ... Fin");
     }

	private void launchStationPicker() {
		Intent intent = new Intent(this, StationPickerActivity.class);
		//intent.putExtra(StationPickerActivity.KEY_MULTI_SELECT, multiSelect);
		startActivityForResult(intent, GET_RADIO_STATION);
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
	        		sendCommand(44558, "CLOCK:RADIO:" + String.valueOf(radioChannel));
	        	}
	        default:
	            break;
	    }
	}		
	
    final Handler handler = new Handler();
    Runnable runnable = new Runnable() {
	    String reply;
	    
		@Override
		public void run() {
			int time = 1000;
			
			while (running) {
				//Log.d("TREAD", "Tick ...");

				//  Log.d("TREAD", "Getting data ...");
				reply = tcp.getMessage(getBaseContext(), getTargetIp(), 44558, "CLOCK:PLAYING");

				handler.post(new Runnable() {
					@Override
					public void run() {
						if ((reply != null) && (reply.length() > 0) && (!reply.equals(txtPlaying.getText()))) 
							txtPlaying.setText(reply);
					}
				});
				
				try {
            		Thread.sleep(time);
                } catch (InterruptedException e1) {
                	// TODO Auto-generated catch block
                	e1.printStackTrace();
                }	
			}
			
			//Log.d("THREAD", "Ended.");		
			return;
		}
	};
    
    public void onStart()
    {
    	super.onStart();

		rbtnClock1.setText(prefs.getString("clock1_name", (String) rbtnClock1.getText()));
		rbtnClock2.setText(prefs.getString("clock2_name", (String) rbtnClock2.getText()));

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
    	if (rbtnClock1.isChecked())
    		hostname = prefs.getString("clock1_address", hostname).toLowerCase(Locale.getDefault());
    	else
    		hostname = prefs.getString("clock2_address", hostname).toLowerCase(Locale.getDefault());
        return dns.getHostAddress(hostname);
    }

	private void updatePlaying() {
		txtPlaying.setText("Updating ...");

    	running = false;
		thread.interrupt();
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
