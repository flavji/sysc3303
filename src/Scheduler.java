import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Scheduler Class that consists of a thread that is used as a communication channel between the clients (i.e., floor and elevator).
 *
 * @author Yash Kapoor
 * @author Faiaz Ahsan
 * @author Zeid Alwash
 * @author Fareen Lavji
 * @author Harishan Amutheesan
 * 
 * @version Final Project Submission
 */
public class Scheduler {
	
	private ArrayList<Elevator> elevators; // collection of elevators
	private Queue<FloorData> allFloorRequests;   // a queue of all requests in the CSV file
	private int[] portNumbers = new int[5];
	private String elevatorAck = "";
	private Object lock = new Object();
	private volatile boolean elevatorsAvailable = true;
	private boolean shouldRequestUpdate = true;
	
	DatagramPacket receivePacketFloor, receiveAcknowledgementFloor, receivePacketElevatorOne, receivePacketElevatorTwo, receivePacketElevatorThree,
	receivePacketElevatorFour, sendPacketFloor, sendPacketElevator1, sendPacketElevator2, sendPacketElevator3, sendPacketElevator4, sendAckPacketFloor;
	DatagramSocket sendAndReceiveSocket, receiveSocket, floorSocket, elevatorOneSocket, elevatorTwoSocket, elevatorThreeSocket, elevatorFourSocket, floorSendSocket;

	/**
	 * Constructor for Scheduler.
	 */
	public Scheduler() {
		
		this.elevators = new ArrayList<Elevator>();
		this.allFloorRequests = new LinkedList<FloorData>();
		
		// initialize ports, assumption: 2 elevators by default
		portNumbers[0] = 50;
		portNumbers[1] = 29;
		portNumbers[2] = 300;
		portNumbers[3] = 400;

		elevators.add(new Elevator(portNumbers[0], "Elevator One")); 
		elevators.add(new Elevator(portNumbers[1], "Elevator Two"));
		elevators.add(new Elevator(portNumbers[2], "Elevator Three"));
		elevators.add(new Elevator(portNumbers[3], "Elevator Four"));
		elevators.get(0).gui.closeGUI();
		elevators.get(1).gui.closeGUI();
		elevators.get(2).gui.closeGUI();
		elevators.get(3).gui.closeGUI();
		
		try {
	         floorSocket = new DatagramSocket(23);
	         floorSendSocket = new DatagramSocket(24);
	         elevatorOneSocket = new DatagramSocket(2000);
	         elevatorTwoSocket = new DatagramSocket(2500);
	         elevatorThreeSocket = new DatagramSocket(3000);
	         elevatorFourSocket = new DatagramSocket(3500);

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
        	FloorData fd = new FloorData(22);

            String[] arrValues2 = arrValues[i].split(",");

            String dateTimeString = arrValues2[0];
            String timeString = dateTimeString.substring(11, 19);

            try {
                String start_date = timeString;
                DateFormat formatter = new SimpleDateFormat("hh:mm:ss");
                Date date;

                date = (Date) formatter.parse(start_date);
                 fd.setTime(date);
                 fd.setInitialFloor(Integer.parseInt(arrValues2[1]));
                 fd.setFloorButton(arrValues2[2]);
                 fd.setDestinationFloor(Integer.parseInt(arrValues2[3]));


                 synchronized(lock) {
                     //add to queue here
                     addRequests(fd);
                     lock.notifyAll();
                 }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
	
	/**
	 * Constantly listens for information (current floor and state of the elevator) 
	 * from the elevator, so it can always know what the current floor and
	 * state of the elevator is.
	 * @param port	an int, the port number of the socket
	 */
    public void receiveElevatorStatus(int port) {
        Runnable elevatorStatusReceiver = () -> {
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
                    } else if (elevator.equals("Elevator Three")) {
                    	elevators.get(2).setCurrentFloor(floor);
                    	elevators.get(2).setState(state);
                    } else if (elevator.equals("Elevator Four")) {
                    	elevators.get(3).setCurrentFloor(floor);
                    	elevators.get(3).setState(state);
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
        new Thread(elevatorStatusReceiver).start();
    }
    
	/**
	 * Assigns the appropriate request to the appropriate elevator.
	 * 
	 * If multiple elevators are stationary (state is 0), it assigns the request to
	 * the nearest elevator. 
	 * 
	 * If all elevators are moving (state is not 0), it checks to see if the request is 
	 * in the same direction as the elevator is currently going in (either up or down).
	 * If that's true, then it checks to see
	 * if the initial floor of the request lies between the current floor of the elevator
	 * and the destination floor of the request the elevator is currently processing. 
	 * If that's true, then that elevator is assigned the request.
	 * 
	 * If the above is true for multiple elevators, then it assigns the request to the nearest 
	 * elevator based off the current floor of the elevator.
	 * 
	 * If none of the conditions above are true, then there are no serviceable elevators available
	 * at the moment.
	 * 
	 * @param fd	a FloorData Object, the information of the request
	 * @return	an int, the port number of the elevator that the request gets assigned to
	 * 
	 * If there are no serviceable elevators available, then this method returns 0.
	 */
	public int checkServiceableRequest(FloorData fd) {
		
		// First Case: go through the elevators and assign the request to the closest elevator that is stationary 
		boolean elevatorFound = false;
		int minDist = Integer.MAX_VALUE;
		int elevatorIdx = -1;
		for(int elevatorNumber = 0; elevatorNumber < 4; elevatorNumber++) {
			if(elevators.get(elevatorNumber).getState() == 0) {
				elevatorFound = true;
				int dist = Math.abs(fd.getInitialFloor() - elevators.get(elevatorNumber).getCurrentFloor());
				if(dist < minDist) {
					minDist = dist;
					elevatorIdx = elevatorNumber;
				}
			}
		}
		if (elevatorFound) {
			System.out.println("ELEVATOR NUMBER: " + elevatorIdx);
			elevators.get(elevatorIdx).setState(fd.getDestinationFloor() < elevators.get(elevatorIdx).getCurrentFloor() ? 2 : 1);
		    return 5000 + (elevatorIdx * 1000);
		}
	    	    
		
		// Second Case: all the elevators are moving
		// and the received request is between the initial floor and the destination floor.
		// If the condition above is true for multiple elevators, the request is assigned
		// to the closest elevator.
		
	    // Check if no elevators are available
		boolean allBusy = elevators.stream().allMatch(elevator -> elevator.getState() != 0);
		if (allBusy) {
			int closestElevator = -1;
			int minDistance = Integer.MAX_VALUE;

			int elevatorNumber;
			// Find the elevator that is closest to the initial floor of the request
			 for (elevatorNumber = 0; elevatorNumber < 4; elevatorNumber++) {
			    Elevator elevator = elevators.get(elevatorNumber);
			    boolean upDirection = fd.getInitialFloor() < fd.getDestinationFloor();
			    boolean downDirection = fd.getInitialFloor() > fd.getDestinationFloor();

			    // request and elevator's direction are the same (up) and the initial floor of the request lies
			    // the current floor of the elevator and the destination floor of the request
			    if ((upDirection && elevator.getState() == 1) && fd.getInitialFloor() >= elevator.getInitialRequest().getInitialFloor() && 
		                fd.getInitialFloor() > elevator.getCurrentFloor() && 
		                fd.getInitialFloor() <= elevator.getInitialRequest().getDestinationFloor()) {
			        int distance = fd.getInitialFloor() - elevator.getCurrentFloor();
			        // assign to closest elevator if it is true for multiple elevators
			        if (distance < minDistance) {
			            closestElevator = elevatorNumber;
			            minDistance = distance;
			        }
			    } 
			    // request and elevator's direction are the same (down) and the initial floor of the request lies
			    // the current floor of the elevator and the destination floor of the request
			    else if ((downDirection && elevator.getState() == 2) && fd.getInitialFloor() >= elevator.getInitialRequest().getInitialFloor() && 
	                       fd.getInitialFloor() < elevator.getCurrentFloor() && 
	                       fd.getInitialFloor() <= elevator.getInitialRequest().getDestinationFloor()) {
			        int distance = elevator.getCurrentFloor() - fd.getInitialFloor();			        
			        // assign to closest elevator if it is true for multiple elevators
			        if (distance < minDistance) {
			            closestElevator = elevatorNumber;
			            minDistance = distance;
			        }
			    }
			}

			// If a closest elevator is found, assign the request to it
			if (closestElevator != -1) {
				shouldRequestUpdate = false;
			    return 5000 + (closestElevator * 1000);
			}
		}
    	elevatorsAvailable = false;		// No serviceable elevators available
	    return 0;
	}
	
	/**
	 * Send and receive DatagramPackets to/from floor.
	 * 
	 * Receives the requests from the floor and sends a reply to the floor
	 * indicating that it has received the request.
	 */
	 public void sendReceiveFloor() {
		 while(true) {
		   byte floorData[] = new byte[1000];
		   byte floorReply[] = new byte[100];
		   String reply = "Scheduler has received the request";
		   
		   //form packets to receive from elevator
		   receivePacketFloor = new DatagramPacket(floorData, floorData.length);

		   			   
			// Block until a datagram packet is received from elevator
		      try {        
		         System.out.println("\nScheduler: Waiting for Request from Floor..."); 
		         floorSocket.receive(receivePacketFloor);
		        
		      } catch (IOException e) {
		         System.out.print("IO Exception: likely:");
		         System.out.println("Receive Socket Timed Out.\n" + e);
		         e.printStackTrace();
		         System.exit(1);
		      }
		      
		      
		      parsePacket(new String(receivePacketFloor.getData(),0,receivePacketFloor.getLength()));
		      
		  
		      
		     //print the received datagram
		      System.out.println("\nScheduler: Request received from Floor");
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
		      
			  // print the reply sent back to floor
		      System.out.println("Scheduler: Reply Packet sent to Floor:");
		      System.out.println("Containing: " + new String(sendPacketFloor.getData(),0,sendPacketFloor.getLength()));

		      // Creates and starts a new thread for every request 
		      // and calls the sendReceiveElevator() method, so this method can 
		      // keep on receiving requests, while the sendReceiveElevator()
		      // method processes them
	          Thread processElevatorRequests = new Thread(() -> {
	                sendReceiveElevator();
	            });
	          processElevatorRequests.start();
	   }				    
}
			      
	 /**
	  * Handles the elevator request by sending it to the elevator and waiting for an acknowledgement.
	  * Once an acknowledgement is received from the elevator, it is sent to the floor.
	  * @param portNumber
	  * @param requests
	  * @param socket
	  * @param sendPacket
	  * @param receivePacket
	  * @param reply
	  */
	 private void handleElevatorRequest(int portNumber, String requests, DatagramSocket socket, DatagramPacket sendPacket, DatagramPacket receivePacket, byte[] reply) {
	    requests += allFloorRequests.element().getTime().toString() + ","
	                + allFloorRequests.element().getInitialFloor() + ","
	                + allFloorRequests.element().getFloorButton() + ","
	                + allFloorRequests.element().getDestinationFloor();

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
	    System.out.println("Request to be Processed by the Elevator: " + new String(sendPacket.getData(),0,sendPacket.getLength()));
	    
	    int index = (portNumber - 5000) / 1000; // Calculate the index based on the port number

	    // if the request lies between the initial floor 
	    // and the destination floor of the request the elevator is currently processing,
	    // then we do not want to update the initial request of the elevator.
	    if (shouldRequestUpdate) {
	    	// only update this when the elevator is stationary
	        elevators.get(index).setInitialRequest(allFloorRequests.element());
	    }

	    elevators.get(index).setCurrentRequest(allFloorRequests.element());

	    shouldRequestUpdate = true;
	    
	    FloorData currentRequestElevatorOne = elevators.get(0).getCurrentRequest();
	    FloorData currentRequestElevatorTwo = elevators.get(1).getCurrentRequest();
	    FloorData currentRequestElevatorThree = elevators.get(2).getCurrentRequest();
	    FloorData currentRequestElevatorFour = elevators.get(3).getCurrentRequest();
	    
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
	    
	    // print the received datagram from the elevator
	    System.out.println("\nScheduler: Acknowledgement Packet received from Elevator:");
	    System.out.println("Containing: " + new String(receivePacket.getData(),0,receivePacket.getLength()));
	    
	    // Once acknowledgement packet is received, we know the elevator is stationary and 
	    // is not processing any requests
	    int elevatorIndex = portNumber == 5000 ? 0 : portNumber == 6000 ? 1 : portNumber == 7000 ? 2 : 3;
	    elevators.get(elevatorIndex).setState(0);
	    
	    System.out.println("\nElevator " + (portNumber == 5000 ? 1 : portNumber == 6000 ? 2 : portNumber == 7000 ? 3 : 4) + " is Stationary\n");
	    
	    elevatorsAvailable = true;
	    
	    // instantiate the Ack from the elevator
	    elevatorAck = new String(receivePacket.getData(),0,receivePacket.getLength());
	    if(portNumber == 5000) {
		    elevatorAck += " successfully processed request: initial floor: " + currentRequestElevatorOne.getInitialFloor() + " and destination floor: " + currentRequestElevatorOne.getDestinationFloor();
		    elevatorAck += " \nas it arrived at floor " + currentRequestElevatorOne.getDestinationFloor();
	    } else if (portNumber == 6000) {
		    elevatorAck += " successfully processed request: initial floor: " + currentRequestElevatorTwo.getInitialFloor() + " and destination floor: " + currentRequestElevatorTwo.getDestinationFloor();
		    elevatorAck += " \nas it arrived at floor " + currentRequestElevatorTwo.getDestinationFloor();
	    } else if (portNumber == 7000) {
		    elevatorAck += " successfully processed request: initial floor: " + currentRequestElevatorThree.getInitialFloor() + " and destination floor: " + currentRequestElevatorThree.getDestinationFloor();
		    elevatorAck += " \nas it arrived at floor " + currentRequestElevatorThree.getDestinationFloor();
	    } else if (portNumber == 8000) {
		    elevatorAck += " successfully processed request: initial floor: " + currentRequestElevatorFour.getInitialFloor() + " and destination floor: " + currentRequestElevatorFour.getDestinationFloor();
		    elevatorAck += " \nas it arrived at floor " + currentRequestElevatorFour.getDestinationFloor();
	    }
	    
 	  
		// form acknowledgement packet to send to floor
    	DatagramPacket sendAckPacketFloor = null;
		try {
			sendAckPacketFloor = new DatagramPacket(elevatorAck.getBytes(), elevatorAck.length(), InetAddress.getLocalHost(), 1500);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
      
	    // print the acknowledgement packet sent to floor
	    System.out.println("Scheduler: Acknowledgement Packet sent to Floor since the elevator has processed the request");
	    System.out.println("Containing: " + new String(sendAckPacketFloor.getData(),0,sendAckPacketFloor.getLength()));
	            
	    try {
	    	floorSendSocket.send(sendAckPacketFloor);
	    } catch (IOException e) {
	       e.printStackTrace();
	       System.exit(1);
	    }	    
	 }			      
			      		
	/**
	 * Send and receive DatagramPackets to/from elevator. 
	 * 
	 * Checks which elevator to send the request to by calling the checkServiceableRequest() 
	 * method. Then, once checkServiceableRequest() returns the port number of the elevator
	 * the request is assigned to, then this method will send the request to that 
	 * specific elevator with the help of the handleElevatorRequest() method.
	 * 
	 * If no serviceable elevators are currently available, this method 
	 * keeps re-checking every 1 second for an available elevator.
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
	     byte elevatorThreeReply[] = new byte[100];
	     byte elevatorFourReply[] = new byte[100];
	     int portNumber = 0;
	   
	     //form packets to send to elevator
	     //call method to decide which elevator
	     String elevatorOneRequests = "";
	     String elevatorTwoRequests = "";
	     String elevatorThreeRequests = "";
	     String elevatorFourRequests = "";

		 while (true) { 
			 portNumber = checkServiceableRequest(allFloorRequests.element());
		     if (elevatorsAvailable) {
		          if (portNumber == 5000) {
		        	  // elevator one
		        	   handleElevatorRequest(5000, elevatorOneRequests, elevatorOneSocket, sendPacketElevator1, receivePacketElevatorOne, elevatorOneReply);		      
		               break;
		           } else if (portNumber == 6000) {
		        	   // elevator two
		        	   handleElevatorRequest(6000, elevatorTwoRequests, elevatorTwoSocket, sendPacketElevator2, receivePacketElevatorTwo, elevatorTwoReply);     
					   break;
		           } else if (portNumber == 7000) {
		        	   // elevator three
		        	   handleElevatorRequest(7000, elevatorThreeRequests, elevatorThreeSocket, sendPacketElevator3, receivePacketElevatorThree, elevatorThreeReply);     
					   break;
		           } else if (portNumber == 8000) {
		        	   // elevator four
		        	   handleElevatorRequest(8000, elevatorFourRequests, elevatorFourSocket, sendPacketElevator4, receivePacketElevatorFour, elevatorFourReply);     
					   break;
		           }	
		     } else {
		    	   // Elevators are unavailable since all of them are moving
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
	
	public static void main(String args[])
	   { 
			Scheduler s = new Scheduler();
		    Thread floorThread = new Thread(s::sendReceiveFloor);		    
		    
		    floorThread.start();
		    s.receiveElevatorStatus(4000);	    
	   }
	
}
