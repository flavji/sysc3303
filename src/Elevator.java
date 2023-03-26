import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
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
	
	Collection<Integer> initialFloors;
	Collection<Integer> destinationFloors;
	private Integer currentFloor;
	private int state;
	private int portNumber;
	private int timeBetweenFloors;
	private int timeHandleDoors;
	private boolean doorFault;
	
	DatagramPacket sendPacket, receivePacket;
	DatagramSocket socketScheduler, socketFloor;
	
	
	/**
	 * Constructor for Elevator.
	 * 
	 * @param s	A Scheduler object that is used to communicate between the two clients (i.e., floor and elevator).
	 */
	public Elevator(int portNumber) {
		this.currentFloor = 2; // assume elevator starts at floor 2 
		this.initialFloors = Collections.synchronizedCollection(new LinkedList<>());
		this.destinationFloors = Collections.synchronizedCollection(new LinkedList<>());
		this.timeBetweenFloors = 8000;		// default time for moving between floors is 8 seconds
		this.timeHandleDoors = 2500;		// default time for doors to open/close is 2.5 seconds
		
		this.state = 0; 
		// 0 (stationary), 1 (moving up), 2 (moving down), 3 (doors opening), 4 (doors closing), 5 (floor fault), 6 (door fault), 7 (out of service)
		this.portNumber = portNumber;
		this.doorFault = false;
		
	    try {
	         socketScheduler = new DatagramSocket(portNumber);
	    } catch (SocketException se) {
	         se.printStackTrace();
	         System.exit(1);
	    }						
	}
	
    public void setTimeBetweenFloors(int time) {
        this.timeBetweenFloors = time;
    }

    public int getTimeBetweenFloors() {
        return this.timeBetweenFloors;
    }
    
    public void setTimeHandleDoors(int time) {
        this.timeHandleDoors = time;
    }

    public int getTimeHandleDoors() {
        return this.timeHandleDoors;
    }
    
	public int getPortNumber() {
		return this.portNumber;
	}	
	
	public int getState() {
		return this.state;
	}
	
	public void setState(int state) {
		this.state = state;
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
	 * Returns the current floor of the elevator.
	 * 
	 * @return current floor of the elevator
	 */
	public void setCurrentFloor(int currentFloor) {
		this.currentFloor = currentFloor;
	}
	
	/**
	 * Returns the size of the elevators queue.
	 * 
	 * @return An integer value with the elevator's queue size.
	 */
	public int getQueueSize() { return destinationFloors.size(); }
	
	/**
	 * Adds destination floors to queue based on most efficient position.
	 * 
	 * @param destinationFloor The destination floor of the given request.
	 */
	public void addDestination(int destinationFloor, int position) {
		destinationFloors.add(destinationFloor);// need to implement the position as well
	}
	
	/**
	 * Adds initial floors to queue 
	 * @param initialFloor		the initial Floor of the given request
	 * @param position
	 */
	public void addInitial(int initialFloor, int position) {
		initialFloors.add(initialFloor);// need to implement the position as well
	}
	
	/**
	 * Executes the request received from the scheduler
	 * @param initialFloors
	 * @param destinationFloors
	 */
	private void executeRequest(Collection<Integer> initialFloors, Collection<Integer> destinationFloors) {
	    Iterator<Integer> initialIterator = initialFloors.iterator();
	    Iterator<Integer> destinationIterator = destinationFloors.iterator();
	    
	    while (initialIterator.hasNext() && destinationIterator.hasNext()) {
	        Integer initialFloor = initialIterator.next();
	        Integer destinationFloor = destinationIterator.next();

	        // check if the elevator is already on the initial floor
	        if (currentFloor != initialFloor) {
        		// move the elevator to the initial floor and open its doors to let passengers in
	            if(!moveElevator(initialFloor)) {
	            	// If there is a permanent door fault (even after retrying), set the elevator state to "out of service"
	                this.state = 7;
	                System.out.println("\nThe elevator is out of service due to a floor fault. Shutting down at the next floor.");
	                // Interrupt the elevator thread
	                Thread.currentThread().interrupt();
	            }
	            
	            System.out.println("\n" + Thread.currentThread().getName() + " doors opening to let people in at floor " + initialFloor);
	            if(!handleDoors()) {
	                // If there is a permanent door fault (even after retrying), set the elevator state to "out of service"
	                this.state = 7;
	                System.out.println("\nThe elevator is out of service due to a permanent door fault. Stopping at initial floor.");
	                // Interrupt the elevator thread
	                Thread.currentThread().interrupt();
	            }	            
	        }

	        // move the elevator to the destination floor and open its doors to let passengers out
            if(!moveElevator(destinationFloor)) {
            	// If there is a permanent door fault (even after retrying), set the elevator state to "out of service"
                this.state = 7;
                System.out.println("\nThe elevator is out of service due to a floor fault. Shutting down at the next floor.");
                // Interrupt the elevator thread
                Thread.currentThread().interrupt();
            }
            System.out.println("\n" + Thread.currentThread().getName() + " doors opening to let people out at floor " + currentFloor);
            if(!handleDoors()) {
                // If there is a permanent door fault (even after retrying), set the elevator state to "out of service"
                this.state = 7;
                System.out.println("\nThe elevator is out of service due to a permanent door fault. Stopping at destination floor.");
                // Interrupt the elevator thread
                Thread.currentThread().interrupt();
            }

	        
	        // remove the initial and destination floors from their respective queues
	        initialIterator.remove();
	        destinationIterator.remove();
	    }
	}

	/**
	 * 
	 * @param destinationFloor
	 * @return
	 */
	public boolean moveElevator(Integer destinationFloor) {
	    int direction = destinationFloor.compareTo(currentFloor);
	    long floorTimeout = 20000; 	// 20 seconds timeout for the elevator to move from one floor to another
	    
	    while (currentFloor != destinationFloor) {
	        // update the current floor based on the direction of the elevator
	        if (direction > 0) {
	            this.state = 1;
	            currentFloor++;
	        } else if (direction < 0) {
	            this.state = 2;
	            currentFloor--;
	        }

	        // start the floor timer after the elevator starts moving
	        long floorTimerStart = System.currentTimeMillis();

	        System.out.println(Thread.currentThread().getName() + " moving " + (direction > 0 ? "up" : "down") + " to floor " + currentFloor);

	        try {
	            Thread.sleep(timeBetweenFloors); // simulate 8 seconds to move one floor up/down
	        } catch (InterruptedException e) {
	            System.out.println(Thread.currentThread().getName() + " thread was interrupted due to a fault.");
	        }

	        // check for a floor fault if the elevator takes too long to reach the floor
	        if (System.currentTimeMillis() - floorTimerStart >= floorTimeout) {
	            System.out.println("\n" + Thread.currentThread().getName() + " has encountered a fault - elevator is stuck between floors!");
	            this.state = 5;
	            // shut down the corresponding elevator if a floor fault occurs
	            System.out.println("\n" + Thread.currentThread().getName() + " is shutting down due to the floor fault.");
	            return false;
	        }
	    }
	    
	    System.out.println(Thread.currentThread().getName() + " arrived at floor: " + destinationFloor);
	    return true;
	}

	/**
	 * 
	 * @return
	 */
	public boolean handleDoors() {
	    // simulate doors opening and closing	    
	    long doorTimerStart = System.currentTimeMillis();   // start door timer
	    long doorTimeout = 10000; 	// 10 seconds timeout for the doors to close
	    
	    try {
	        this.state = 3;
	        Thread.sleep(timeHandleDoors); // simulate 2.5 seconds to open doors
	    } catch (InterruptedException e) {
	        System.out.println("\n" + Thread.currentThread().getName() + " thread was interrupted due to a fault.");
	    }

	    // check for a door fault if the doors do not close after opening
	    if (System.currentTimeMillis() - doorTimerStart >= doorTimeout) {
	        System.out.println("\n" + Thread.currentThread().getName() + " has encountered a door fault - doors are stuck open!");
	        this.state = 6;
	        // set the door fault flag to true and continue executing the rest of the elevator's tasks
	        doorFault = true;
	        
	        System.out.println("\nRetrying... please wait.");
	        
	        int retryClosingDoors = 0;
	        
	        // Handling the situation gracefully. 
	        // try closing the doors a few times. If unsuccessful, stop the elevator.
	        while (retryClosingDoors < 4 && doorFault) {
	        	System.out.println("\n" + Thread.currentThread().getName() + " retrying to close doors (attempt " + (retryClosingDoors + 1) + ")");
	        	try {
	        		Thread.sleep(5000);
	        	} catch (InterruptedException e) {
	        	    System.out.println("\n" + Thread.currentThread().getName() + " thread was interrupted due to a fault.");
	        	}
	        	
	        	long doorRetryTimerStart = System.currentTimeMillis();   // start door timer
	        	
	    	    try {
	    	        this.state = 3;
	    	        Thread.sleep(timeHandleDoors); // simulate 2.5 seconds to open doors
	    	    } catch (InterruptedException e) {
	    	        System.out.println("\n" + Thread.currentThread().getName() + " thread was interrupted due to a fault.");
	    	    }
	    	    
	            if (System.currentTimeMillis() - doorRetryTimerStart >= doorTimeout) {
	                // if the doors still don't close after all retries, declaring a permanent fault
	                if (retryClosingDoors == 3) {
		                this.state = 7;    // set the elevator state to "out of service"		        
		            	System.out.println("\n" + Thread.currentThread().getName() + " has encountered a permanent fault - doors are stuck open!");
		                // notify the user of the permanent fault
		                System.out.println("Door fault not resolved - elevator out of service.");
		                return false; // stop executing the rest of the elevator's tasks
	                }
	            } else {
		            doorFault = false;
	            }
	            retryClosingDoors++;
	        }
	    }  
	    
        System.out.println("\n" + Thread.currentThread().getName() + " doors closing");
        try {
            this.state = 4;
            Thread.sleep(timeHandleDoors); // simulate 2.5 seconds to close doors
        } catch (InterruptedException e) {
            System.out.println("\n" + Thread.currentThread().getName() + " thread was interrupted due to a fault.");
        }
        
        return true;
	    
	}
	
	/**
	 * Send and receive DatagramPackets to/from scheduler 
	 */
	public synchronized void sendReceive() {
		while(true) {
			byte schedulerData[] = new byte[1000];
			String ack = "Request processed: " + Thread.currentThread().getName();

			
			//form packet to receive data from scheduler
			receivePacket = new DatagramPacket(schedulerData, schedulerData.length);
			
			
			//receive Packet from scheduler
			try {        
				System.out.println(Thread.currentThread().getName() + " waiting for request...");
		         socketScheduler.receive(receivePacket);
		        
		      } catch (IOException e) {
		         System.out.print("IO Exception: likely:");
		         System.out.println("Receive Socket Timed Out.\n" + e);
		         e.printStackTrace();
		         System.exit(1);
		      }
			
			
			//print the received datagram from the scheduler
		    System.out.println("\n" + Thread.currentThread().getName() + " received request from Scheduler");
		    System.out.println("Containing: " + new String(receivePacket.getData(),0,receivePacket.getLength()));
			
			//process the request, call the internal method
		     String data = new String(receivePacket.getData(),0,receivePacket.getLength());
		     String[] arrValues = data.split("/");
		     for(int i = 0; i < arrValues.length; i++) {
		    	 String[] arrValues2 = arrValues[i].split(",");
		    	 
		    	//add to the queue, need to change this if condition
                 if(arrValues2[0] != null && arrValues2.length == 4 ) {
                	 addInitial(Integer.parseInt(arrValues2[1]), getCurrentFloor());
                     addDestination(Integer.parseInt(arrValues2[3]), getCurrentFloor());
                 }
		    	 System.out.println(Thread.currentThread().getName() + " processing request: Initial floor = " + arrValues2[1].toString() + " and Destination Floor = " + arrValues2[3].toString());
		     }
		     
		     executeRequest(initialFloors, destinationFloors);
		     
		 
		      try {
		    	  if(Thread.currentThread().getName().equals("Elevator One")) {
		    		  sendPacket = new DatagramPacket(ack.getBytes(), ack.length(), InetAddress.getLocalHost(), 2000);
		    	  } else if(Thread.currentThread().getName().equals("Elevator Two")) {
		    		  sendPacket = new DatagramPacket(ack.getBytes(), ack.length(), InetAddress.getLocalHost(), 2500);
		    	  }
		      } catch(UnknownHostException e) {
			         e.printStackTrace();
			         System.exit(1);
			  }
		      
		      //send the Ack Packet to the scheduler
		      try {
		    	  socketScheduler.send(sendPacket);
		    	  System.out.println(Thread.currentThread().getName() + " is done processing this request: Acknowledgement sent to Scheduler");
		    	  System.out.println("Containing: " + new String(sendPacket.getData(),0,sendPacket.getLength()));
		      } catch (IOException e) {
		         e.printStackTrace();
		         System.exit(1);
		      }    			
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
			Elevator e = new Elevator(5000);		
		    Thread t1 = new Thread(e, "Elevator One");	    	    
		    t1.start();
		    Elevator e2 = new Elevator(6000);
		    Thread t2 = new Thread(e2, "Elevator Two");
		    t2.start();    
	   }
}
        		

         	
            
    
	

