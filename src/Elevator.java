import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;

/**
 * Elevator Class that consists of the elevator thread that will execute after the scheduler sends the request.
 * 
 * @author Yash Kapoor
 * @author Faiaz Ahsan
 * @author Zeid Alwash
 * @author Fareen Lavji
 * @author Harishan Amutheesan
 * 
 * @version 02.27.2023
 */
public class Elevator implements Runnable {
	
	
	//private int currentFloor;	
	// the floor the elevator needs to go to, so it can take the request
	// the initial floor of the request

	
	private int direction;
	private static long travelTime = 1000; // average time elevator takes to move 1 floor
	Collection<Integer> requestsQueue;
	private Integer currentFloor;

	
	DatagramPacket sendPacket, receivePacket;
	DatagramSocket socketScheduler, socketFloor;
	
	
	
	/**
	 * Constructor for Elevator.
	 * 
	 * @param s	A Scheduler object that is used to communicate between the two clients (i.e., floor and elevator).
	 */
	public Elevator(int portNumber) {
		this.currentFloor = 2; // assume elevator starts at floor 2 
		this.requestsQueue = Collections.synchronizedCollection(new LinkedList<>());
		this.direction = 2; 
		
		
		  try {
		         socketScheduler = new DatagramSocket(portNumber);
		      } catch (SocketException se) {
		         se.printStackTrace();
		         System.exit(1);
		      }						
	}
	
	/**
	 * Returns the current floor of the elevator.
	 * 
	 * @return current floor of the elevator
	 */
	public int getCurrentFloor() {
		return this.currentFloor;
	}
	/**
	 * Returns the direction of the elevator.
	 * 
	 * @return Up if moving up, down if moving down, stationary if not moving.
	 */
	public int getDirection() { return this.direction; }
	
	/**
	 * Returns the current floor of the elevator.
	 * 
	 * @return current floor of the elevator
	 */
	public void setCurrentFloor(int currentFloor) {
		this.currentFloor = currentFloor;
	}
	/**
	 * Returns the direction of the elevator.
	 * 
	 * @return Up if moving up, down if moving down, stationary if not moving.
	 */
	public void setDirection(int direction) { 
		this.direction = direction; 
	}
	
	/**
	 * Returns the size of the elevators queue.
	 * 
	 * @return An integer value with the elevator's queue size.
	 */
	public int getQueueSize() { return requestsQueue.size(); }
	
	/**
	 * Adds request to queue based on most efficient position.
	 * 
	 * @param destinationFloor The destination floor of the given request.
	 */
	public void addRequest(int destinationFloor, int position) {
		requestsQueue.add(destinationFloor);// need to implement the position as well

	}
	

	// Elevator is going to receive a list of destination floors and it is going to process each request
	// As it approaches each floor, it is going to ask the scheduler whether it should stop at that floor or not (send UDP Packet)
		// If packet is empty, then we don't stop since if scheduler sends something, then the elevator must stop at that floor
		// Otherwise, if the elevator receives an empty packet from the scheduler, then it does not need to stop.
	// If the new request is made after 8 seconds, then the new request is not serviceable
	// One queue in the elevator which has a list of destination floors
	// Another queue in the scheduler that has a list of all requests (time, initial floor, direction, and destination floor)
	
	// Have a method that constantly asks the scheduler after every 8 seconds (the time that it takes to move between floors)
	// After every 8 seconds, the method gets executed - method returns true if we need to stop at the floor, false otherwise
	// Might be able to call this method in the executeRequest() method
	
	
	
	/**
	 * Executes a request based on the destination floor. Switches the state to active and calculates how long the
	 * elevator would take to reach the destination floor. If there are no more requests in the queue, state stays
	 * active.
	 * 
	 * @param destinationFloor The destination floor that the elevator needs to go to.
	 */
	private void executeRequest(Integer destinationFloor) {
		
		// switch the state of the elevator
		int comparisor = destinationFloor.compareTo(this.currentFloor);
		
		if (comparisor < 0) { this.direction = 0; }
		else if (comparisor > 0) {this.direction = 1; }
		else { this.direction = 2; }
		
		// processing request
		try {
			Thread.sleep(Elevator.travelTime * (destinationFloor - this.currentFloor));
		}
		catch (InterruptedException e) {}
		
		// switch elevator back to stationary if there are no more requests
		if (!this.requestsQueue.isEmpty()) { this.direction = 2; }
		
		this.currentFloor = destinationFloor; // update current floor
		
		//0 for up, 1 or down and 2 for stationnary
		
	}
	
	/**
	 * Send and receive DatagramPackets to/from scheduler 
	 */
	public void sendReceive() {
		while(true) {
			byte schedulerData[] = new byte[1000];
			byte ackData[] = new byte[100];
			String ack = "Data received";

			
			//form packet to receive data from scheduler
			receivePacket = new DatagramPacket(schedulerData, schedulerData.length);
			
			
			//receive Packet from scheduler
			try {        
				System.out.println("Waiting...");
		         socketScheduler.receive(receivePacket);
		        
		      } catch (IOException e) {
		         System.out.print("IO Exception: likely:");
		         System.out.println("Receive Socket Timed Out.\n" + e);
		         e.printStackTrace();
		         System.exit(1);
		      }
			
			
			//print the received datagram from the scheduler
		      System.out.println("Elevator: Packet received from Floor:");
		      System.out.println("From host: " + receivePacket.getAddress());
		      System.out.println("Host port: " + receivePacket.getPort());
		      System.out.println("Length: " + receivePacket.getLength());
		      System.out.println("Containing: " + new String(receivePacket.getData(),0,receivePacket.getLength()));
		      System.out.println("Containing in bytes: " + Arrays.toString(receivePacket.getData()));
			
			//process the request, call the internal method
		     String data = new String(receivePacket.getData(),0,receivePacket.getLength());
		     String[] arrValues = data.split("/");
		     for(int i = 0; i < arrValues.length; i++) {
		    	 String[] arrValues2 = arrValues[i].split(",");
//		    	 executeRequest(Integer.parseInt(arrValues2[3]));
		    	 System.out.println(Thread.currentThread().getName() + " " + arrValues[i].toString());
		     }
			
			//Print request has been serviced
			System.out.println("This Request has been fulfilled!");
			
			

		      		      		      
		      //form ackPacket to send to scheduler
		      ackData = ack.getBytes();
		      try {
		      sendPacket = new DatagramPacket(ackData, ackData.length, InetAddress.getLocalHost(), receivePacket.getPort());
		      }catch(UnknownHostException e) {
			         e.printStackTrace();
			         System.exit(1);
			      }
		      
		      //send the Ack Packet to the scheduler
		      try {
		    	  socketScheduler.send(sendPacket);
		      } catch (IOException e) {
		         e.printStackTrace();
		         System.exit(1);
		      }
		      
			  //print the sent to the scheduler
		      System.out.println("Elevator: Packet received from Floor:");
		      System.out.println("From host: " + sendPacket.getAddress());
		      System.out.println("Host port: " + sendPacket.getPort());
		      System.out.println("Length: " + sendPacket.getLength());
		      System.out.println("Containing: " + new String(sendPacket.getData(),0,sendPacket.getLength()));
		      System.out.println("Containing in bytes: " + Arrays.toString(sendPacket.getData()));
		      
		      			
		}		
	}
	
	/**
	 * Used to run the Elevator threads.
	 */
	@Override
    public void run() { 
		sendReceive();
	}
	
	public static void main(String args[])
	   {
			Elevator e = new Elevator(69);		
		    Thread t1 = new Thread(e, "Elevator One");	    	    
		    t1.start();
		    Elevator e2 = new Elevator(70);
		    Thread t2 = new Thread(e2, "Elevator Two");
		    t2.start();
	    
	   }
}
        		

         	
            
    
	

