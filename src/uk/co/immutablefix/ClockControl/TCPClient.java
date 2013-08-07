//
// Copyright 2012 Shaun Simpson
// shauns2029@gmail.com
//

package uk.co.immutablefix.ClockControl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.content.Context;
import android.util.Log;

public class TCPClient extends Object{
    Socket s = null;
    BufferedWriter out = null;
    int timeout = 15000;
    
	
	public void setTimeout(int milli) {
		timeout = milli;
	}
	
	public synchronized void sendMessage(String address, int port, String message) {
		String response = "";
		InetAddress serverIP = null;
		
		try {
			serverIP = InetAddress.getByName(address);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if (serverIP != null){
		   	try {
		   		Socket s = new Socket(serverIP.getHostAddress(), port);
		   		s.setSoTimeout(timeout);
		   		
				//outgoing stream redirect to socket
			    OutputStream out = s.getOutputStream();
	
			    PrintWriter output = new PrintWriter(out);
			    BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
			    output.println(message);
			    output.flush();
			    
			    //read line(s)
			    while ((response != null) && (!response.equals(":OK")))
			    {
			    	response = input.readLine();
			    }

			    output.close();
			    s.close();
			} catch (UnknownHostException e) {
			    // TODO Auto-generated catch block
				Log.d("COMMS", "Failed to resolve address. Error: " + e.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public synchronized String getMessage(Context context, String address, int port, String message) {
		String replyStr = "";
		String response = "";
		InetAddress serverIP = null;

		try {
			serverIP = InetAddress.getByName(address);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if (serverIP != null){
		   	try {
		   		Socket s = new Socket(serverIP.getHostAddress(), port);
		   		s.setSoTimeout(timeout);

				//outgoing stream redirect to socket
			    OutputStream out = s.getOutputStream();
	
			    PrintWriter output = new PrintWriter(out);
			    BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
			    output.println(message);
			    output.flush();
			    
			    //read line(s)
			    while ((response != null) && (!response.equals(":OK")))
			    {
			    	response = input.readLine();
			    	if (!response.equals(":OK")) 
			    	{
			    		// Add line ending if needed
			    		if (replyStr.length() > 0)
			    		{
			    			replyStr = replyStr.concat(System.getProperty("line.separator"));
			    		}

		    			replyStr = replyStr.concat(response);
			    	}
			    	
			    }

			    output.close();
			    s.close();
			} catch (UnknownHostException e) {
			    // TODO Auto-generated catch block
				Log.d("COMMS", "Failed to resolve address. Error: " + e.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return replyStr;
	}
}