import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.PriorityQueue;
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
	private PriorityQueue<DestinationFloor> destinationFloors; 
	public ElevatorGUI gui;
	
	DatagramPacket sendPacket, receivePacket;
	DatagramSocket socketScheduler, socketFloor;
	
	
	/**
	 * Constructor for Elevator.
	 * 
	 * @param s	A Scheduler object that is used to communicate between the two clients (i.e., floor and elevator).
	 */
	public Elevator(int portNumber, String name) {
	
		
		this.floorsQueue = new ConcurrentLinkedQueue<>();
		this.name = name;
		this.state = 0; 
		this.currentFloor = 2;
		// 0 (stationary), 1 (moving up), 2 (moving down), 3 (doors opening), 4 (doors closing), 5 (floor fault), 6 (door fault), 7 (out of service)
		this.portNumber = portNumber;
		this.doorFault = false;
		
		gui = new ElevatorGUI(name);
	
		
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
    	String currentThread = Thread.currentThread().getName();
        Runnable floorSender = () -> {
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
    
    private boolean executeRequest() {

    	boolean floorFault = false;
    	boolean doorFault = false;
        Pair<Integer, Integer> floorPair = floorsQueue.peek();
        Integer initialFloor = floorPair.getFirst();
        Integer destinationFloor = floorPair.getSecond();
        floorsQueue.remove();

        if(!floorPair.isExecuted()) {
        	// check if the elevator is already on the initial floor
            if (currentFloor != initialFloor && !floorPair.passengersPickedUp()) {
                // move the elevator to the initial floor and open its doors to let passengers in
                System.out.println("\n" + Thread.currentThread().getName() + " needs to go to floor " + initialFloor + " to pick up passengers." );
                gui.updateElevatorStatus(state);
                if(!moveElevator(initialFloor)) {
                	floorFault = true;
                	gui.updateElevatorStatus(5);
                	
                	try {
                		Thread.sleep(3000);
                	}catch(Exception e) {
                		System.out.println(Thread.currentThread().getName() + "'s movement was interrupted due to a floor fault");
                	}
                	
                    // If there is a permanent floor fault, set the elevator state to "out of service"
                	if(!doorFault) {
                		System.out.println("\nThe elevator is out of service due to a floor fault. Shutting down at the next floor.");
                		 gui.updateElevatorStatus(7);
                	}
                   
                    // Interrupt the elevator thread
                    Thread.currentThread().interrupt();
                    return false;
                   }
                } else {
	            	System.out.println("\n" + Thread.currentThread().getName() + " doors opening to let people in at floor " + initialFloor);  
	                if(!handleDoors()) {
	                	doorFault = true;
	                	gui.updateElevatorStatus(6);
	                    // If there is a permanent door fault (even after retrying), set the elevator state to "out of service"
	                    System.out.println("\nThe elevator is out of service due to a permanent door fault. Stopping at initial floor.");
	                    gui.updateElevatorStatus(7);
	                    // Interrupt the elevator thread
	                    Thread.currentThread().interrupt();
	                    return false;
	             }    
            }

            if(!floorFault && !doorFault) {
            	System.out.println("\n" + Thread.currentThread().getName() + " needs to go to floor " + destinationFloor + " to drop off passengers.");
            }
            // move the elevator to the destination floor and open its doors to let passengers out
            if(!moveElevator(destinationFloor)) {
            	floorFault = true;
            	gui.updateElevatorStatus(5);
            	
            	try {
            		Thread.sleep(3000);
            	}catch(Exception e) {
            		System.out.println(Thread.currentThread().getName() + "'s movement was interrupted due to a floor fault");
            	}
            	
                // If there is a permanent door fault (even after retrying), set the elevator state to "out of service"
            	if(!doorFault) {
            		System.out.println("\nThe elevator is out of service due to a floor fault. Shutting down at the next floor.");
            		gui.updateElevatorStatus(7);
            	}
                
                
                // Interrupt the elevator thread
                Thread.currentThread().interrupt();
                return false;
            }
            
            if(!floorFault && !doorFault) {
                this.state = 0;

                //gui.updateElevatorStatus(state);
            }
            

//            System.out.print("Floors queue: ");
//            for (Pair<Integer, Integer> floor : floorsQueue) {
//                System.out.print("(" + floor.getFirst() + ", " + floor.getSecond() + ") ");
//            }
        }
              return true;      
    }

	/**
	 * Move the elevator to the specified floor
	 * @param destinationFloor		an Integer, the floor the elevator needs to go to from its current floor
	 * @return	a boolean, true if elevator successfully moves from the current floor to the destination floor, false otherwise
	 * This method returns false if a floor fault occurs, true otherwise
	 */
	public boolean moveElevator(Integer destinationFloor) {
	    int direction = destinationFloor.compareTo(currentFloor);
	    long floorTimeout = 20000; 	// 20 seconds timeout for the elevator to move from one floor to another
        if (direction > 0) {
        	destinationFloors = new PriorityQueue<>(Comparator.comparingInt(DestinationFloor::getFloorNumber));
        } else if (direction < 0) {
            destinationFloors = new PriorityQueue<>(Comparator.comparingInt(DestinationFloor::getFloorNumber).reversed());
        }
        destinationFloors.add(new DestinationFloor(destinationFloor, false));
        
        while (!destinationFloors.isEmpty()) {
        	DestinationFloor currentDestinationFloorElement = destinationFloors.element();
        	int currentDestinationFloor = currentDestinationFloorElement.getFloorNumber();
//        	System.out.println(Thread.currentThread().getName() + " DESTINATION FLOORS: " + currentDestinationFloorElement.getFloorNumber());
//        	System.out.println(Thread.currentThread().getName() + " DESTINATION FLOORS 2: " + currentDestinationFloor);
//           	System.out.println(Thread.currentThread().getName() + " DESTINATION FLOORS 3: " + currentFloor);
//           	
//           	System.out.println(Thread.currentThread().getName() + " DESTINATION FLOORS 3: " + destinationFloors.toString());


        	while (currentFloor != currentDestinationFloor) {
                // check for interrupt flag and exit early if set
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println(Thread.currentThread().getName() + " was interrupted during elevator movement.");
                    return false;
                }
    	        // update the current floor based on the direction of the elevator
    	        if (direction > 0) {
    	            this.state = 1;
    	            currentFloor++;
    	        } else if (direction < 0) {
    	            this.state = 2;
    	            currentFloor--;
    	        }
    	        
    	        //update GUI
    	        gui.updateElevatorFloor(currentFloor);
    	        gui.updateElevatorStatus(state);
    	        
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
    	            gui.updateElevatorStatus(state);
    	            
    	            // shut down the corresponding elevator if a floor fault occurs
    	            System.out.println("\n" + Thread.currentThread().getName() + " is shutting down due to the floor fault.");
    	            gui.updateElevatorStatus(7);
    	            return false;
    	        }
    	        
    	        Iterator<Pair<Integer, Integer>> iterator = floorsQueue.iterator();
//    	        Pair<Integer, Integer> firstRequest = floorsQueue.peek();

    	        
    	        while (iterator.hasNext()) {
    	            Pair<Integer, Integer> request = iterator.next();

    	            // Check if the elevator is moving up and the request's initial floor is above the current floor,
    	            // or if the elevator is moving down and the request's initial floor is below the current floor.
    	            boolean isRequestInRange = (direction > 0 && request.getFirst() >= currentFloor && request.getFirst() <= destinationFloor && !request.isExecuted())
    	                    || (direction < 0 && request.getFirst() <= currentFloor && request.getFirst() >= destinationFloor);

    	            if (isRequestInRange) {
    	                request.setPassengersInElevator(true);

    	                if (request.getSecond() == destinationFloor || request.getSecond() < destinationFloor) {
    	                    request.setExecuted(true);
    	                    destinationFloors.add(new DestinationFloor(request.getFirst(), true));
    	                    destinationFloors.add(new DestinationFloor(request.getSecond(), false));
    	                    currentDestinationFloor = request.getFirst();
    	                    currentDestinationFloorElement = new DestinationFloor(request.getSecond(), true);
    	                }
    	            }
    	        }

    	        
    	        
        }
            System.out.println(Thread.currentThread().getName() + " arrived at floor " + currentDestinationFloor);
            
            if(currentDestinationFloorElement.isInitialFloor()) {
	            System.out.println("\n" + Thread.currentThread().getName() + " doors opening to let people in at floor " + currentDestinationFloor);
	            gui.updateElevatorStatus(3);
            } else {
            	System.out.println("\n" + Thread.currentThread().getName() + " doors opening to let people out at floor " + currentDestinationFloor);              
            	gui.updateElevatorStatus(3);
            }
            if(!handleDoors()) {
                System.out.println("\nThe elevator is out of service due to a permanent door fault. Stopping at destination floor.");
                
                gui.updateElevatorStatus(7);
                // Interrupt the elevator thread
                Thread.currentThread().interrupt();
                return false;
            }
        	destinationFloors.poll();
	    
	 }
	    

	    return true;
	}

	/**
	 * Opens and closes doors of the elevator 
	 * @return 	a boolean, true if the doors close successfully, false if a door fault occurs
	 */
	public boolean handleDoors() {
		
        if (Thread.currentThread().isInterrupted()) {
            System.out.println(Thread.currentThread().getName() + " was interrupted while the elevator was opening/closing doors.");
            return false;
        }
        
	    // simulate doors opening and closing	    
	    long doorTimerStart = System.currentTimeMillis();   // start door timer
	    long doorTimeout = 10000; 	// 10 seconds timeout for the doors to close
	    
	    try {
	        this.state = 3;
	        gui.updateElevatorStatus(state);
	        Thread.sleep(timeHandleDoors); // simulate 2.5 seconds to open doors
	    } catch (InterruptedException e) {
	        System.out.println("\n" + Thread.currentThread().getName() + " thread was interrupted due to a fault.");
	    }

	    // check for a door fault if the doors do not close after opening
	    if (System.currentTimeMillis() - doorTimerStart >= doorTimeout) {
	        System.out.println("\n" + Thread.currentThread().getName() + " has encountered a door fault - doors are stuck open!");
	        this.state = 6;
	        gui.updateElevatorStatus(state);
	        // set the door fault flag to true and continue executing the rest of the elevator's tasks
	        doorFault = true;
	        
	        System.out.println("\nRetrying... please wait.");
	        
	        int retryClosingDoors = 0;
	        
	        // Handling the situation gracefully. 
	        // try closing the doors a few times. If unsuccessful, stop the elevator.
	        while (retryClosingDoors < 4 && doorFault) {
	        	System.out.println("\n" + Thread.currentThread().getName() + " retrying to close doors (attempt " + (retryClosingDoors + 1) + ")");
	        	gui.updateElevatorStatus(6);
	        	try {
	        		Thread.sleep(5000);
	        	} catch (InterruptedException e) {
	        	    System.out.println("\n" + Thread.currentThread().getName() + " thread was interrupted due to a fault.");
	        	}
	        	
	        	long doorRetryTimerStart = System.currentTimeMillis();   // start door timer
	        	
	    	    try {
	    	        this.state = 3;
	    	        gui.updateElevatorStatus(state);
	    	        Thread.sleep(timeHandleDoors); // simulate 2.5 seconds to open doors
	    	    } catch (InterruptedException e) {
	    	        System.out.println("\n" + Thread.currentThread().getName() + " thread was interrupted due to a fault.");
	    	    }
	    	    
	            if (System.currentTimeMillis() - doorRetryTimerStart >= doorTimeout) {
	                // if the doors still don't close after all retries, declaring a permanent fault
	                if (retryClosingDoors == 3) {	        
		            	System.out.println("\n" + Thread.currentThread().getName() + " has encountered a permanent fault - doors are stuck open!");
		                // notify the user of the permanent fault
		                System.out.println("Door fault not resolved - elevator out of service.");
		                gui.updateElevatorStatus(7);
		                break; // break out of the while loop if the doors are permanently stuck open	                }
	                } 
	            } else {
		            doorFault = false;
	            }
	            retryClosingDoors++;
	        }
	    }  
	    
	    
	    if (doorFault) {
	    	return false;
	    }
	    
        System.out.println("\n" + Thread.currentThread().getName() + " doors closing");
        try {
            this.state = 4;
            gui.updateElevatorStatus(state);
            Thread.sleep(timeHandleDoors); // simulate 2.5 seconds to close doors
        } catch (InterruptedException e) {
            System.out.println("\n" + Thread.currentThread().getName() + " thread was interrupted due to a fault.");
        }
        	 
	 return true;    
}	

    public void sendReceive() {
    	startFloorSenderThread(4000);
        Thread receivingThread = new Thread(() -> {
            while (true) {
                byte[] schedulerData = new byte[1000];
                DatagramPacket receivePacket = new DatagramPacket(schedulerData, schedulerData.length);
                try {
                    System.out.println(name + " waiting for request from Scheduler...");
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

                String ack = "Request processed - " + Thread.currentThread().getName();
                executeRequest();
                
                if (Thread.currentThread().isInterrupted()) {
                	this.state = 7;
                	gui.updateElevatorStatus(state);
                    System.out.println("\n" + Thread.currentThread().getName() + " is out of service.");
                    break;
                }
		      try {
		    	  if(Thread.currentThread().getName().equals("Elevator One")) {
		    		  sendPacket = new DatagramPacket(ack.getBytes(), ack.length(), InetAddress.getLocalHost(), 2000);
		    	  } else if(Thread.currentThread().getName().equals("Elevator Two")) {
		    		  sendPacket = new DatagramPacket(ack.getBytes(), ack.length(), InetAddress.getLocalHost(), 2500);
		    	  } else if(Thread.currentThread().getName().equals("Elevator Three")) {
		    		  sendPacket = new DatagramPacket(ack.getBytes(), ack.length(), InetAddress.getLocalHost(), 3000);
		    	  } else if(Thread.currentThread().getName().equals("Elevator Four")) {
		    		  sendPacket = new DatagramPacket(ack.getBytes(), ack.length(), InetAddress.getLocalHost(), 3500);
		    	  }
		      } catch(UnknownHostException e) {
			         e.printStackTrace();
			         System.exit(1);
			  }
		      
		      //send the Ack Packet to the scheduler
		      try {
		    	  socketScheduler.send(sendPacket);
		    	  System.out.println("\n" + Thread.currentThread().getName() + " is done processing this request: Acknowledgement sent to Scheduler");
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
			e.setTimeBetweenFloors(8000);
			e.setTimeHandleDoors(2500);

		    Thread t1 = new Thread(e, "Elevator One");	    	    
		    t1.start();
		    
		    Elevator e2 = new Elevator(6000, "Elevator Two");
			e2.setTimeBetweenFloors(8000);
			e2.setTimeHandleDoors(2500);

		    Thread t2 = new Thread(e2, "Elevator Two");
		    t2.start();   
		    
		    Elevator e3 = new Elevator(7000, "Elevator Three");
			e3.setTimeBetweenFloors(21000);
			e3.setTimeHandleDoors(2500);

		    Thread t3 = new Thread(e3, "Elevator Three");
		    t3.start();    
		    
		    Elevator e4 = new Elevator(8000, "Elevator Four");
			e4.setTimeBetweenFloors(8000);
			e4.setTimeHandleDoors(11000);

		    Thread t4 = new Thread(e4, "Elevator Four");
		    t4.start();    
	   }
}
        		

         	
            
    
	

