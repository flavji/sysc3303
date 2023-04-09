import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
	private FloorData initialRequest;
	private final Object lock = new Object();
	private final String name;
	private ConcurrentLinkedQueue<Pair<Integer, Integer>> floorsQueue;
	private PriorityQueue<DestinationFloor> destinationFloors; 
	private ConcurrentLinkedQueue<Pair<Integer, Integer>> executedRequests;
	public ElevatorGUI gui;
	private Integer initialFloor;
	private Integer destinationFloor;
	
	
	DatagramPacket sendPacket, receivePacket;
	DatagramSocket socketScheduler, socketFloor;
	
	
	/**
	 * Constructor for Elevator.
	 * 
	 * @param s	A Scheduler object that is used to communicate between the two clients (i.e., floor and elevator).
	 */
	public Elevator(int portNumber, String name) {		
		this.floorsQueue = new ConcurrentLinkedQueue<>();
		this.executedRequests = new ConcurrentLinkedQueue<>();
		this.destinationFloors = new PriorityQueue<>(Comparator.comparingInt(DestinationFloor::getFloorNumber));
		this.name = name;
		this.state = 0; 
//		this.currentFloor = 2;
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
	
	public void setInitialRequest(FloorData request) {
		this.initialRequest = request;
	}
	
	public FloorData getInitialRequest() {
		return initialRequest;
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
        initialFloor = floorPair.getFirst();
        destinationFloor = floorPair.getSecond();
    	gui.updateLogArea(floorsQueue.peek());
		if(Thread.currentThread().getName().equals("Elevator Two")) {
    		System.out.println("LOG AREA UPDATED 0");
		}
    	executedRequests.add(floorPair);

        if(!floorPair.isExecuted()) {
        	// check if the elevator is already on the initial floor
            if (currentFloor != initialFloor && !floorPair.passengersPickedUp()) {
            	System.out.println("METHOD BEING EXE");
                // move the elevator to the initial floor and open its doors to let passengers in
                System.out.println("\n" + Thread.currentThread().getName() + " needs to go to floor " + initialFloor + " to pick up passengers." );
                gui.updateElevatorStatus(state);
                if(!moveElevator(initialFloor, true)) {
                	floorFault = true;
                	gui.updateElevatorStatus(5);
                	
                    // If there is a permanent floor fault, set the elevator state to "out of service"
                	if(!this.doorFault) {
                    	try {
                    		Thread.sleep(3000);
                    	}catch(Exception e) {
                    		System.out.println(Thread.currentThread().getName() + "'s movement was interrupted due to a floor fault");
                    	}
                		System.out.println("\nThe elevator is out of service due to a floor fault. Shutting down at the next floor.");
                		 gui.updateElevatorStatus(7);
                	}
                   
                    // Interrupt the elevator thread
                    Thread.currentThread().interrupt();
                    return false;
                   }
                } else {
	            	System.out.println("\n" + Thread.currentThread().getName() + " doors opening to let people in at floor " + initialFloor);  
	                if(!handleDoors() ) {
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
            if(!moveElevator(destinationFloor, false)) {
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
        floorsQueue.remove();
        return true;      
    }
    
    public void sortDestinationFloorsByDirection(PriorityQueue<DestinationFloor> destinationFloors, int direction) {
        List<DestinationFloor> temp = new ArrayList<>();
        while (!destinationFloors.isEmpty()) {
            temp.add(destinationFloors.poll());
        }
        if (direction > 0) {
            Collections.sort(temp, Comparator.comparingInt(DestinationFloor::getFloorNumber));
        } else if (direction < 0) {
            Collections.sort(temp, Comparator.comparingInt(DestinationFloor::getFloorNumber).reversed());
        }
        destinationFloors.addAll(temp);
    }

	/**
	 * Move the elevator to the specified floor
	 * @param destinationFloor		an Integer, the floor the elevator needs to go to from its current floor
	 * @return	a boolean, true if elevator successfully moves from the current floor to the destination floor, false otherwise
	 * This method returns false if a floor fault occurs, true otherwise
	 */
	public boolean moveElevator(Integer destinationFloor, boolean isInitialFloor)  {
		String executedRequest = "Finished Requests:";
	    int direction = destinationFloor.compareTo(currentFloor);
	    long floorTimeout = 20000; 	// 20 seconds timeout for the elevator to move from one floor to another
        destinationFloors.add(new DestinationFloor(destinationFloor, isInitialFloor));
        
        while (!destinationFloors.isEmpty()) {
        	DestinationFloor currentDestinationFloorElement = destinationFloors.element();
        	int currentDestinationFloor = currentDestinationFloorElement.getFloorNumber();

        	while (currentFloor != currentDestinationFloor) {
        		gui.updateFinishedRequests(executedRequest);
            	gui.updateLogArea(new Pair<>(initialFloor, destinationFloors.peek().getFloorNumber()));
        		System.out.println(Thread.currentThread().getName() + " THE CURRENT DESTINATION IS: " + currentDestinationFloor);
        		System.out.println(Thread.currentThread().getName() + " THE CURRENT FLOOR IS: " + currentFloor);
//        		sortDestinationFloorsByDirection(destinationFloors, direction);
                
        		Iterator<DestinationFloor> iterators = destinationFloors.iterator();
        		while (iterators.hasNext()) {
        		    DestinationFloor destinationFloors = iterators.next();
        		    int floorNumber = destinationFloors.getFloorNumber();
        		    boolean isInitialFloors = destinationFloors.isInitialFloor();
        		    System.out.println("Floor " + floorNumber + " is an " + (isInitialFloors ? "initial" : "Destination") + " floor.");
        		}
        		
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
    	        
    	        // update GUI
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

    	        
//            	if(currentFloor == currentDestinationFloor && !currentDestinationFloorElement.isInitialFloor()) {
//            		if(Thread.currentThread().getName().equals("Elevator Two")) {
//                		System.out.println("LOG AREA UPDATED 1");
//            		}
//
//            		gui.updateFinishedRequests(executedRequest);
//            	}
    	        while (iterator.hasNext()) {
    	        	Pair<Integer, Integer> request = iterator.next();

                	if(currentFloor == currentDestinationFloor && !currentDestinationFloorElement.isInitialFloor()) {
                		if(Thread.currentThread().getName().equals("Elevator Two")) {
                    		System.out.println("LOG AREA UPDATED 1");
                		}

                		if(request.getSecond() == currentDestinationFloor) {
                			executedRequest += "\nInitial floor: " + request.getFirst() + " and Destination floor: " + request.getSecond();        
                		}
                	}
    	            // Check if the elevator is moving up and the request's initial floor is above the current floor,
    	            // or if the elevator is moving down and the request's initial floor is below the current floor.
    	            boolean isRequestInRange = (direction > 0 && request.getFirst() >= currentFloor && request.getFirst() <= request.getSecond() && !request.isExecuted())
    	                    || (direction < 0 && request.getFirst() <= currentFloor && request.getFirst() >= request.getSecond());

    	            if (isRequestInRange) {
    	            	System.out.println(Thread.currentThread().getName() + " BEING EXECUTED HERE: " + request.getFirst() + " " + destinationFloor);
    	                request.setPassengersInElevator(true);

    	                if (request.getSecond() == destinationFloor || request.getSecond() < destinationFloor) {
    	                	System.out.println(Thread.currentThread().getName() + " BEING EXECUTED HERE 2");   	                	
    	                    request.setExecuted(true);
    	                    destinationFloors.add(new DestinationFloor(request.getFirst(), true));
    	                    destinationFloors.add(new DestinationFloor(request.getSecond(), false));
    	                    sortDestinationFloorsByDirection(destinationFloors, request.getFirst().compareTo(request.getSecond()));
    	                    currentDestinationFloor = destinationFloors.peek().getFloorNumber();
    	                    currentDestinationFloorElement = new DestinationFloor(destinationFloors.peek().getFloorNumber(), true);
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

	public void startReceiving() {
		Runnable receivingThread = () -> {
            while (true) {
            	System.out.println("BEING EXECUTED");
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

                    System.out.println("BEING EXECUTED 2");
                    synchronized (lock) {
                        lock.notify(); // notify the elevator thread to execute the request
                    }
                    System.out.println("BEING EXECUTED 3");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(receivingThread).start();
	}
    public void send() {
    	startFloorSenderThread(4000);

        while (true) {
            synchronized (lock) {
                while (floorsQueue.isEmpty()) {
                    try {
                        lock.wait(); // wait for a request to be received
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
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
		    	  // Elevator has finished the request
		    	  gui.updateElevatorStatus(8);
		    	  String requestsExecuted = "Finished Requests:";
	    	      Iterator<Pair<Integer, Integer>> iterator = executedRequests.iterator();
	    	      
	    	      while (iterator.hasNext()) {
	    	          Pair<Integer, Integer> request = iterator.next();
	    	          requestsExecuted += "\nInitial floor: " + request.getFirst() + " and Destination floor: " + request.getSecond();        
	    	      }
	    	      System.out.println("REQUESTS EXECUTED: " + requestsExecuted);
	    	      gui.updateFinishedRequests(requestsExecuted);
	    	      gui.updateLogArea(new Pair<>(null, null));

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
		startReceiving();
		send();
	}
	
	public static void main(String args[])
	   {
			Elevator e = new Elevator(5000, "Elevator One");
			e.setTimeBetweenFloors(8000);
			e.setTimeHandleDoors(2500);
			e.setCurrentFloor(1);

		    Thread t1 = new Thread(e, "Elevator One");	    	    
		    t1.start();
		    
		    Elevator e2 = new Elevator(6000, "Elevator Two");
			e2.setTimeBetweenFloors(8000);
			e2.setTimeHandleDoors(2500);
			e2.setCurrentFloor(2);

		    Thread t2 = new Thread(e2, "Elevator Two");
		    t2.start();   
		    
		    Elevator e3 = new Elevator(7000, "Elevator Three");
			e3.setTimeBetweenFloors(21000);
			e3.setTimeHandleDoors(2500);
			e3.setCurrentFloor(2);


		    Thread t3 = new Thread(e3, "Elevator Three");
		    t3.start();    
		    
		    Elevator e4 = new Elevator(8000, "Elevator Four");
			e4.setTimeBetweenFloors(8000);
			e4.setTimeHandleDoors(11000);
			e4.setCurrentFloor(16);


		    Thread t4 = new Thread(e4, "Elevator Four");
		    t4.start();    
	   }
}
        		

         	
            
    
	

