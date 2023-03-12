import java.util.Arrays;
import java.util.Date;

//import udpIP.Client;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.*;

/**
 * Floor Class that consists of the floor thread that executes first to send a request to the scheduler.
 * 
 * @author Yash Kapoor
 * @author Faiaz Ahsan
 * @author Zeid Alwash
 * @author Fareen Lavji
 * @author Harishan Amutheesan
 * 
 * @version 02.27.2023
 */
public class Floor implements Runnable {
	private String floorRequests;
	private FloorData fdPacket;
	private String csvRequests = "";
	private DatagramPacket sendPacket, receivePacket, receiveAckPacket;
	private DatagramSocket socket;
	
	/**
	 * Constructor for Floor that initializes a scheduler and floor data.
	 * 
	 * @param s	A Scheduler Object, the server that is used to communicate between the two clients (i.e., floor and elevator).
	 */
	public Floor(String floorRequests) {
		this.floorRequests = floorRequests;
		
		 try {
	         socket = new DatagramSocket();
	      } catch (SocketException se) {
	         se.printStackTrace();
	         System.exit(1);
	      }
		 
	}
	
	/**
	 * Reads the floorRequests.csv file that contains instructions for the elevator to execute.
	 * Sets the floor data, and notifies the scheduler.
	 */ 
	public void unwrapData() {
		try 
	    {
			// parsing a CSV file into BufferedReader class constructor
			File csvFile = new File(floorRequests);
		    BufferedReader br = new BufferedReader(new FileReader(csvFile));

		    String line = "";
		    while ((line = br.readLine()) != null)   //returns a Boolean value
		    {
		    	String[] elevatorData = new String[4];
			    elevatorData = line.split(",");
			
			    String start_date = elevatorData[0];
			    DateFormat formatter = new SimpleDateFormat("hh:mm:ss");
			    Date date = (Date) formatter.parse(start_date);
			    // parses out the date, so the date is in this format: hh:mm:ss
			   
			    setFloorData(date,
			    		Integer.parseInt(elevatorData[1]),
			    		elevatorData[2],
			    		Integer.parseInt(elevatorData[3]));
			    
			    
		    }
	    
		    br.close();
	    }
	    catch (IOException e) {} 
		catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Send and receive DatagramPackets to/from scheduler 
	 */
	public void sendReceive() {
		byte floorData[] = new byte[1000];
		byte floorReply[] = new byte[100];
		
        // convert string to bytes for packet
        floorData = csvRequests.getBytes();
        
        
	      try {
	      sendPacket = new DatagramPacket(floorData, floorData.length, InetAddress.getLocalHost(), 23);
	      }catch(UnknownHostException e) {
		         e.printStackTrace();
		         System.exit(1);
		      }
	      
	      
	      //send the packet to the scheduler
	      try {
	    	  socket.send(sendPacket);
	      } catch (IOException e) {
	         e.printStackTrace();
	         System.exit(1);
	      }
	      
	      //print the sent packet
	      System.out.println("Floor: Data Request Packet sent to scheduler:");
	      System.out.println("Containing: " + new String(sendPacket.getData(),0,sendPacket.getLength()));
	      System.out.println("Containing in bytes: " + Arrays.toString(sendPacket.getData()).trim());
	      
	      
	      //form packet to receive reply from scheduler
	      receivePacket = new DatagramPacket(floorReply, floorReply.length);
	      
	      //receive the reply packet from the scheduler
	      try {        
	         System.out.println("Waiting..."); // so we know we're waiting
	         socket.receive(receivePacket);
	        
	      } catch (IOException e) {
	         System.out.print("IO Exception: likely:");
	         System.out.println("Receive Socket Timed Out.\n" + e);
	         e.printStackTrace();
	         System.exit(1);
	      }
	      
	      // print the reply packet received from the scheduler
	      System.out.println("\nFloor: Reply Packet received from scheduler:");
	      System.out.println("Containing: " + new String(receivePacket.getData(),0,receivePacket.getLength()));
	      System.out.println("Containing in bytes: " + Arrays.toString(receivePacket.getData()).trim());
	      
	      
	      //form packet to receive Ack packet
			byte floorAck[] = new byte[100];
		    receiveAckPacket = new DatagramPacket(floorAck, floorAck.length);
	      
	      
	      //receive the Ack packet from the scheduler
	      try {        
	         System.out.println("Waiting..."); // so we know we're waiting
	         socket.receive(receiveAckPacket);
	        
	      } catch (IOException e) {
	         System.out.print("IO Exception: likely:");
	         System.out.println("Receive Socket Timed Out.\n" + e);
	         e.printStackTrace();
	         System.exit(1);
	      }
	      
	      // print the reply packet received from the scheduler
	      System.out.println("Floor: Acknowledgement Packet received from scheduler:");
	      System.out.println("Containing: " + new String(receiveAckPacket.getData(),0,receiveAckPacket.getLength()));
	      System.out.println("Containing in bytes: " + Arrays.toString(receiveAckPacket.getData()).trim());
		      		      		      		      		      		      	
		  //close the sockets once done
		  socket.close();
	}

	/**
	 * Used to run the Floor thread.
	 */
	@Override
	public void run() {
        System.out.println("Starting at Floor\n");
        unwrapData();
        sendReceive();
    }
	
	/**
	 * Sets the FloorData Object
	 * @param date		a Date object, the time of the request 
	 * @param iFloor	an int, the initial floor the elevator is at
	 * @param direction		a String, the direction the elevator is going in (up or down)
	 * @param dFloor	an int, the destination floor the elevator needs to go to 
	 * @throws IOException	
	 */
	private void setFloorData(Date date, int iFloor, String direction, int dFloor) throws IOException {
		fdPacket = new FloorData(10);    // setting default floors to 10
		
		
		fdPacket.setTime(date);
		fdPacket.setInitialFloor(iFloor);
		fdPacket.setFloorButton(direction.toLowerCase()); // Up & Down
		fdPacket.setDestinationFloor(dFloor);
		
		fdPacket.toString();
		
		
		// wrapping floorData to String
		csvRequests = csvRequests.concat(fdPacket.getTime() + "," + fdPacket.getInitialFloor() + "," + fdPacket.getFloorButton() + "," + fdPacket.getDestinationFloor() + "/");
     
		System.out.println(csvRequests);
		
	    System.out.println("Scheduler: A request has been added to the queue");
	}
	
	public static void main(String args[])
	   {
	    Floor f = new Floor("./floorRequests.csv");
	    Thread t1 = new Thread(f);
	    
	    t1.start();
	   }
	
}