//
// Copyright 2012 Shaun Simpson
// shauns2029@gmail.com
//

package uk.co.immutablefix.ClockControl;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.util.Log;

public class UDP extends Object {
	DatagramSocket socket = null;

	public void open() throws java.io.IOException {
    	socket = new DatagramSocket();
		socket.setSoTimeout(500);
	}

	public void close() {
		if (socket != null) socket.close();
	}
	
	public synchronized void sendUDPMessage(String address, int port, String message) throws java.io.IOException {
	     if (socket == null) open();
	     InetAddress serverIP = InetAddress.getByName(address);
	     
	     if (serverIP != null){
	    	 byte[] outData = (message).getBytes();
	    	 DatagramPacket out = new DatagramPacket(outData, outData.length, serverIP, port);
	    	 socket.send(out);
	     }
	}
	
	public synchronized String getUDPMessage(Context context, String address, int port, String message) throws java.io.IOException {
		int i;
		
		if (socket == null) open();
		try {
			InetAddress serverIP = InetAddress.getByName(address);
		
			byte[] outData = (message).getBytes();
			DatagramPacket out = new DatagramPacket(outData, outData.length, serverIP, port);
		
			byte[] reply = new byte[1500];
			DatagramPacket in = new DatagramPacket(reply, reply.length);
/*		             
			for (i = 0; i < 3; i++){
				try {
					socket.send(out);
			     	socket.receive(in);
			     	break;
			     } catch (IOException e) {
			     	//  Log.d("COMMS", "Failed to get reply. Error: " + e.getMessage());
			     }
			}
			
		    if (i < 3) {
		    	return new String(reply, 0, in.getLength());
		    } else { 
		    	return "";
		    }
*/
			try {
				socket.send(out);
		     	socket.receive(in);
		    } catch (IOException e) {
		     	//  Log.d("COMMS", "Failed to get reply. Error: " + e.getMessage());
		    	return "";
		    }
			
	    	return new String(reply, 0, in.getLength());
		} catch (IOException e) {
			//Log.d("COMMS", "Failed to resolve address. Error: " + e.getMessage());
		}
		return "";
	}
}
