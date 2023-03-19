import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
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
	private Queue<FloorData> allFloorRequests;   // a queue of all requests in the CSV file
	private boolean floorRequestReceived;
	private int[] portNumbers = new int[5];
	private String elevatorAck = "";
	private int portNumber;
	
	DatagramPacket receivePacketFloor, receivePacketElevator, sendPacketFloor, sendPacketElevator1, sendPacketElevator2, sendAckPacketFloor;
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
		
		this.elevators = new ArrayList<Elevator>();
		this.allFloorRequests = new LinkedList<FloorData>();
		this.floorRequestReceived = false;
		
		//initialize ports, assumption: 2 elevators by default
		portNumbers[0] = 50;
		portNumbers[1] = 29;

		elevators.add(new Elevator( portNumbers[0])); //adding one default elevator to elevator list
		elevators.add(new Elevator( portNumbers[1]));
		
		portNumber = 0;
		
		  try {
		         floorSocket = new DatagramSocket(23);
		         elevatorSocket = new DatagramSocket();

		      } catch (SocketException se) {
		         se.printStackTrace();
		         System.exit(1);
		      }
	}
	
	// The first request is always gonna get serviced by the first elevator since both of them are stationary to begin with
	// Once we receive another request while requests are serviced by the elevator, we are gonna put the request in the
	// allRequests queue and send it to an elevator that is stationary. As they are performing requests, we are going to keep
	// putting them in the allRequests queue. 
	// As both the elevators are servicing a request, we are going to receive another request from floor. Then, we are going
	// to get messages from the elevators asking the scheduler whether it should stop or not. This is where we use the Wall Clock Time.
	// If request was made less than 8 seconds ago, then we can process that request.
	
	// We are getting the initial time of the request and comparing it to 8/2 = 4 seconds since we want to check
	// the whether we should stop at the next floor or not. We check this before we arrive at the next floor.
	
	
	// To DO at the end: Use larger port numbers instead of smaller ones
	
	// we are comparing the initial time of the second request to the initial time of the first request
	// if the first request (1 to 2) was made at 2:36:30s and the next request was made at 2:36:34s
	// Once we send the packet from the elevator to the scheduler, we subtract the actual time to the request time
	// Only check the difference if the request is in the same direction as the elevator
	// Current time (actual time): 2:36:30s, Request Time: 2:36:20s, Floor: Elevator is at floor 5
	// Current time - Request time = less than or equal to 8 seconds - the request is serviceable 
	// The elevator should send a request to the scheduler right before it arrives at each floor, asking it if it needs to stop at the next floor
	// If the elevator is going in the same direction as the request and the difference b/w req time is less than 8, then we process that request
	// and the floor that the request is being made is greater than elevator's current floor (going up) and is less than the elevator's current floor (going down)
	
	// If both elevators can service the request, we should give it to a random one
	// by choosing a port number based off a random object.
	// Use random object to pick either 1 or 2, if its 1 - use port 69. If its 2 - use port 70. 
	
	// Questions to ask Ben
	// does the csv file need to have requests within a short time span (i.e., 5 minute)?
	// would the system clock be relative to the requests on the csv file (i.e., set the system clock to whatever time the first request is set to in the csv file)?
	// the floor class should send requests based off the times in the csv files, therefore should we have a system clock be based off the earliest request in the csv file?
	// When do the elevators go back to stationary?
	
	/**
	 * Add requests to the allFloorRequests queue
	 * @param fd	a FloorData Object that gets added to the queue
	 */
	public void addRequests(FloorData fd) {
		
		allFloorRequests.add(fd);
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
	 * Adds to queue by parsing the packet it received from the floor
	 * @param arr	a String, the set of requests separated by a "/" that the scheduler received from the Floor
	 */
	private void parsePacket(String arr) {

        String data = arr;
        //System.out.println(data);

        String[] arrValues = data.split("/");

        for(int i = 0; i < arrValues.length ; i++) {
        	FloorData fd = new FloorData(10);

        	System.out.println("floorData: " + arrValues[i]);
            String[] arrValues2 = arrValues[i].split(",");

            String dateTimeString = arrValues2[0];
            String timeString = dateTimeString.substring(11, 19);

            try {
            	System.out.println("floorData: " + arrValues2[0]);
                String start_date = timeString;
                DateFormat formatter = new SimpleDateFormat("hh:mm:ss");
                Date date;

                date = (Date) formatter.parse(start_date);
                 fd.setTime(date);
                 fd.setInitialFloor(Integer.parseInt(arrValues2[1]));
                 fd.setFloorButton(arrValues2[2]);
                 fd.setDestinationFloor(Integer.parseInt(arrValues2[3]));


                 for(FloorData item: getAllRequests()) {
                	 System.out.println("ITEM IN QUEUE: " + item.getTime());
                 }
                 //add to queue here
                 addRequests(fd);
                 System.out.println("A request has been to the all Requests q" );

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
	/**
	 * Checks whether the request is serviceable at the moment
	 * @param fd	a FloorData object, the request that needs to be serviced
	 * @return	an int, port number of elevator we are sending the request to
	 */
	public int checkServiceableRequest(FloorData fd) {
		int currentFloorElevator1 = elevators.get(0).getCurrentFloor();
		int currentFloorElevator2 = elevators.get(1).getCurrentFloor();
		
		int directionElevator1 = elevators.get(0).getDirection();
		int directionElevator2 = elevators.get(1).getDirection();
		
		System.out.println("Direction elevator 1: " + directionElevator1);
		System.out.println("Direction elevator 2: " + directionElevator2);
		System.out.println("Current Floor elevator 1: " + currentFloorElevator1);
		System.out.println("Current Floor elevator 2: " + currentFloorElevator2);
		
		
		if(directionElevator1 == 2 && directionElevator2 == 2) {
			// both are stationary - give it elevator one
			if(fd.getFloorButton().equals("up")) {
				elevators.get(0).setDirection(0);
			} else {
				elevators.get(0).setDirection(1);
			}
			elevators.get(0).setCurrentFloor(fd.getDestinationFloor());
			elevators.get(0).setDirection(2);
			
			portNumber = 69;
		} else if ((directionElevator1 == 0 || directionElevator1 == 1) && directionElevator2 == 2) {
			// second elevator stationary - give it elevator 2
			if(fd.getFloorButton().equals("up")) {
				elevators.get(1).setDirection(0);
			} else {
				elevators.get(1).setDirection(1);
			}
			elevators.get(1).setCurrentFloor(fd.getDestinationFloor());
			elevators.get(1).setDirection(2);
			portNumber = 70;
		} 
		// Refactor everything below
		else if ((directionElevator2 == 0 || directionElevator2 == 1) && directionElevator1 == 2) {
			// first elevator stationary - give it to elevator 1
			if(fd.getFloorButton().equals("up")) {
				elevators.get(0).setDirection(0);
			} else {
				elevators.get(0).setDirection(1);
			}
			elevators.get(0).setCurrentFloor(fd.getDestinationFloor());
			portNumber = 69;
		} else if ((directionElevator2 == 0 || directionElevator2 == 1) && (directionElevator1 == 0 || directionElevator1 == 1)) {
			if(directionElevator1 == 0 && directionElevator2 == 1 && currentFloorElevator1 < fd.getDestinationFloor()) {
				// first elevator going up, second elevator going down 
				elevators.get(0).setCurrentFloor(fd.getDestinationFloor());
				portNumber = 69;
			} else if (directionElevator1 == 1 && directionElevator2 == 0 && currentFloorElevator1 > fd.getDestinationFloor()) {
				elevators.get(0).setCurrentFloor(fd.getDestinationFloor());
				portNumber = 69;
			} else if (directionElevator2 == 0 && directionElevator1 == 1 && currentFloorElevator2 < fd.getDestinationFloor()) {
				elevators.get(1).setCurrentFloor(fd.getDestinationFloor());
				portNumber = 70;
			} else if (directionElevator2 == 1 && directionElevator1 == 0 && currentFloorElevator2 > fd.getDestinationFloor()) {
				elevators.get(1).setCurrentFloor(fd.getDestinationFloor());
				portNumber = 70;
			} else if (directionElevator1 == 0 && directionElevator2 == 0 && (fd.getInitialFloor() > currentFloorElevator1) && (fd.getInitialFloor() > currentFloorElevator2)) {
				int distanceBetweenElevator1 = fd.getInitialFloor() - currentFloorElevator1;
				int distanceBetweenElevator2 = fd.getInitialFloor() - currentFloorElevator2;
				if(fd.getFloorButton().equals("up") && distanceBetweenElevator1 < distanceBetweenElevator2) {
					elevators.get(0).setCurrentFloor(fd.getDestinationFloor());
					portNumber = 69;
				} else if (fd.getFloorButton().equals("up") && distanceBetweenElevator2 < distanceBetweenElevator1) {
					elevators.get(1).setCurrentFloor(fd.getDestinationFloor());
					portNumber = 70;
				}
			} else if (directionElevator1 == 1 && directionElevator2 == 1 && (fd.getInitialFloor() < currentFloorElevator1) && (fd.getInitialFloor() < currentFloorElevator2)) {
				int distanceBetweenElevator1 = currentFloorElevator1 - fd.getInitialFloor();
				int distanceBetweenElevator2 = currentFloorElevator2 - fd.getInitialFloor();
				if(fd.getFloorButton().equals("down") && distanceBetweenElevator1 < distanceBetweenElevator2) {
					elevators.get(0).setCurrentFloor(fd.getDestinationFloor());
					portNumber = 69;
				} else if (fd.getFloorButton().equals("down") && distanceBetweenElevator2 < distanceBetweenElevator1) {
					elevators.get(1).setCurrentFloor(fd.getDestinationFloor());
					portNumber = 70;
				}
			}
		}
		return portNumber;
	}
		
	
	/**
	 * Send and receive DatagramPackets to/from floor 
	 */
	 public void sendReceiveFloor() {
		   
			   while(true) {
				   byte floorData[] = new byte[1000];
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
				      
				      parsePacket(new String(receivePacketFloor.getData(),0,receivePacketFloor.getLength()));
				      
				  
				      
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
				      
				      
				      // send the reply packet to floor
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
				      
				      if(floorRequestReceived) {
				    	  sendReceiveElevator();
				      }
					  
				 	   
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
					      
					      // send the reply packet to floor
					      try {
					    	  floorSocket.send(sendAckPacketFloor);
					      } catch (IOException e) {
					         e.printStackTrace();
					         System.exit(1);
					      }
			   }
			   }
			  
				    
			      }
			      
			      
			      		
	/**
	 * Send and receive DatagramPackets to/from elevator 
	 */
	 public void sendReceiveElevator() {
	
		 while(!allFloorRequests.isEmpty()) {
				   byte elevatorReply[] = new byte[100];
				   
				   //form packets to send to elevator
				   //call method to decide which elevator
				   
				   //System.out.println("Port number returned: " + checkServiceableRequest(getAllRequests().element()));
				   String elevatorOneRequests = "";
				   String elevatorTwoRequests = "";

				  int counter = 0;
				   // Loop through all requests and append them to the corresponding string
				   for (FloorData item : getAllRequests()) {
					   System.out.println("item: " + item.getTime());
				       if (counter % 2 == 0) {
				           elevatorOneRequests += item.getTime().toString() + ","
				                   + item.getInitialFloor() + ","
				                   + item.getFloorButton() + ","
				                   + item.getDestinationFloor() + "/";
				       }  else {
				           elevatorTwoRequests += item.getTime().toString() + ","
				                   + item.getInitialFloor() + ","
				                   + item.getFloorButton() + ","
				                   + item.getDestinationFloor() + "/";
				       }
				       counter++;
				   }
				   
				// Initialize DatagramPacket objects with the final values of the strings
				   try {
				       sendPacketElevator1 = new DatagramPacket(elevatorOneRequests.getBytes(), elevatorOneRequests.length(), InetAddress.getLocalHost(), 69);
				       sendPacketElevator2 = new DatagramPacket(elevatorTwoRequests.getBytes(), elevatorTwoRequests.length(), InetAddress.getLocalHost(), 70);

				       // Send the packets to the corresponding elevators
				       // ...
				   } catch (UnknownHostException e) {
				       e.printStackTrace();
				       System.exit(1);
				   }
				   
					System.out.println("being executed 1" + elevatorOneRequests);
					System.out.println("being executed 2" + elevatorTwoRequests);
				      
				      
				     //call the method to check if you can send this request to the elevator
				      
				     //send the packet to the elevator 
				      try {
				    	  elevatorSocket.send(sendPacketElevator1);
					      } catch (IOException e) {
					         e.printStackTrace();
					         System.exit(1);
					  }	
				      
				      try {
				    	  elevatorSocket.send(sendPacketElevator2);
					      } catch (IOException e) {
					         e.printStackTrace();
					         System.exit(1);
					  }	
				      
				     //print the sent datagram to the elevator
				      System.out.println("Scheduler: Floor Request Packet sent to Elevator:");
				      System.out.println("Containing: " + new String(sendPacketElevator1.getData(),0,sendPacketElevator1.getLength()));
				      System.out.println("Containing in bytes: " + Arrays.toString(sendPacketElevator1.getData())); 
				      
				      System.out.println("Scheduler: Floor Request Packet sent to Elevator:");
				      System.out.println("Containing: " + new String(sendPacketElevator2.getData(),0,sendPacketElevator2.getLength()));
				      System.out.println("Containing in bytes: " + Arrays.toString(sendPacketElevator2.getData())); 
				      
				      
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
				      System.out.println("Scheduler: Ack Packet received from Elevator:");
				      System.out.println("From host: " + receivePacketElevator.getAddress());
				      System.out.println("Host port: " + receivePacketElevator.getPort());
				      System.out.println("Length: " + receivePacketElevator.getLength());
				      System.out.println("Containing: " + new String(receivePacketElevator.getData(),0,receivePacketElevator.getLength()));
				      System.out.println("Containing in bytes: " + Arrays.toString(receivePacketElevator.getData())); 	
				      
				      allFloorRequests.remove();
				      //instantiate the Ack from the elevator
				      elevatorAck = new String(receivePacketElevator.getData(),0,receivePacketElevator.getLength());
				      			      
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
			
        } else {
        	System.out.println("Elevator method runs");
        	sendReceiveElevator();
        }
        
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

// The only thing that needs to be implemented/refactored in the scheduler class
// is the checkServiceableRequest method for now
