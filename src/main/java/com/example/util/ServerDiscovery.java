package com.example.util;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ServerDiscovery {
    private static final int DISCOVERY_PORT = 8889;
    private static final int TIMEOUT_MS = 3000;
    
    /**
     * Discovers servers in the local network
     * @return List of server addresses in format "ip:port"
     */
    public static List<String> discoverServers() throws IOException {
        List<String> servers = new ArrayList<>();
        
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.setSoTimeout(TIMEOUT_MS);
            
            // Send discovery request
            byte[] sendData = "DISCOVER_SERVER".getBytes();
            
            // Broadcast to local network
            DatagramPacket sendPacket = new DatagramPacket(
                sendData, 
                sendData.length, 
                InetAddress.getByName("255.255.255.255"), 
                DISCOVERY_PORT
            );
            
            socket.send(sendPacket);
            System.out.println("Discovery request sent");
            
            // Wait for responses
            long endTime = System.currentTimeMillis() + TIMEOUT_MS;
            byte[] receiveData = new byte[256];
            
            while (System.currentTimeMillis() < endTime) {
                try {
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    socket.receive(receivePacket);
                    
                    String serverInfo = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    servers.add(serverInfo);
                    
                    System.out.println("Found server: " + serverInfo);
                } catch (SocketTimeoutException e) {
                    // Timeout reached, stop waiting
                    break;
                }
            }
        }
        
        return servers;
    }
    
    /**
     * Simple test method
     */
    public static void main(String[] args) {
        try {
            List<String> servers = discoverServers();
            if (servers.isEmpty()) {
                System.out.println("No servers found");
            } else {
                System.out.println("Found " + servers.size() + " servers:");
                for (String server : servers) {
                    System.out.println("  " + server);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}