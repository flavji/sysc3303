import java.util.ArrayList;
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
	private DatagramSocket replySocket, acknowledgementSocket;
	private ArrayList<Date> times;
	private int pendingRequests;
	private long startTime;
	private long endTime;
	private long elapsedTime;
	
	// One request at a time from the floor goes to the scheduler -> elevator > scheduler then back to floor
	// Use arrival times to send the requests
	// Scheduler assigns it to a specific elevator 
	// Elevator going up, travels up until it has serviced all the requests and same for elevator going up
	// When scheduler receives the request, its gonna add it to the queue we have
	// Use the queue as people that are waiting for the elevator, and each elevator has a queue that has all the people on it 
	// Elevator needs a list of destination floors 
	// scheduler needs a list of initial and destination floor - scheduler keeps track of all ppl waiting to enter elevator
	// elevator keeps track of ppl on it 
	// You can have any number of elevators as a result 
	// Elevator has one queue - it can store the number of people in the elevator and the destination floor the elevator is supposed to go to
	
	// We're still confused about how to implement the timer - should it be like as soon as the program starts, the timer begins?
	// Wall clock - 10 real seconds (import statement) go by, simulator time - doesnt actually wait the 10 seconds 
	// Floor controls simulation time 
	// Will each request count as one person in the elevator? 
	
	// If the requests happen simultaneously, does the floor send both the requests
	// and then the scheduler determines if they are serviceable?
	
	/**
	 * Constructor for Floor that initializes a scheduler and floor data.
	 * 
	 * @param s	A Scheduler Object, the server that is used to communicate between the two clients (i.e., floor and elevator).
	 */
	public Floor(String floorRequests) {
		this.floorRequests = floorRequests;
		this.times = new ArrayList<Date>();
		this.pendingRequests = 0;
		 
	}
	
	/**
	 * Add times to the times ArrayList to check how long we are going to be 
	 * sleeping for in between requests 
	 */
	public void addTimes() {
		try {
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
			    times.add(date);
		    }
		    br.close();
		} catch(Exception e) {
			e.printStackTrace();
		}

	}
	
	// Check which request is the first one based on the wall clock time
	// the request that has the smallest time in the CSV file is going to run first
	// implement selectRequest() method (don't need to create a new method - we can modify unwrapData() method)
	
	// We start the wall clock time when we send the first request
	
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
		    int lineNumber = 1;
		    // Record start time
		    startTime = System.currentTimeMillis();
		    System.out.println("System Start Time (current time in milliseconds): " + startTime + " ms");
		    while ((line = br.readLine()) != null)   //returns a Boolean value
		    {
		    	pendingRequests++;
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
			    
	            // spawn a new thread for each floor request
	            Thread thread = new Thread(() -> {
	                sendReceive();
	            });
	            thread.start();
			    long time = 0;
	            if (lineNumber < times.size()) {
	                time = times.get(lineNumber).getTime() - times.get(lineNumber - 1).getTime();
	            }
			    try {
			    	if(time == 0) {
			    		Thread.sleep(1000);
			    	} else {
						Thread.sleep(time);
			    	}
					lineNumber++;
					continue;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			   
			    
		    }
	    
		    br.close();
	    }
	    catch (IOException e) {} 
		catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	   // method to listen for packets from the Scheduler
	   public void listenForSchedulerPackets(int port) {
		   
		   Runnable floorReceiver = () -> {
		      // create a DatagramSocket and DatagramPacket to receive packets from the Scheduler
		     DatagramSocket schedulerSocket = null;
			try {
				schedulerSocket = new DatagramSocket(port);
			      byte[] receiveData = new byte[1024];
			      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		
			      // loop to constantly listen for packets from the Scheduler
			      while (true) {
			         try {
						schedulerSocket.receive(receivePacket);
					    System.out.println("\nFloor: Acknowledgement Packet received from scheduler:");
				        System.out.println("Containing: " + new String(receivePacket.getData(),0,receivePacket.getLength()));
				        pendingRequests--;
				        
				        if(pendingRequests == 0) {
				        	endTime = System.currentTimeMillis();
				        	System.out.println("\nSystem End Time (current time in milliseconds): " + endTime + " ms");
				        	elapsedTime = endTime - startTime;
				        	System.out.println("\nThe system took " + elapsedTime + " ms to run the entire input file.");
				        	System.exit(1);
				        }
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			      }
			      
			} catch (SocketException e) {
				e.printStackTrace();
			} 
		   };
	        new Thread(floorReceiver).start();
	   }
		   
		   
	   
	/**
	 * Send and receive DatagramPackets to/from scheduler 
	 */
	public void sendReceive() {
	    byte floorData[] = new byte[1000];
	    byte floorReply[] = new byte[1000];
	    
	    try (DatagramSocket replySocket = new DatagramSocket()) {
	        // convert string to bytes for packet
	        floorData = csvRequests.getBytes();
	        
	        
	        try {
	            sendPacket = new DatagramPacket(floorData, floorData.length, InetAddress.getLocalHost(), 23);
	        } catch (UnknownHostException e) {
	            e.printStackTrace();
	            System.exit(1);
	        }
	        
	        
	        //send the packet to the scheduler
	        try {
	            replySocket.send(sendPacket);
	        } catch (IOException e) {
	            e.printStackTrace();
	            System.exit(1);
	        }
	        
	        //print the sent packet
	        System.out.println("\nFloor: Request sent to scheduler");
	        System.out.println("Containing: " + new String(sendPacket.getData(),0,sendPacket.getLength()));
	        
	        
	        //form packet to receive reply from scheduler
	        receivePacket = new DatagramPacket(floorReply, floorReply.length);
	        
	        //receive the reply packet from the scheduler
	        try {        
	            System.out.println("Floor: Waiting for reply from Scheduler..."); // so we know we're waiting
	            replySocket.receive(receivePacket);
	            
	        } catch (IOException e) {
	            System.out.print("IO Exception: likely:");
	            System.out.println("Receive Socket Timed Out.\n" + e);
	            e.printStackTrace();
	            System.exit(1);
	        }
	        
	        // print the reply packet received from the scheduler
	        System.out.println("Floor: Reply Packet received from scheduler:");
	        System.out.println("Containing: " + new String(receivePacket.getData(),0,receivePacket.getLength()));
	        
	        replySocket.close();
	    } catch (IOException e) {
	        e.printStackTrace();
	        System.exit(1);
	    }
	
}

	/**
	 * Used to run the Floor thread.
	 */
	@Override
	public void run() {
        System.out.println("Starting at Floor\n");
        addTimes();
        unwrapData();
//        sendReceive();
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
		fdPacket = new FloorData(22);    // setting default floors to 22
		
		
		fdPacket.setTime(date);
		fdPacket.setInitialFloor(iFloor);
		fdPacket.setFloorButton(direction.toLowerCase()); // Up & Down
		fdPacket.setDestinationFloor(dFloor);
		
		fdPacket.toString();
		
		// wrapping floorData to String
		csvRequests = fdPacket.getTime() + "," + fdPacket.getInitialFloor() + "," + fdPacket.getFloorButton() + "," + fdPacket.getDestinationFloor();
//		csvRequests = csvRequests.concat(fdPacket.getTime() + "," + fdPacket.getInitialFloor() + "," + fdPacket.getFloorButton() + "," + fdPacket.getDestinationFloor() + "/");
	}
	
	public static void main(String args[])
	   {
	    Floor f = new Floor("./floorRequests.csv");
	    Thread t1 = new Thread(f);
	    
	    t1.start();
	    f.listenForSchedulerPackets(1500);
	   }
	
}