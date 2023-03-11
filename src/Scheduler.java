import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

//import udpIP.Client;

/**
 * Scheduler Class that consists of a thread that is used as a communication channel between the clients (i.e., floor and elevator).
 *
 * @author Yash Kapoor
 * @author Faiaz Ahsan
 * @author Zeid Alwash
 * @author Fareen Lavji
 * @author Harishan Amutheesan
 * 
 * @version 02.27.2023
 */
public class Scheduler implements Runnable {
	
	private ArrayList<Elevator> elevators; // collection of elevators
	private int schedulerToElevatorCondition; // equals to 1 if elevator class can work
	private int schedulerToFloorCondition; // equals to 1 if floor class can work
	
	private int idle;

	private Queue<FloorData> allFloorRequests;   // a queue of all requests in the CSV file
	private Queue<FloorData> serviceableFloorRequests;    // a queue of serviceable requests at the moment
	private boolean floorRequestReceived;
	private int[] portNumbers = new int[5];
	private String elevatorAck = "";
	
	DatagramPacket receivePacketFloor, receivePacketElevator, sendPacketFloor, sendPacketElevator, sendAckPacketFloor;
	DatagramSocket sendAndReceiveSocket, receiveSocket, floorSocket, elevatorSocket;
	
	// Assume all the requests in the CSV file come in simultaneously or around roughly the same time.
	// Based off that, we check whether the request is serviceable.
	// If it is, then add it to the serviceableFloorRequests queue, service it,
	// and remove it from both the queues. 
	// If it is not serviceable at the moment, skip over it 
	// and do NOT add it to the serviceableFloorRequests queue.
	// Come back to the non-serviceable requests after the serviceable requests have been serviced
	// and check whether they are serviceable again. 

	/**
	 * Constructor for Scheduler.
	 */
	public Scheduler() {
		
		//initialization
		this.schedulerToElevatorCondition = 0;
		this.schedulerToFloorCondition = 0;
		this.elevators = new ArrayList<Elevator>();
		this.idle = 1;
		this.allFloorRequests = new LinkedList<FloorData>();
		this.serviceableFloorRequests = new LinkedList<FloorData>();
		this.floorRequestReceived = false;
		
		//initialize ports, assumption: 2 elevators by default
		portNumbers[0] = 50;
		portNumbers[1] = 29;

		elevators.add(new Elevator( portNumbers[0])); //adding one default elevator to elevator list
		elevators.add(new Elevator( portNumbers[1]));
		
		

		
		  try {

		         floorSocket = new DatagramSocket(23);
		         elevatorSocket = new DatagramSocket();

		      } catch (SocketException se) {
		         se.printStackTrace();
		         System.exit(1);
		      }
	}

	/**
	 * Get the schedulerToElevatorCondition.
	 * 
	 * @return	An integer value (either 0 or 1) that is used to communicate from the scheduler to the elevator thread.
	 */
	public int getSchedulerToElevatorCondition() {
		return schedulerToElevatorCondition;
	}
	
	/**
	 * Get the schedulerToFloorCondition.
	 * 
	 * @return	An integer value (either 0 or 1) that is used to communicate from the scheduler back to the floor.
	 */
	public int getSchedulerToFloorCondition() {
		return schedulerToFloorCondition;
	}
	
	/**
	 * Sets the schedulerToElevatorCondition to 0 to prevent the elevator from executing when
	 * we are going back to the scheduler, then eventually, we are going from the scheduler back to the floor thread.
	 */
	public void setSchedulerToElevatorConditionToFalse() {
		schedulerToElevatorCondition = 0;
	}
	
	/**
	 * Sets the schedulerToElevatorCondition to 1 to communicate from the scheduler to the elevator thread.
	 * Sets the floor condition to 0 so the floor thread doesn't execute while the elevator is executing.
	 */
	public void notifySchedulerToElevator() {
		schedulerToElevatorCondition = 1;
	}

	/**
	 * Sets the schedulerToFloorCondition to 1.
	 */
	public void notifySchedulerToFloor() {
 		schedulerToFloorCondition = 1;
	}
	
	/**
	 * Add requests to the allFloorRequests queue
	 * @param fd	a FloorData Object that gets added to the queue
	 */
	public void addRequests(FloorData fd) {
		
		allFloorRequests.add(fd);
	}
	
	/**
	 * Add requests to the servicableFloorRequests queue 
	 * @param fd	a FloorData Object that gets added to the queue
	 */
	public void addServiceableRequests(FloorData fd) {
		serviceableFloorRequests.add(fd);
	}
	
	/**
	 * remove the first FloorData Object from the servicableFloorRequests queue
	 */
	public void removeServiceableRequests() {
		serviceableFloorRequests.remove();
	}
	
	/**
	 * remove the first FloorData Object from the allFloorRequests queue
	 */
	public void removeRequests() {
		allFloorRequests.remove();
	}
	
	/**
	 * Get the allFloorRequests queue
	 * @return	a Queue, the allFloorRequests queue
	 */
	public Queue<FloorData> getAllRequests() {
		return allFloorRequests;
	}
	
	/**
	 * Get the serviceableFloorRequests queue 
	 * @return	a Queue, the serviceableFloorRequests queue
	 */
	public Queue<FloorData> getServiceableRequests() {
		return serviceableFloorRequests;
	}
	
	/**
	 * Sets the schedulerToFloorCondition to 0 to prevent the floor from executing when
	 * we are going back to the scheduler
	 */
	public void setSchedulerToFloorConditionToFalse() {
		schedulerToFloorCondition = 0;	
	}
	
	private FloorData parsePacket(String arr) {
		
		String data = arr;
		System.out.println(data);
		
		String[] arrValues = data.split(",");
		FloorData fd = new FloorData(10);
		
		String dateTimeString = arrValues[0];
		String timeString = dateTimeString.substring(11, 19);

		

	

		try {
			String start_date = timeString;
			DateFormat formatter = new SimpleDateFormat("hh:mm:ss");
			Date date;
			 
			date = (Date) formatter.parse(start_date);
			 fd.setTime(date);
			 fd.setInitialFloor(Integer.parseInt(arrValues[1]));
			 fd.setFloorButton(arrValues[2]);
			 fd.setDestinationFloor(Integer.parseInt(arrValues[3]));
		} catch (Exception e) {
			e.printStackTrace();
		}
		 	 
		return fd;
	}
	
	 public synchronized void sendReceiveFloor() {
		   while(true) {
			   
			   byte floorData[] = new byte[100];
			   byte floorReply[] = new byte[100];
			   String reply = "Scheduler has received the request";
			   
			   //form packets to receive from elevator
			   receivePacketFloor = new DatagramPacket(floorData, floorData.length);
			   
			   System.out.println(" Scheduler: Waiting for Packet.\n");
			   			   
				// Block until a datagram packet is received from elevator
			      try {        
			         System.out.println(" Floor method Waiting..."); // so we know we're waiting
			         floorSocket.receive(receivePacketFloor);
			        
			      } catch (IOException e) {
			         System.out.print("IO Exception: likely:");
			         System.out.println("Receive Socket Timed Out.\n" + e);
			         e.printStackTrace();
			         System.exit(1);
			      }
			      
			      //set boolean to true once floor request is received from floor
			      floorRequestReceived = true;
			      
			      FloorData fdx = parsePacket(new String(receivePacketFloor.getData(),0,receivePacketFloor.getLength()));
			      
			      addRequests(fdx);
			      
			      
			      
			     //print the received datagram
			      System.out.println("Scheduler: Packet received from Floor:");
			      System.out.println("From host: " + receivePacketFloor.getAddress());
			      System.out.println("Host port: " + receivePacketFloor.getPort());
			      System.out.println("Length: " + receivePacketFloor.getLength());
			      System.out.println("Containing: " + new String(receivePacketFloor.getData(),0,receivePacketFloor.getLength()));
			      System.out.println("Containing in bytes: " + Arrays.toString(receivePacketFloor.getData()));
			      
			      
			     //form packet to reply to floor
			      floorReply = reply.getBytes();
			      try {
			      sendPacketFloor = new DatagramPacket(floorReply, floorReply.length, InetAddress.getLocalHost(), receivePacketFloor.getPort());
			      }catch(UnknownHostException e) {
				         e.printStackTrace();
				         System.exit(1);
				      }
			      
			      
			      //send the reply packet to floor
			      try {
			    	  floorSocket.send(sendPacketFloor);
			      } catch (IOException e) {
			         e.printStackTrace();
			         System.exit(1);
			      }
			      
				  //print the reply sent back to floor
			      System.out.println("Scheduler: Reply Packet sent to Floor:");
			      System.out.println("From host: " + sendPacketFloor.getAddress());
			      System.out.println("Host port: " + sendPacketFloor.getPort());
			      System.out.println("Length: " + sendPacketFloor.getLength());
			      System.out.println("Containing: " + new String(sendPacketFloor.getData(),0,sendPacketFloor.getLength()));
			      System.out.println("Containing in bytes: " + Arrays.toString(sendPacketFloor.getData()));
			      
			      //if ack message from elevator 
			      if(!elevatorAck.isEmpty()) {
			    	  byte floorAck[] = new byte[100];
			    	  floorAck = elevatorAck.getBytes();
			    	  
					  //form Ack packet to floor
				      try {
				      sendAckPacketFloor = new DatagramPacket(floorAck, floorAck.length, InetAddress.getLocalHost(), receivePacketFloor.getPort());
				      }catch(UnknownHostException e) {
					         e.printStackTrace();
					         System.exit(1);
					      }
				      
				      //print the ack packet sent to floor
				      System.out.println("Scheduler: Ack Packet sent to Floor:");
				      System.out.println("From host: " + sendAckPacketFloor.getAddress());
				      System.out.println("Host port: " + sendAckPacketFloor.getPort());
				      System.out.println("Length: " + sendAckPacketFloor.getLength());
				      System.out.println("Containing: " + new String(sendAckPacketFloor.getData(),0,sendAckPacketFloor.getLength()));
				      System.out.println("Containing in bytes: " + Arrays.toString(sendAckPacketFloor.getData()));
				      
			    	  			    	  
			      }
			      
			      
			      			      			      			   			   			   
		   }
		   	   
	   }
	 
	 
	 public synchronized void sendReceiveElevator() {
		 int portNumber = 69;
		 
		   while(true) {
			   
			   byte elevatorData[] = new byte[100];
			   byte elevatorReply[] = new byte[100];
			   byte elevator[] = new byte[100];
			   
			   String reply = "Scheduler has received the request";
			   
			   //form packets to send to elevator
			   //call method to decide which elevator
			   
			      try {
			    	  sendPacketElevator = new DatagramPacket(receivePacketFloor.getData(), elevatorData.length, InetAddress.getLocalHost(), portNumber);
			      }catch(UnknownHostException e) {
				         e.printStackTrace();
				         System.exit(1);
				      }
			      
			      
			     //call the method to check if you can send this request to the elevator
			      
			     //send the packet to the elevator 
			      try {
			    	  elevatorSocket.send(sendPacketElevator);
				      } catch (IOException e) {
				         e.printStackTrace();
				         System.exit(1);
				      }			   			   			   
			      
			     //print the sent datagram to the elevator
			      System.out.println("Scheduler: Floor Request Packet sent to Elevator:");
			      System.out.println("From host: " + sendPacketElevator.getAddress());
			      System.out.println("Host port: " + sendPacketElevator.getPort());
			      System.out.println("Length: " + sendPacketElevator.getLength());
			      System.out.println("Containing: " + new String(sendPacketElevator.getData(),0,sendPacketElevator.getLength()));
			      System.out.println("Containing in bytes: " + Arrays.toString(sendPacketElevator.getData())); 
			      
			      
			      //form packet to receive from elevator
			      receivePacketElevator = new DatagramPacket(elevatorReply, elevatorReply.length);
			      
				// Block until a datagram packet is received from elevator
			      try {        
			         System.out.println(" Elevator method Waiting..."); // so we know we're waiting
			         elevatorSocket.receive(receivePacketElevator);
			        
			      } catch (IOException e) {
			         System.out.print("IO Exception: likely:");
			         System.out.println("Receive Socket Timed Out.\n" + e);
			         e.printStackTrace();
			         System.exit(1);
			      }
			      
			      //print the received datagram from the elevator
			      System.out.println("Scheduler: Floor Request Packet sent to Elevator:");
			      System.out.println("From host: " + receivePacketElevator.getAddress());
			      System.out.println("Host port: " + receivePacketElevator.getPort());
			      System.out.println("Length: " + receivePacketElevator.getLength());
			      System.out.println("Containing: " + new String(receivePacketElevator.getData(),0,receivePacketElevator.getLength()));
			      System.out.println("Containing in bytes: " + Arrays.toString(receivePacketElevator.getData())); 	
			      
			      //instantiate the Ack from the elevator
			      elevatorAck = Arrays.toString(receivePacketElevator.getData());
			      			      
			      

			    			     	      			   			   			   
		   }
		   
	   }

	/**
	 * Used to run the Scheduler thread.
	 */
	@Override
	public void run() {
		
		if(Thread.currentThread().getName().equals("Floor")) {
			System.out.println("Floor method runs");
			sendReceiveFloor();
			
        } else if (Thread.currentThread().getName().equals("Elevator")){
        	System.out.println("Elevator method runs");
        	sendReceiveElevator();
        }
		
		
//        boolean elevatorNotExecuted = true;
//        
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {}
//        
//        while(true) {
//        	System.out.println("SERVICEABLE REQUESTS QUEUE: " + getServiceableRequests());
//        	System.out.println("ALL REQUESTS QUEUE: " + getAllRequests());
//        	
//        	while(!getAllRequests().isEmpty() || !getServiceableRequests().isEmpty()) {
//	        	if (elevatorNotExecuted && getSchedulerToFloorCondition() == 0) {
//	        		// tell the elevator to start executing
//	                try {
//	                    Thread.sleep(1000);
//	                } catch (InterruptedException e) {}
//	        		idle = 0;
//	                System.out.println("\nScheduler: Request received from floor");	                
//	                System.out.println("Scheduler State = processing Requests from floor ");
//	                
//	                notifySchedulerToElevator();
//	                    
//	                elevatorNotExecuted = false;
//	                System.out.println("Scheduler: Request sent to elevator\n");     
//	                
//	            }
//	            else if (getSchedulerToElevatorCondition() == 0 && idle == 0) {
//	            	// tell the floor to start executing
//	            	System.out.println("\nScheduler State = Processing Requests from elevator ");
//	                System.out.println("Scheduler: Request received from elevator");
//	
//	                System.out.println("Scheduler: Request sent to floor");
//	               
//	                notifySchedulerToFloor();
//	                
//	                elevatorNotExecuted = true;
//	          
//	                break;
//	            }
//	            else {
//	                try {
//	                    Thread.sleep(1000);
//	                } catch (InterruptedException e) {}
//	            }
//	        }
//    		idle = 1;
//    		if (idle == 1 && getAllRequests().isEmpty() && getServiceableRequests().isEmpty()) {
//    			// idle when all the requests in the allFloorRequests have been serviced
//    			// and both queues are empty
//    			System.out.println("Scheduler State = Idle");
//    			break;
//    		}
//    		
//        }
        
    }
	public static void main(String args[])
	   {
		Scheduler s = new Scheduler();
	    Thread t1 = new Thread(s, "Floor");
	    Thread t2 = new Thread(s, "Elevator");
	    
	    
	    t1.start();
	    t2.start();
	    
	   }
	
}
