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
	
	DatagramPacket receivePacketFloor, receivePacketElevatorOne, receivePacketElevatorTwo, sendPacketFloor, sendPacketElevator1, sendPacketElevator2, sendAckPacketFloor;
	DatagramSocket sendAndReceiveSocket, receiveSocket, floorSocket, elevatorOneSocket, elevatorTwoSocket;

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

		elevators.add(new Elevator(portNumbers[0])); 
		elevators.add(new Elevator(portNumbers[1]));
		
		//portNumber = 2;
		
		  try {
		         floorSocket = new DatagramSocket(23);
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
	private void parsePacket(String arr) {
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
	
	private int checkServiceableRequest(FloorData fd) {
		// Calculate the distance between each elevator and the requested floor
	    int distElevator1 = Math.abs(elevators.get(0).getCurrentFloor() - fd.getInitialFloor());
	    int distElevator2 = Math.abs(elevators.get(1).getCurrentFloor() - fd.getInitialFloor());
	    
//		System.out.println("Current State elevator 1: " + elevators.get(0).getState());
//		System.out.println("Current State elevator 2: " + elevators.get(1).getState());

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
	    
	    // Check if one elevator is available and the other is moving in the same direction
	    if (elevators.get(0).getState() == 0 && elevators.get(1).getState() != 0) {
	        if ((elevators.get(1).getState() == 1 && fd.getFloorButton().equals("up")) ||
	                (elevators.get(1).getState() == 2 && fd.getFloorButton().equals("down"))) {
	        	elevators.get(0).setState(fd.getDestinationFloor() < elevators.get(0).getCurrentFloor() ? 2 : 1);
	            return 5000;
	        }
	    } else if (elevators.get(1).getState() == 0 && elevators.get(0).getState() != 0) {
	        if ((elevators.get(0).getState() == 1 && fd.getFloorButton().equals("up")) ||
	                (elevators.get(0).getState() == 2 && fd.getFloorButton().equals("down"))) {
	            elevators.get(1).setState(fd.getDestinationFloor() < elevators.get(1).getCurrentFloor() ? 2 : 1);
	            return 6000;
	        }
	    }
	    
	    // Check if one elevator is available and the other is moving in the opposite direction
	    if (elevators.get(0).getState() != 0 && elevators.get(1).getState() != 0) {
	        if ((elevators.get(0).getState() == 1 && elevators.get(1).getState() == 2 && fd.getFloorButton().equals("down")) ||
	                (elevators.get(0).getState() == 2 && elevators.get(1).getState() == 1 && fd.getFloorButton().equals("up"))) {
	            if (distElevator1 <= distElevator2) {
	                elevators.get(0).setState(fd.getDestinationFloor() < elevators.get(0).getCurrentFloor() ? 2 : 1);
	                return 5000;
	            } else {
	                elevators.get(1).setState(fd.getDestinationFloor() < elevators.get(1).getCurrentFloor() ? 2 : 1);
	                return 6000;
	            }
	        }
	    }
	    
	    elevatorsAvailable = false;
	    // No serviceable elevators available
	    return portNumber;
	}
//	/**
//	 * Checks whether the request is serviceable at the moment
//	 * @param fd	a FloorData object, the request that needs to be serviced
//	 * @return	an int, port number of elevator we are sending the request to
//	 */
//	public int checkServiceableRequest(FloorData fd) {
//		int currentFloorElevator1 = elevators.get(0).getCurrentFloor();
//		int currentFloorElevator2 = elevators.get(1).getCurrentFloor();
//		
//		System.out.println("Current State elevator 1: " + elevators.get(0).getState());
//		System.out.println("Current State elevator 2: " + elevators.get(1).getState());
//		
//		
//		if(elevators.get(0).getState() == 0 && elevators.get(1).getState() == 0) {
//			// both are stationary - give it elevator one
//			
//			elevators.get(0).setState(1);
//			portNumber = 5000;
//		} else if (elevators.get(0).getState() != 0 && elevators.get(1).getState() == 0) {
//			// second elevator stationary - give it elevator 2
//
//			elevators.get(1).setState(1);
//			portNumber = 6000;
//		} else if (elevators.get(0).getState() == 0 && elevators.get(1).getState() != 0) {
//		    // first elevator stationary - give it elevator 1
//			
//			elevators.get(0).setState(1);
//		    portNumber = 5000;
//		} else if (elevators.get(0).getState() != 0 && elevators.get(1).getState() != 0) {
//			return 6000;
//		}
//		
//		System.out.println("\nELEVATOR NUMBER: " + portNumber);
//		return portNumber;
//	}
//		
	
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
//				      if(elevators.get(0).getState() == 0 || elevators.get(1).getState() == 0) {
				          Thread thread = new Thread(() -> {
				                sendReceiveElevator();
				            });
				          thread.start();
				      //}

				      
//				      if(floorRequestReceived) {
//				    	  sendReceiveElevator();
//				      }
					  
				 	   
				      //if ack message from elevator 
//				      if(!elevatorAck.isEmpty()) {
//				    	  byte floorAck[] = new byte[100];
//				    	  floorAck = elevatorAck.getBytes();
//				    	  
//						  //form Ack packet to floor
//					      try {
//					    	  sendAckPacketFloor = new DatagramPacket(floorAck, floorAck.length, InetAddress.getLocalHost(), receivePacketFloor.getPort());
//					      }catch(UnknownHostException e) {
//						         e.printStackTrace();
//						         System.exit(1);
//						      }
//					      
//					      //print the ack packet sent to floor
//					      System.out.println("Scheduler: Ack Packet sent to Floor:");
//					      System.out.println("Containing: " + new String(sendAckPacketFloor.getData(),0,sendAckPacketFloor.getLength()));
//					      
//					      // send the reply packet to floor
//					      try {
//					    	  floorSocket.send(sendAckPacketFloor);
//					      } catch (IOException e) {
//					         e.printStackTrace();
//					         System.exit(1);
//					      }
//			   }
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
		        	  elevatorOneRequests += allFloorRequests.element().getTime().toString() + ","
                      + allFloorRequests.element().getInitialFloor() + ","
                      + allFloorRequests.element().getFloorButton() + ","
                      + allFloorRequests.element().getDestinationFloor() + "/";
		               
		              System.out.println("\nAssigned request(s) to Elevator One: " + elevatorOneRequests);

		              try {
		                  sendPacketElevator1 = new DatagramPacket(elevatorOneRequests.getBytes(), elevatorOneRequests.length(), InetAddress.getLocalHost(), 5000);
		              } catch (UnknownHostException e1) {
		                  e1.printStackTrace();
		              }
		              //send the packet to the elevator 
		              try {
		            	  elevatorOneSocket.send(sendPacketElevator1);
		              } catch (IOException e) {
		                  e.printStackTrace();
		                  System.exit(1);
		              }   
		              System.out.println("Containing: " + new String(sendPacketElevator1.getData(),0,sendPacketElevator1.getLength()));
		               
			          // Remove the element from the queue after sending it to the elevator
		              allFloorRequests.remove();
		              //form packet to receive from elevator one
		              receivePacketElevatorOne = new DatagramPacket(elevatorOneReply, elevatorOneReply.length);

					      
					 // Block until a datagram packet is received from elevator to indicate that it successfully processed 
					 // the request
				      try {        
				         System.out.println("Waiting for acknowledgement packet from Elevator One...");
				         elevatorOneSocket.receive(receivePacketElevatorOne);							        
				      } catch (IOException e) {
				         System.out.print("IO Exception: likely:");
				         System.out.println("Receive Socket Timed Out.\n" + e);
				         e.printStackTrace();
				         System.exit(1);
				      }
					      
					      
				      //print the received datagram from the elevator
				      System.out.println("Scheduler: Ack Packet received from Elevator:");
				      System.out.println("Containing: " + new String(receivePacketElevatorOne.getData(),0,receivePacketElevatorOne.getLength()));
	
			          // Once acknowledgement packet is received, we know the elevator is stationary and 
				      // is not processing any requests
		        	  elevators.get(0).setState(0);
		        	  System.out.println("\nElevator One is Stationary\n");
		        	  elevatorsAvailable = true;
			         
				      
				      // instantiate the Ack from the elevator
				      elevatorAck = new String(receivePacketElevatorOne.getData(),0,receivePacketElevatorOne.getLength());		      
		               break;
		           } else if (portNumber == 6000) {
		               elevatorTwoRequests += allFloorRequests.element().getTime().toString() + ","
		                                      + allFloorRequests.element().getInitialFloor() + ","
		                                      + allFloorRequests.element().getFloorButton() + ","
		                                      + allFloorRequests.element().getDestinationFloor() + "/";
		               
		               System.out.println("\nAssigned request(s) to Elevator Two: " + elevatorTwoRequests);
		               try {
		                   sendPacketElevator2 = new DatagramPacket(elevatorTwoRequests.getBytes(), elevatorTwoRequests.length(), InetAddress.getLocalHost(), 6000);
		               } catch (UnknownHostException e1) {
		                   e1.printStackTrace();
		               }

		               try {
		                   elevatorTwoSocket.send(sendPacketElevator2);
		               } catch (IOException e) {
		                   e.printStackTrace();
		                   System.exit(1);
		               }   
		               System.out.println("Containing: " + new String(sendPacketElevator2.getData(),0,sendPacketElevator2.getLength()));
		               
			           // Remove the element from the queue after sending it to the elevator
		               allFloorRequests.remove();
		               

		               receivePacketElevatorTwo = new DatagramPacket(elevatorTwoReply, elevatorTwoReply.length);

					      
					   // Block until a datagram packet is received from elevator
					   try {        
						   System.out.println("Waiting for acknowledgement packet from Elevator Two..."); // so we know we're waiting
					       elevatorTwoSocket.receive(receivePacketElevatorTwo);					        
					    } catch (IOException e) {
					       System.out.print("IO Exception: likely:");
					       System.out.println("Receive Socket Timed Out.\n" + e);
					       e.printStackTrace();
					       System.exit(1);
					    }
					      					      
					    // print the received datagram from the elevator
					    System.out.println("Scheduler: Ack Packet received from Elevator:");
					    System.out.println("Containing: " + new String(receivePacketElevatorTwo.getData(),0,receivePacketElevatorTwo.getLength()));
		
				        // Once acknowledgement packet is received, we know the elevator is stationary and 
					    // is not processing any requests
			        	elevators.get(1).setState(0);
			        	System.out.println("\nElevator Two is Stationary\n");
			        	elevatorsAvailable = true;
				       					      
					    // instantiate the Acknowledgement from the elevator
					    elevatorAck = new String(receivePacketElevatorTwo.getData(),0,receivePacketElevatorTwo.getLength());					      			      
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
		    //elevatorThread.start();
	    
	   }

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
}
