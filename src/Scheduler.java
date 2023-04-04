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
import java.util.Iterator;
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
	private Object lock = new Object();
	private volatile boolean elevatorsAvailable = true;
	
	DatagramPacket receivePacketFloor, receiveAcknowledgementFloor, receivePacketElevatorOne, receivePacketElevatorTwo, sendPacketFloor, sendPacketElevator1, sendPacketElevator2, sendAckPacketFloor;
	DatagramSocket sendAndReceiveSocket, receiveSocket, floorSocket, elevatorOneSocket, elevatorTwoSocket, floorSendSocket;

	/**
	 * Constructor for Scheduler.
	 */
	public Scheduler() {
		
		this.elevators = new ArrayList<Elevator>();
		this.allFloorRequests = new LinkedList<FloorData>();
		this.floorRequestReceived = false;
		
		// initialize ports, assumption: 2 elevators by default
		portNumbers[0] = 50;
		portNumbers[1] = 29;

		elevators.add(new Elevator(portNumbers[0], "Elevator One")); 
		elevators.add(new Elevator(portNumbers[1], "Elevator Two"));
		
		try {
	         floorSocket = new DatagramSocket(23);
	         floorSendSocket = new DatagramSocket(24);
	         elevatorOneSocket = new DatagramSocket(2000);
	         elevatorTwoSocket = new DatagramSocket(2500);

	      } catch (SocketException se) {
	         se.printStackTrace();
	         System.exit(1);
	      }
	}
	
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
	public void parsePacket(String arr) {
        String data = arr;

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
                 synchronized(lock) {
                     //add to queue here
                     addRequests(fd);
                     lock.notifyAll();
                 }
                 System.out.println("A request has been to the all Requests q" );

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
	
    public void startFloorReceiverThread(int port) {
        Runnable floorReceiver = () -> {
            DatagramSocket socket = null;
            try {
                socket = new DatagramSocket(port);
                while (true) {
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String floorString = new String(packet.getData(), 0, packet.getLength());
                    String[] parts = floorString.split(":");
                    String elevator = parts[0];
                    int floor = Integer.parseInt(parts[1].trim());
                    int state = Integer.parseInt(parts[2].trim());
                    
                    if (elevator.equals("Elevator One")) {
                        elevators.get(0).setCurrentFloor(floor);
                        elevators.get(0).setState(state);
                    } else if (elevator.equals("Elevator Two")) {
                    	elevators.get(1).setCurrentFloor(floor);
                    	elevators.get(1).setState(state);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    socket.close();
                }
            }
        };
        new Thread(floorReceiver).start();
    }
    
	/**
	 * Assigns the appropriate request to the appropriate elevator based off which elevator is stationary
	 * and what floors the elevators are currently at
	 * @param fd	a FloorData Object, the information of the request
	 * @return	an int, the port number of the elevator that the request gets assigned to
	 */
	public int checkServiceableRequest(FloorData fd) {
		// Calculate the distance between each elevator and the requested floor
	    int distElevator1 = Math.abs(elevators.get(0).getCurrentFloor() - fd.getInitialFloor());
	    int distElevator2 = Math.abs(elevators.get(1).getCurrentFloor() - fd.getInitialFloor());
	    
//    	System.out.println("Current FLOOR elevator 1: " + elevators.get(0).getCurrentFloor());
//		System.out.println("Current FLOOR elevator 2: " + elevators.get(1).getCurrentFloor());
//		
//    	System.out.println("Current STATE elevator 1: " + elevators.get(0).getState());
//		System.out.println("Current STATE elevator 2: " + elevators.get(1).getState());
		
		
	  
//	    if (elevators.get(0).getCurrentRequest().getTime() != null) {
//	    	System.out.println("Current Request elevator 1: " + elevators.get(0).getCurrentRequest().getTime());
//			System.out.println("Current Request elevator 2: " + elevators.get(1).getCurrentRequest().getTime());
//	    }
	    

	    // Check if both elevators are available
	    if (elevators.get(0).getState() == 0 && elevators.get(1).getState() == 0) {
	    	// Assign the closest elevator to handle the request
	        if (distElevator1 <= distElevator2) {
	            elevators.get(0).setState(fd.getDestinationFloor() < elevators.get(0).getCurrentFloor() ? 2 : 1);
	            return 5000;
	        } else {
	        	elevators.get(1).setState(fd.getDestinationFloor() < elevators.get(1).getCurrentFloor() ? 2 : 1);
	            return 6000;
	        }
	    }
	    
	    // Check if one elevator is available
	    if (elevators.get(0).getState() == 0 && elevators.get(1).getState() != 0) {
	    	// if second elevator is currently moving (going up or going down), then
	    	// we want to set the state of the first elevator based on the destination floor
	    	// if the destination floor is less than the current floor of the first elevator, then set its state to moving down
	    	// if the destination floor is greater than the current floor of the first elevator, then set its state to moving up
	        if ((elevators.get(1).getState() == 1) ||
	                (elevators.get(1).getState() == 2)) {
	        	elevators.get(0).setState(fd.getDestinationFloor() < elevators.get(0).getCurrentFloor() ? 2 : 1);
	            return 5000;
	        }
	    } 
	    else if (elevators.get(1).getState() == 0 && elevators.get(0).getState() != 0) {
	    	// if first elevator is currently moving (going up or going down), then
	    	// we want to set the state of the second elevator based on the destination floor
	    	// if the destination floor is less than the current floor of the second elevator, then set its state to moving down
	    	// if the destination floor is greater than the current floor of the second elevator, then set its state to moving up 
	        if ((elevators.get(0).getState() == 1) ||
	                (elevators.get(0).getState() == 2)) {
	            elevators.get(1).setState(fd.getDestinationFloor() < elevators.get(1).getCurrentFloor() ? 2 : 1);
	            return 6000;
	        }
	    }
	    
	    //System.out.println("Current TIME elevator 1 request: " + fd.getTime() + " " + (elevators.get(0).getCurrentRequest().getTime()));
	    // first elevator: 2, 8
	    // second elevator: 1, 4
	    
	    // current floor of the first elevator: floor 2
	    // 3, 5
	    
	    // Check if no elevators are available
	    if (elevators.get(0).getState() != 0 && elevators.get(1).getState() != 0) {
//	    		&& (fd.getTime().equals(elevators.get(0).getCurrentRequest().getTime()) 
//	    		|| fd.getTime().equals(elevators.get(1).getCurrentRequest().getTime()))) {
	    	
	    	// Both elevators are moving up
	        if ((elevators.get(0).getState() == 1 && elevators.get(1).getState() == 1)) {
	        	
		    	// request lies between initial floor and destination floor
		    	// and initial floor of request is greater than the current floor the elevator is currently on
		    	// then we want to assign it to the appropriate elevator
	        	if(fd.getInitialFloor() >= elevators.get(0).getCurrentRequest().getInitialFloor() &&
	        			   fd.getInitialFloor() > elevators.get(0).getCurrentFloor() &&
	        			   fd.getInitialFloor() <= elevators.get(0).getCurrentRequest().getDestinationFloor()) {
	        		System.out.println("ELEVATOR 1 ASSIGNED UP INITIAL FLOOR: " + elevators.get(0).getCurrentRequest().getInitialFloor());
	        		System.out.println("ELEVATOR 1 CURRENT FLOOR: " + elevators.get(0).getCurrentFloor());
	        		System.out.println("REQUEST DESTINATION FLOOR: " + elevators.get(0).getCurrentRequest().getDestinationFloor());
	        		return 5000;
	        	} else if (fd.getInitialFloor() >= elevators.get(1).getCurrentRequest().getInitialFloor() &&
	        			   fd.getInitialFloor() > elevators.get(1).getCurrentFloor() &&
	        			   fd.getInitialFloor() <= elevators.get(1).getCurrentRequest().getDestinationFloor()) {
	        		System.out.println("ELEVATOR 2 ASSIGNED UP: " + elevators.get(0).getCurrentRequest().getInitialFloor());
	        		return 6000;
	        	}
	        }
	        
	        // Both elevators are moving down
	        if ((elevators.get(0).getState() == 2 && elevators.get(1).getState() == 2)) {
	        
	        	
		    	// request lies between initial floor and destination floor
		    	// and initial floor of request is less than the current floor the elevator is currently on
		    	// then we want to assign it to the appropriate elevator
	        	if(fd.getInitialFloor() >= elevators.get(0).getCurrentRequest().getInitialFloor() &&
	        			   fd.getInitialFloor() < elevators.get(0).getCurrentFloor() &&
	        			   fd.getInitialFloor() <= elevators.get(0).getCurrentRequest().getDestinationFloor()) {
	        		System.out.println("ELEVATOR 1 ASSIGNED DOWN: " + elevators.get(0).getCurrentRequest().getInitialFloor());
	        		return 5000;
	        	} else if (fd.getInitialFloor() >= elevators.get(1).getCurrentRequest().getInitialFloor() &&
	        			   fd.getInitialFloor() < elevators.get(1).getCurrentFloor() &&
	        			   fd.getInitialFloor() <= elevators.get(1).getCurrentRequest().getDestinationFloor()) {
	        		System.out.println("ELEVATOR 2 ASSIGNED DOWN: " + elevators.get(0).getCurrentRequest().getInitialFloor());
	        		return 6000;
	        	}
	        }
//	        if ((elevators.get(0).getState() == 1 && elevators.get(1).getState() == 2) ||
//	                (elevators.get(0).getState() == 2 && elevators.get(1).getState() == 1)) {
//	        	// if the first elevator is moving up and the second elevator is moving down (and the request involves going down)
//	        	// OR if the first elevator is moving down and the second elevator is moving up (and the request involves going up)
//	        	// then assign request based on the distance of each elevator
//	            if (distElevator1 <= distElevator2) {
//	            	// if elevator one is closer to the request, assign it to elevator one
//	                elevators.get(0).setState(fd.getDestinationFloor() < elevators.get(0).getCurrentFloor() ? 2 : 1);
//	                return 5000;
//	            } else {
//	            	// if elevator two is closer to the request, assign it to elevator two
//	                elevators.get(1).setState(fd.getDestinationFloor() < elevators.get(1).getCurrentFloor() ? 2 : 1);
//	                return 6000;
//	            }
//	        }
	    }
    	elevatorsAvailable = false;		// No serviceable elevators available
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
				      System.out.println("Containing: " + new String(receivePacketFloor.getData(),0,receivePacketFloor.getLength()));
				      
				      
				     // form packet to reply to floor
				      floorReply = reply.getBytes();
				      try {
				    	   sendPacketFloor = new DatagramPacket(floorReply, floorReply.length, InetAddress.getLocalHost(), receivePacketFloor.getPort());
				      } catch(UnknownHostException e) {
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
				      System.out.println("Containing: " + new String(sendPacketFloor.getData(),0,sendPacketFloor.getLength()));

			          Thread processElevatorRequests = new Thread(() -> {
			                sendReceiveElevator();
			            });
			          processElevatorRequests.start();
			   }
			   
			  
				    
}
			      
	 
	 private void handleElevatorRequest(int portNumber, String requests, DatagramSocket socket, DatagramPacket sendPacket, DatagramPacket receivePacket, byte[] reply) {
	    requests += allFloorRequests.element().getTime().toString() + ","
	                + allFloorRequests.element().getInitialFloor() + ","
	                + allFloorRequests.element().getFloorButton() + ","
	                + allFloorRequests.element().getDestinationFloor() + "/";

	    System.out.println("\nAssigned request(s) to Elevator " + (portNumber == 5000 ? 1 : portNumber == 6000 ? 2 : portNumber == 7000 ? 3 : 4) + ": " + requests);

	    try {
	        sendPacket = new DatagramPacket(requests.getBytes(), requests.length(), InetAddress.getLocalHost(), portNumber);
	    } catch (UnknownHostException e1) {
	        e1.printStackTrace();
	    }

	    try {
	        socket.send(sendPacket);
	    } catch (IOException e) {
	        e.printStackTrace();
	        System.exit(1);
	    }   
	    System.out.println("Containing: " + new String(sendPacket.getData(),0,sendPacket.getLength()));
	    
	    if(portNumber == 5000) {
	    	elevators.get(0).setCurrentRequest(allFloorRequests.element());
	    } else if (portNumber == 6000) {
	    	elevators.get(1).setCurrentRequest(allFloorRequests.element());
	    }
	    FloorData currentRequest = allFloorRequests.element();
	    // Remove the element from the queue after sending it to the elevator
	    allFloorRequests.remove();
	    
	    receivePacket = new DatagramPacket(reply, reply.length);
	    
	    // Block until a datagram packet is received from elevator to indicate that it successfully processed 
	    // the request
	    try {        
	    	System.out.println("Waiting for acknowledgement packet from Elevator " + (portNumber == 5000 ? 1 : portNumber == 6000 ? 2 : portNumber == 7000 ? 3 : 4) + "...");
	        socket.receive(receivePacket);                        
	    } catch (IOException e) {
	        System.out.print("IO Exception: likely:");
	        System.out.println("Receive Socket Timed Out.\n" + e);
	        e.printStackTrace();
	        System.exit(1);
	    }
	    
	    //print the received datagram from the elevator
	    System.out.println("Scheduler: Ack Packet received from Elevator:");
	    System.out.println("Containing: " + new String(receivePacket.getData(),0,receivePacket.getLength()));
	    
	    // Once acknowledgement packet is received, we know the elevator is stationary and 
	    // is not processing any requests
	    int elevatorIndex = portNumber == 5000 ? 0 : portNumber == 6000 ? 1 : portNumber == 7000 ? 2 : 3;
	    elevators.get(elevatorIndex).setState(0);
	    
	    System.out.println("\nElevator " + (portNumber == 5000 ? 1 : portNumber == 6000 ? 2 : portNumber == 7000 ? 3 : 4) + " is Stationary\n");
	    
	    elevatorsAvailable = true;
	    
	    // instantiate the Ack from the elevator
	    elevatorAck = new String(receivePacket.getData(),0,receivePacket.getLength());
	    elevatorAck += " successfully processed request: initial floor: " + currentRequest.getInitialFloor() + " and destination floor: " + currentRequest.getDestinationFloor();
	    elevatorAck += " \nas it arrived at floor " + currentRequest.getDestinationFloor();
	    
 	  
		//form Ack packet to floor
    	DatagramPacket sendAckPacketFloor = null;
		try {
			sendAckPacketFloor = new DatagramPacket(elevatorAck.getBytes(), elevatorAck.length(), InetAddress.getLocalHost(), 1500);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
      
      //print the ack packet sent to floor
      System.out.println("Scheduler: Ack Packet sent to Floor:");
      System.out.println("Containing: " + new String(sendAckPacketFloor.getData(),0,sendAckPacketFloor.getLength()));
            
      try {
    	  floorSendSocket.send(sendAckPacketFloor);
      } catch (IOException e) {
         e.printStackTrace();
         System.exit(1);
      }
	    
	    
}

			      
			      		
	/**
	 * Send and receive DatagramPackets to/from elevator 
	 */
	 public void sendReceiveElevator() {
		 
		 synchronized(lock) {
			 while(allFloorRequests.isEmpty()) {
				 try {
					 lock.wait();
				 } catch(InterruptedException e) {
					 return;
				 }
			 }
		 }
	     byte elevatorOneReply[] = new byte[100];
	     byte elevatorTwoReply[] = new byte[100];
	     int portNumber = 0;
	   
	     //form packets to send to elevator
	     //call method to decide which elevator
	     String elevatorOneRequests = "";
	     String elevatorTwoRequests = "";

		 while (true) { 
			 portNumber = checkServiceableRequest(allFloorRequests.element());
		     if (elevatorsAvailable) {
		          if (portNumber == 5000) {
		        	   handleElevatorRequest(5000, elevatorOneRequests, elevatorOneSocket, sendPacketElevator1, receivePacketElevatorOne, elevatorOneReply);		      
		               break;
		           } else if (portNumber == 6000) {
		        	   handleElevatorRequest(6000, elevatorTwoRequests, elevatorTwoSocket, sendPacketElevator2, receivePacketElevatorTwo, elevatorTwoReply);     
					   break;
		           }		           
		       } else {
		    	   // Elevators are unavailable since both of them are moving
		    	   // Keep checking whether an elevator becomes available by going to the beginning of the loop
		           try {
		               Thread.sleep(1000);
		           } catch (InterruptedException e) {
		               e.printStackTrace();
		           }
		           continue;
		       }
		 }
}
		 
		 
	 
			      			     	      			   			   			   
		   

//	/**
//	 * Used to run the Scheduler thread.
//	 */
//	@Override
//	public void run() {
//		
//		if(Thread.currentThread().getName().equals("Floor")) {
//			System.out.println("Floor method runs");
//			sendReceiveFloor();
//			
//        } else {
//        	System.out.println("Elevator method runs");
//        	sendReceiveElevator();
//        }
//        
//    }
	
	public static void main(String args[])
	   { 
			Scheduler s = new Scheduler();
		    Thread floorThread = new Thread(s::sendReceiveFloor);
//		    Thread elevatorThread = new Thread(s::sendReceiveElevator);
		    
		    
		    floorThread.start();
		    s.startFloorReceiverThread(4000);
		    //elevatorThread.start();
	    
	   }

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
}
