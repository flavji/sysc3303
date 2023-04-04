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
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

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
	
	private Integer currentFloor;
	private int state;
	private int portNumber;
	private int timeBetweenFloors;
	private int timeHandleDoors;
	private boolean doorFault;
	private FloorData currentRequest;
	private final Object lock = new Object();
	private final String name;
	private ConcurrentLinkedQueue<Pair<Integer, Integer>> floorsQueue;
	
	DatagramPacket sendPacket, receivePacket;
	DatagramSocket socketScheduler, socketFloor;
	
	
	/**
	 * Constructor for Elevator.
	 * 
	 * @param s	A Scheduler object that is used to communicate between the two clients (i.e., floor and elevator).
	 */
	public Elevator(int portNumber, String name) {
		this.currentFloor = 2; // assume elevator starts at floor 2 
		this.timeBetweenFloors = 8000;		// default time for moving between floors is 8 seconds
		this.timeHandleDoors = 2500;		// default time for doors to open/close is 2.5 seconds
		
		this.floorsQueue = new ConcurrentLinkedQueue<>();
		this.name = name;
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
	
	/**
	 * Testing purposes - tests floor fault
	 * Sets the time it takes to go from one floor to another
	 * @param time	an int, the time it takes to go from one floor to another
	 */
    public void setTimeBetweenFloors(int time) {
        this.timeBetweenFloors = time;
    }

    /**
     * Testing purposes
     * Gets the time it takes to go from one floor to another
     * @return	an int, the time it takes to go from one floor to another
     */
    public int getTimeBetweenFloors() {
        return this.timeBetweenFloors;
    }
    
    /**
     * Testing purposes - tests door fault
     * Sets the time it takes to open/close doors
     * @param time	an int, the time it takes to open/close doors
     */
    public void setTimeHandleDoors(int time) {
        this.timeHandleDoors = time;
    }

    /**
     * Gets the time it takes to open/close doors
     * @return	an int, the time it takes to open/close doors
     */
    public int getTimeHandleDoors() {
        return this.timeHandleDoors;
    }
    
    /**
     * Gets the port number of the current elevator
     * @return	an int, the port number of the current elevator
     */
	public int getPortNumber() {
		return this.portNumber;
	}	
	
	/**
	 * Gets the state of the current elevator
	 * @return	an int, the state of the current elevator
	 */
	public int getState() {
		return this.state;
	}
	
	/**
	 * Sets the state of the current elevator
	 * Scheduler uses this method to set the appropriate state for the elevator, 
	 * so it can send the right request to the correct elevator (Elevator 1 or Elevator 2)
	 * @param state
	 */
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
	
	public void setCurrentRequest(FloorData request) {
		this.currentRequest = request;
	}
	
	public FloorData getCurrentRequest() {
		return currentRequest;
	}
	
	/**
	 * Returns the current floor of the elevator.
	 * 
	 * @return current floor of the elevator
	 */
	public void setCurrentFloor(int currentFloor) {
		this.currentFloor = currentFloor;
	}
	
	
    public void startFloorSenderThread(int port) {
    	System.out.println("ELEVATOR RUNNING: " + Thread.currentThread().getName());
    	String currentThread = Thread.currentThread().getName();
        Runnable floorSender = () -> {
        	System.out.println("SENDING FLOOR");
            DatagramSocket socket = null;
            try {
                socket = new DatagramSocket();
                while (true) {
                	byte[] buffer = (currentThread + ": " + String.valueOf(currentFloor) + ": " + String.valueOf(state)).getBytes();
                    InetAddress address = InetAddress.getLocalHost();
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
                    socket.send(packet);
                    Thread.sleep(1000); // Wait for 1 second before sending the next update
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    socket.close();
                }
            }
        };
        new Thread(floorSender).start();
    }
    
    private void executeRequest() {
        Iterator<Pair<Integer, Integer>> floorIterator = floorsQueue.iterator();

        while (floorIterator.hasNext()) {
            Pair<Integer, Integer> floorPair = floorIterator.next();
            Integer initialFloor = floorPair.getFirst();
            Integer destinationFloor = floorPair.getSecond();
            floorIterator.remove();

            // check if the elevator is already on the initial floor
            if (currentFloor != initialFloor) {
                // move the elevator to the initial floor and open its doors to let passengers in
                System.out.println("\n" + Thread.currentThread().getName() + " needs to go to floor " + initialFloor + " to pick up passengers.");
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

            System.out.println("\n" + Thread.currentThread().getName() + " needs to go to floor " + destinationFloor + " to drop off passengers.");
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
            
            this.state = 0;

            System.out.print("Floors queue: ");
            for (Pair<Integer, Integer> floor : floorsQueue) {
                System.out.print("(" + floor.getFirst() + ", " + floor.getSecond() + ") ");
            }
            
        }
    }

//	/**
//	 * Executes the request received from the scheduler
//	 * @param initialFloors		a queue of the initial floors the elevator needs to go to, so it can pick up passengers
//	 * @param destinationFloors		a queue of the destination floors the elevator needs to go to, so it can drop off passengers
//	 */
//	private void executeRequest() {
//	    Iterator<Integer> initialIterator = initialFloors.iterator();
//	    Iterator<Integer> destinationIterator = destinationFloors.iterator();
//	    
//	    while (initialIterator.hasNext() && destinationIterator.hasNext()) {
//	        Integer initialFloor = initialIterator.next();
//	        Integer destinationFloor = destinationIterator.next();
//
//	        // check if the elevator is already on the initial floor
//	        if (currentFloor != initialFloor) {
//        		// move the elevator to the initial floor and open its doors to let passengers in
//	        	System.out.println("\n" + Thread.currentThread().getName() + " needs to go to floor " + initialFloor + " to pick up passengers.");
//	            if(!moveElevator(initialFloor)) {
//	            	// If there is a permanent door fault (even after retrying), set the elevator state to "out of service"
//	                this.state = 7;
//	                System.out.println("\nThe elevator is out of service due to a floor fault. Shutting down at the next floor.");
//	                // Interrupt the elevator thread
//	                Thread.currentThread().interrupt();
//	            }
//	            
//	            System.out.println("\n" + Thread.currentThread().getName() + " doors opening to let people in at floor " + initialFloor);
//	            if(!handleDoors()) {
//	                // If there is a permanent door fault (even after retrying), set the elevator state to "out of service"
//	                this.state = 7;
//	                System.out.println("\nThe elevator is out of service due to a permanent door fault. Stopping at initial floor.");
//	                // Interrupt the elevator thread
//	                Thread.currentThread().interrupt();
//	            }	            
//	        }
//	        initialIterator.remove();
//
//	        System.out.println("\n" + Thread.currentThread().getName() + " needs to go to floor " + destinationFloor + " to drop off passengers.");
//	        // move the elevator to the destination floor and open its doors to let passengers out
//            if(!moveElevator(destinationFloor)) {
//            	// If there is a permanent door fault (even after retrying), set the elevator state to "out of service"
//                this.state = 7;
//                System.out.println("\nThe elevator is out of service due to a floor fault. Shutting down at the next floor.");
//                // Interrupt the elevator thread
//                Thread.currentThread().interrupt();
//            }
//            System.out.println("\n" + Thread.currentThread().getName() + " doors opening to let people out at floor " + currentFloor);
//            if(!handleDoors()) {
//                // If there is a permanent door fault (even after retrying), set the elevator state to "out of service"
//                this.state = 7;
//                System.out.println("\nThe elevator is out of service due to a permanent door fault. Stopping at destination floor.");
//                // Interrupt the elevator thread
//                Thread.currentThread().interrupt();
//            }
//            
//            
//            this.state = 0;
//	        
//	
//            System.out.print("Initial floors: ");
//            for (Integer floor : initialFloors) {
//                System.out.print(floor + " ");
//            }
//
//            // Print out the contents of the destinationFloors queue
//            System.out.print("\nDestination floors: ");
//            for (Integer floor : destinationFloors) {
//                System.out.print(floor + " ");
//            }
//
//            destinationIterator.remove();
//	    }
//	}

	/**
	 * Move the elevator to the specified floor
	 * @param destinationFloor		an Integer, the floor the elevator needs to go to from its current floor
	 * @return	a boolean, true if elevator successfully moves from the current floor to the destination floor, false otherwise
	 * This method returns false if a floor fault occurs, true otherwise
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
	        
	        Pair<Integer, Integer> firstRequest = floorsQueue.peek();

	        // Check if there are any pending initial floors and process them before continuing to the current destination floor.
	        if (!floorsQueue.isEmpty() && firstRequest.getFirst() == currentFloor && firstRequest.getFirst() != destinationFloor) {
	            floorsQueue.poll();
	        	Integer nextInitialFloor = firstRequest.getFirst();	            
	            
	            System.out.println(Thread.currentThread().getName() + " stopping at initial floor " + nextInitialFloor);
	            System.out.println("\n" + Thread.currentThread().getName() + " doors opening to let people in at floor " + nextInitialFloor);
	            if(!handleDoors()) {
	            	System.out.println(Thread.currentThread().getName() + " stopping at floor " + nextInitialFloor + " to pick up passengers.");
	                // If there is a permanent door fault (even after retrying), set the elevator state to "out of service"
	                this.state = 7;
	                System.out.println("\nThe elevator is out of service due to a permanent door fault. Stopping at destination floor.");
	                // Interrupt the elevator thread
	                Thread.currentThread().interrupt();
	            }
	        }
	    }
	    
	    System.out.println(Thread.currentThread().getName() + " arrived at floor " + destinationFloor);
	    return true;
	}

	/**
	 * Opens and closes doors of the elevator 
	 * @return 	a boolean, true if the doors close successfully, false if a door fault occurs
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
	

    public void sendReceive() {
        Thread receivingThread = new Thread(() -> {
            while (true) {
                byte[] schedulerData = new byte[1000];
                DatagramPacket receivePacket = new DatagramPacket(schedulerData, schedulerData.length);
                try {
                    System.out.println(name + " waiting for request...");
                    socketScheduler.receive(receivePacket);

                    // print the received datagram from the scheduler
                    System.out.println("\n" + name + " received request from Scheduler");
                    System.out.println("Containing: " + new String(receivePacket.getData(), 0, receivePacket.getLength()));

                    // process the request, call the internal method
                    String data = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    String[] arrValues = data.split("/");
                    for (String arrValue : arrValues) {
                        String[] arrValues2 = arrValue.split(",");

                        // add to the queue, need to change this if condition
                        if (arrValues2[0] != null && arrValues2.length == 4) {
                            floorsQueue.add(new Pair<>(Integer.parseInt(arrValues2[1]), Integer.parseInt(arrValues2[3])));
                        }
                        System.out.println(name + " processing request: Initial floor = " + arrValues2[1] + " and Destination Floor = " + arrValues2[3]);
                    }
                    
//                    System.out.print("Initial floors here: ");
//                    for (Integer floor : initialFloors) {
//                        System.out.print(floor + " ");
//                    }
//
//                    // Print out the contents of the destinationFloors queue
//                    System.out.print("\nDestination floors here: ");
//                    for (Integer floor : destinationFloors) {
//                        System.out.print(floor + " ");
//                    }

                    synchronized (lock) {
                        lock.notify(); // notify the elevator thread to execute the request
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        receivingThread.start();

        while (true) {
            synchronized (lock) {
                while (floorsQueue.isEmpty()) {
                    try {
                        lock.wait(); // wait for a request to be received
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                String ack = "Request processed: " + Thread.currentThread().getName();
                executeRequest();
                
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
    }
	
	/**
	 * Send and receive DatagramPackets to/from scheduler 
	 */
//	public synchronized void sendReceive() {
//		startFloorSenderThread(4000);
//		while(true) {
//			String ack = "Request processed: " + Thread.currentThread().getName();
//		     
//		     executeRequest(initialFloors, destinationFloors);
//		     
//		 
//    			
//		}		
//	}
	
	/**
	 * Used to run the Elevator threads.
	 */
	@Override
    public void run() { 
		sendReceive();
	}
	
	public static void main(String args[])
	   {
			Elevator e = new Elevator(5000, "Elevator One");		
		    Thread t1 = new Thread(e, "Elevator One");	    	    
		    t1.start();
		    Elevator e2 = new Elevator(6000, "Elevator Two");
		    Thread t2 = new Thread(e2, "Elevator Two");
		    t2.start();    
	   }
}
        		

         	
            
    
	

