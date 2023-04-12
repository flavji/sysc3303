import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Elevator Class that consists of the elevator thread that will execute after the scheduler sends the request.
 * 
 * Receives the request from the scheduler, processes the request, and then sends an acknowledgement to the scheduler
 * that indicates the request has been successfully serviced by the elevator.
 * 
 * There are a total of 4 elevators (a separate thread is used for each elevator).
 * There are 8 states for each elevator: 
 * 0 (stationary), 1 (moving up), 2 (moving down), 3 (doors opening), 4 (doors closing),
 * 5 (floor fault), 6 (door fault), 7 (out of service)
 * 
 * @author Yash Kapoor
 * @author Faiaz Ahsan
 * @author Zeid Alwash
 * @author Fareen Lavji
 * @author Harishan Amutheesan
 * 
 * @version Final Project Submission
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
	private List<DestinationFloor> destinationFloors; 
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
		this.destinationFloors = new ArrayList<>();
		this.name = name;
		this.state = 0; 	// elevator is initially stationary
		this.portNumber = portNumber;
		this.doorFault = false;
		
		gui = new ElevatorGUI(name);	
		
	    try {
	         socketScheduler = new DatagramSocket(portNumber);
	    } catch (SocketException se) {
	         System.out.println("Terminated due to a fault in the program");
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
	
	/**
	 * Sets the current request the elevator is processing
	 * @param request	a FloorData object, the current request the elevator is processing
	 */
	public void setCurrentRequest(FloorData request) {
		this.currentRequest = request;
	}
	
	/**
	 * Gets the current request the elevator is processing
	 * @return	a Floordata object, the current request the elevator is processing
	 */
	public FloorData getCurrentRequest() {
		return currentRequest;
	}
	
	/**
	 * Sets the initial request that the elevator receives
	 * when it is stationary. If a request is assigned
	 * to the elevator by the scheduler while it is currently 
	 * moving, then initialRequest will NOT be updated with
	 * that request since we want the scheduler to check
	 * whether the request it receives lies between the INITIAL
	 * request initial floor and destination floor.
	 * 
	 * For example, if the elevator receives initial floor: 3, destination floor: 22
	 * from the scheduler while it is stationary, then initialRequest will be updated
	 * to that specific request. Then, while the elevator is moving (processing that request) 
	 * and it receives initial floor: 10, destination floor: 13, the initialRequest will remain
	 * initial floor: 3, destination floor: 22. It will NOT be updated since the elevator
	 * is moving.
	 *  
	 * @param request	a FloorData object, the request 
	 * that the elevator receives when it is stationary
	 */
	public void setInitialRequest(FloorData request) {
		this.initialRequest = request;
	}
	
	/**
	 * Gets the initial request that the elevator receives when it is 
	 * stationary
	 * @return a FloorData object, the request 
	 * that the elevator receives when it is stationary
	 */
	public FloorData getInitialRequest() {
		return initialRequest;
	}
	
	/**
	 * Returns the current floor of the elevator.
	 * 
	 * @return 	an int, current floor of the elevator
	 */
	public void setCurrentFloor(int currentFloor) {
		this.currentFloor = currentFloor;
	}
	
	/**
	 * Sends the elevator status (current floor and state of the elevator)
	 * to the scheduler every second.
	 * @param port	an int, the port number of the socket
	 */
    public void sendElevatorStatus(int port) {
    	String currentThread = Thread.currentThread().getName();
        Runnable elevatorStatusSender = () -> {
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
            	System.out.println("This thread got terminated because of a fault in the program.");
            } finally {
                if (socket != null) {
                    socket.close();
                }
            }
        };
        new Thread(elevatorStatusSender).start();
    }
    
    /**
     * Executes the first request in the floorsQueue queue. 
     * Once it is done processing the request, it removes the request
     * from the floorsQueue. 
     * 
     * This method keeps executing requests until the floorsQueue queue 
     * is empty.
     * 
     * @return	a boolean, true if the request is successfully serviced, false otherwise
     */
    private boolean executeRequest() {

    	boolean floorFault = false;
    	boolean doorFault = false;
        Pair<Integer, Integer> floorPair = floorsQueue.peek();
        initialFloor = floorPair.getInitialFloor();
        destinationFloor = floorPair.getDestinationFloor();
    	gui.updateLogArea(floorsQueue.peek());

    	executedRequests.add(floorPair);

        if(!floorPair.isExecuted()) {
        	// check if the elevator is already on the initial floor
            if (currentFloor != initialFloor && !floorPair.passengersPickedUp()) {
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
            }
        }
        floorsQueue.remove();
        return true;      
    }
    
    /**
     * Sorts the destinationFloors queue based on the direction. 
     * 
     * If the elevator is going up (direction > 0), then it sorts the queue in ascending order
     * Otherwise, if the elevator is going down (direction < 0), then it sorts the queue in descending order
     * 
     * @param destinationFloors		the destinationFloors queue, stores the list of destination floors the elevator needs to stop at
     * @param direction		the direction of the elevator, up (greater than 0) or down (less than 0)
     */
    public void sortDestinationFloorsByDirection(int direction) {
        if (direction > 0) {
            // Sort in ascending order based on floorNumber
            Collections.sort(destinationFloors, Comparator.comparingInt(DestinationFloor::getFloorNumber));
        } else if (direction < 0) {
            // Sort in descending order based on floorNumber
            Collections.sort(destinationFloors, Comparator.comparingInt(DestinationFloor::getFloorNumber).reversed());
        }
    }



	/**
	 * Move the elevator to the specified floor
	 * @param destinationFloor		an Integer, the floor the elevator needs to go to from its current floor
	 * @param isInitialFloor	a boolean, true if the elevator needs to go to the initial floor to pick up passengers
	 * 							false otherwise (meaning it is going to the destination floor of the request to drop 
	 * 							off passengers).
	 * 
	 * @return	a boolean, true if elevator successfully moves from the current floor to the destination floor, false otherwise
	 * This method returns false if a floor/door fault occurs, true otherwise
	 */
	public boolean moveElevator(Integer destinationFloor, boolean isInitialFloor)  {
		String executedRequest = "";
	    int direction = destinationFloor.compareTo(currentFloor);
	    long floorTimeout = 20000; 	// 20 seconds timeout for the elevator to move from one floor to another
        destinationFloors.add(new DestinationFloor(destinationFloor, isInitialFloor));
        
        while (!destinationFloors.isEmpty()) {
        	DestinationFloor currentDestinationFloorElement = destinationFloors.get(0);
        	int currentDestinationFloor = currentDestinationFloorElement.getFloorNumber();

        	while (currentFloor != currentDestinationFloor) {
        		if(!executedRequest.equals("")) {
            		gui.updateFinishedRequests(executedRequest);
        		}
            	gui.updateLogArea(new Pair<>(initialFloor, destinationFloors.get(0).getFloorNumber()));
        		
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

    	        while (iterator.hasNext()) {
    	        	Pair<Integer, Integer> request = iterator.next();

                	if(currentFloor == currentDestinationFloor && !currentDestinationFloorElement.isInitialFloor()) {
                		if(request.getDestinationFloor() == currentDestinationFloor) {
                			executedRequest += "\nInitial floor: " + request.getInitialFloor() + " and Destination floor: " + request.getDestinationFloor();        
                		}
                	}
    	            // Check if the elevator is moving up and the request's initial floor is above the current floor,
    	            // or if the elevator is moving down and the request's initial floor is below the current floor.
    	            boolean isRequestInRange = (direction > 0 && request.getInitialFloor() >= currentFloor && request.getInitialFloor() <= request.getDestinationFloor() && !request.isExecuted())
    	                    || (direction < 0 && request.getInitialFloor() <= currentFloor && request.getInitialFloor() >= request.getDestinationFloor() && !request.isExecuted());

    	            if (isRequestInRange) {
    	                request.setPassengersInElevator(true);
	                    request.setExecuted(true);
	                    destinationFloors.add(new DestinationFloor(request.getInitialFloor(), true));
	                    destinationFloors.add(new DestinationFloor(request.getDestinationFloor(), false));
	                    sortDestinationFloorsByDirection(request.getDestinationFloor().compareTo(request.getInitialFloor()));
	                    currentDestinationFloor = destinationFloors.get(0).getFloorNumber();
	                    currentDestinationFloorElement = new DestinationFloor(destinationFloors.get(0).getFloorNumber(), true);
    	                
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
            
        	destinationFloors.remove(0);
	    
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

	/**
	 * Constantly listens for requests on a separate thread 
	 * from the scheduler. Once it receives the request,
	 * it adds it to the floorsQueue queue. Then, it notifies
	 * the send() method to start processing the request.
	 */
	public void startReceivingRequests() {
		Runnable receivingThread = () -> {
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

                    synchronized (lock) {
                        lock.notify(); // notify the elevator thread to execute the request
                    }
                } catch (IOException e) {
                	System.out.println("This thread got terminated because of a fault in the program.");
                }
            }
        };
        new Thread(receivingThread).start();
	}
	
	/**
	 * Sends an acknowledgement to the scheduler to the appropriate 
	 * port depending on which elevator finished executing its request.
	 * 
	 * Elevator One: send acknowledgement to port 2000
	 * Elevator Two: send acknowledgement to port 2500
	 * Elevator Three: send acknowledgement to port 3000
	 * Elevator Four: send acknowledgement to port 3500
	 */
    public void send() {
		gui.updateElevatorFloor(getCurrentFloor());
    	// start sending information to the scheduler, so it is aware
    	// of the elevator's current floor and status at all times
    	sendElevatorStatus(4000);

        while (true) {
            synchronized (lock) {
                while (floorsQueue.isEmpty()) {
                    try {
                        lock.wait(); // wait for a request to be received
                    } catch (InterruptedException e) {
                        System.out.println("Failed to acquire lock because the elevator got terminated due to a door/floor fault.");
                        Thread.currentThread().interrupt(); // Reset the interrupted status
                        return; 
                    }
                }
            }

            String ack = "Request processed - " + Thread.currentThread().getName() + " successful";
            
            executeRequest();
            
            if (Thread.currentThread().isInterrupted()) {
            	String notifyFault = "Request NOT processed - " + Thread.currentThread().getName() + " unsuccessful";
            	// floor/door fault occurs, then we want to step out of the while loop 
            	// so the elevator stops processing/receiving requests
            	this.state = 7;
            	gui.updateElevatorStatus(state);
                System.out.println("\n" + Thread.currentThread().getName() + " is out of service.");
                try {
    	    	  if(Thread.currentThread().getName().equals("Elevator One")) {
    	    		  sendPacket = new DatagramPacket(notifyFault.getBytes(), notifyFault.length(), InetAddress.getLocalHost(), 2000);
    	    	  } else if(Thread.currentThread().getName().equals("Elevator Two")) {
    	    		  sendPacket = new DatagramPacket(notifyFault.getBytes(), notifyFault.length(), InetAddress.getLocalHost(), 2500);
    	    	  } else if(Thread.currentThread().getName().equals("Elevator Three")) {
    	    		  sendPacket = new DatagramPacket(notifyFault.getBytes(), notifyFault.length(), InetAddress.getLocalHost(), 3000);
    	    	  } else if(Thread.currentThread().getName().equals("Elevator Four")) {
    	    		  sendPacket = new DatagramPacket(notifyFault.getBytes(), notifyFault.length(), InetAddress.getLocalHost(), 3500);
    	    	  }
                } catch(UnknownHostException e) {
               	 System.out.println("This thread got terminated because of a fault in the program.");
   		         System.exit(1);
               }
                
    		    // send the acknowledgement packet to the scheduler that the 
                // request has not been processed successfully 
    		    try {
    		    	 socketScheduler.send(sendPacket);
    		    	 System.out.println("\n" + Thread.currentThread().getName() + " request cannot be processed due to a fault: Acknowledgement sent to Scheduler");
    		    	 System.out.println("Containing: " + new String(sendPacket.getData(),0,sendPacket.getLength()));
    		    } catch (IOException e) {
                	 System.out.println("This thread got terminated because of a fault in the program.");
    		         System.exit(1);
    		    }
                break;
            }
            try {
	    	  // Elevator has finished the request
	    	  gui.updateElevatorStatus(8);
	    	  String requestsExecuted = "";
    	      Iterator<Pair<Integer, Integer>> iterator = executedRequests.iterator();
    	      
    	      while (iterator.hasNext()) {
    	          Pair<Integer, Integer> request = iterator.next();
    	          requestsExecuted += "\nInitial floor: " + request.getInitialFloor() + " and Destination floor: " + request.getDestinationFloor();        
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
            	 System.out.println("This thread got terminated because of a fault in the program.");
		         System.exit(1);
            }
		      
		    //send the acknowledgement packet to the scheduler
		    try {
		    	 socketScheduler.send(sendPacket);
		    	 System.out.println("\n" + Thread.currentThread().getName() + " is done processing this request: Acknowledgement sent to Scheduler");
		    	 System.out.println("Containing: " + new String(sendPacket.getData(),0,sendPacket.getLength()));
		    } catch (IOException e) {
            	 System.out.println("This thread got terminated because of a fault in the program.");
		         System.exit(1);
		    }
        }
    }
	
	/**
	 * Used to run the Elevator threads.
	 */
	@Override
    public void run() { 
		startReceivingRequests();
		send();
	}
	
	public static void main(String args[])
	   {
		// default time to move between floors: 8 seconds
		// default time to open doors: 2.5 seconds
		// default time to close doors: 2.5 seconds
		
			Elevator e = new Elevator(5000, "Elevator One");
			e.setTimeBetweenFloors(8000);
			e.setTimeHandleDoors(2500);
			e.setCurrentFloor(22);

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
        		

         	
            
    
	

