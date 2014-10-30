// Copyright 2012 Rémi Emonet and Shaun Simpson.
// Thanks to Rémi Emonet for his work.

package uk.co.immutablefix.ClockControl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;

public class DnssdDiscovery extends Object {
	
    android.net.wifi.WifiManager.MulticastLock lock;
    android.os.Handler handler = new android.os.Handler();
    Context context = null;
    List<String> hostnames, ipAdresses;

    private static DnssdDiscovery mInstance = null;
     
    public static DnssdDiscovery getInstance(Context myContext){
    	if(mInstance == null)
    	{
    		mInstance = new DnssdDiscovery(myContext);
    	}
    	return mInstance;
    }   
    
    DnssdDiscovery(Context myContext) {
    	context = myContext;
    	
    	hostnames = new ArrayList<String>();
    	ipAdresses = new ArrayList<String>();
    }
    
    public synchronized String getHostAddress(String host) {
		String address = host;
		// Search cache.
		int i = hostnames.indexOf(host);
		if (i >= 0) {
			return ipAdresses.get(i);
		}
		byte[] header = new byte[] { 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0 };
		byte[] footer = new byte[] { 0x05, 0x6c, 0x6f, 0x63, 0x61, 0x6c, 0, 0, 1, 0, 1 };
		byte[] reply = new byte[1500];
		
		if (!host.contains(".local"))
			host = host + ".local";
		
		int hostLen = host.indexOf(".local");
		// Get multicast lock.
		
		android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context
				.getSystemService(android.content.Context.WIFI_SERVICE);
		lock = wifi.createMulticastLock("clockcontrolmulticastlock");
		lock.setReferenceCounted(true);
		lock.acquire();
		try {
			// Build mDND request packet.
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			buf.write(header);
			buf.write(hostLen);
			for (i = 0; i < hostLen; i++) {
				buf.write(host.charAt(i));
			}
			
			buf.write(footer);
			
			MulticastSocket s = new MulticastSocket();
			DatagramPacket replyPacket = new DatagramPacket(reply, reply.length);
			DatagramPacket sendPacket = new DatagramPacket(buf.toByteArray(),
					buf.size(), InetAddress.getByName("224.0.0.251"), 5353);
			
			s.setTimeToLive(255);
			s.setSoTimeout(1000);
			s.send(sendPacket);
			s.receive(replyPacket);
			
			address = replyPacket.getAddress().getHostAddress();
			
			// Cache host info.
			hostnames.add(host);
			ipAdresses.add(address);
			
			s.close();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		lock.release();
		return address;
    }    
    public synchronized String[] getHostList() {
    	String request = "REQUEST:CLOCKNAME";
    	String address = "";
    	String clockName;
        int requestLen = request.length();
    	
	    byte[] reply = new byte[1500];
	
	    // Get multicast lock.
	    android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context.getSystemService(android.content.Context.WIFI_SERVICE);
	    lock = wifi.createMulticastLock("clockcontrolmulticastlock");
	    lock.setReferenceCounted(true);
	    lock.acquire();
	    
		try {
	        // Build mDND request packet.
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
	        
	        for (int i = 0; i < requestLen; i++)
	        {
	        	buf.write(request.charAt(i));
	        }
	
			MulticastSocket s = new MulticastSocket();
	
			DatagramPacket replyPacket = new DatagramPacket(reply, reply.length);
			DatagramPacket sendPacket = new DatagramPacket(buf.toByteArray(), buf.size(), 
					InetAddress.getByName("255.255.255.255"), 44557);
		
			s.setTimeToLive(255);
			s.setSoTimeout(500);
			
			s.send(sendPacket);
			
			while (true) {
				try {
					s.receive(replyPacket);
				} catch (SocketException e) {
					break;
				} catch (IOException e) {
					break;
				}	
				
				address = replyPacket.getAddress().getHostAddress();
				
				// Add host if not already in the list
				int i = ipAdresses.indexOf(address);
				if (i < 0) {
					clockName = new String(reply, "UTF-8");
					if (clockName.startsWith("CLOCKNAME:")) {
						String[] parts = clockName.split(":");
		
						if (parts.length > 1) {
							// Cache host info.
				        	hostnames.add(parts[1]);
				        	ipAdresses.add(address);						
						}
					}
				}
			}
			
			s.close();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		lock.release();

		ArrayList<String> clocks = new ArrayList<String>(hostnames);
		Collections.sort(clocks);
		
		String[] result = new String[clocks.size()];
		clocks.toArray(result);
		
		return result;
    }
}
