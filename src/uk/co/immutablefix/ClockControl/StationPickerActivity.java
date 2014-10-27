// Copyright 2012 Shaun Simpson shauns2029@gmail.com

package uk.co.immutablefix.ClockControl;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class StationPickerActivity extends ListActivity  {
	public static final String KEY_RADIO_STATIONS = "radio_stations";
	
	String radioStations = "";
			
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stations);
        
        radioStations = (String) getIntent().getExtras().getString(KEY_RADIO_STATIONS);
        
        //listView = (ListView) findViewById(R.id.list);
        String[] values = radioStations.split(";"); 
        		
        // Define a new Adapter
        // First parameter - Context
        // Second parameter - Layout for the row
        // Third - the Array of data

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, values);


        // Assign adapter to List
        setListAdapter(adapter); 
   }

    
  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
        
        super.onListItemClick(l, v, position, id);

           // ListView Clicked item index
           int station     = position;
           Intent result = new Intent();
           result.putExtra("station", station);
           setResult(RESULT_OK, result);
           finish();
  }
}
