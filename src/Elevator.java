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
 * @version 02.04.2023
 */
public class Elevator implements Runnable {
	private Scheduler scheduler;
	private int elevatorToSchedulerCondition;
	
	
	private int currentFloor;	
	// the floor the elevator needs to go to, so it can take the request
	// the initial floor of the request
	
	private int upState;
	private int downState;
	private int idle;
	
	private boolean canService;
	// true if the request is serviceable, false otherwise
	
	
	/**
	 * Constructor for Elevator.
	 * 
	 * @param s	A Scheduler object that is used to communicate between the two clients (i.e., floor and elevator).
	 */
	public Elevator(Scheduler s) {
		this.scheduler = s;
		this.elevatorToSchedulerCondition = 0;
		this.upState = 0;
		this.downState = 0;
		this.idle = 1;
		this.currentFloor = 2; // assume elevator starts at floor 2
	}
	
	/**
	 * Sets the elevatorToScheduler Condition to 1 when it is time to go back to the scheduler.
	 * Sets the schedulerToElevatorCondition to false when it is time to go back to the
	 * scheduler so the elevator thread stops executing.
	 * 
	 * @return	An integer value (either 0 or 1) that is used to go back to scheduler from the elevator.
	 */
	public int notifyElevatorToScheduler() {
 		elevatorToSchedulerCondition = 1;
 		scheduler.setSchedulerToElevatorConditionToFalse();
 		
 		
 		return elevatorToSchedulerCondition;
	}

	/**
	 * Checks whether the request is serviceable at the moment
	 * @param fd	a FloorData object, the request that needs to be serviced
	 * @return	a boolean, true if the request is serviceable, false otherwise
	 */
	public boolean executeRequest(FloorData fd) {
		if (fd.getInitialFloor() < fd.getDestinationFloor()) {
			upState = 1;
		}
		else {
			downState = 1;
		}
		
		if(upState == 1 && currentFloor > fd.getDestinationFloor()) {
			canService = false;
			
		} else if(upState == 1 && currentFloor < fd.getDestinationFloor()) {
			downState = 0;
			currentFloor = fd.getInitialFloor();
			canService = true;
		}
		else if(downState == 1 && currentFloor < fd.getDestinationFloor()) {
			canService = false;	
		}
		else if(downState == 1 && currentFloor > fd.getDestinationFloor()) {
			upState = 0;
			currentFloor = fd.getInitialFloor();
			canService = true;
			
		}
		else if (Objects.isNull(fd)) {
			canService = false;
		}
		return canService;
		
	}
	
	/**
	 * Used to run the Elevator threads.
	 */
	@Override
    public void run() { 
        while(true) {
        	if (scheduler.getSchedulerToElevatorCondition() == 1) {
        		if(scheduler.getServiceableRequests().isEmpty() && !scheduler.getAllRequests().isEmpty()) {
        			// as long as the serviceableRequests and allFloorRequests queue are not empty
        			// keep checking whether the request is serviceable or not
        			currentFloor = ((FloorData) scheduler.getAllRequests().element()).getInitialFloor();
    	        	for(FloorData item: scheduler.getAllRequests()) {
    	        		// if request is serviceable, add the FloorData object to the serviceableRequests queue
    	        		if(executeRequest(item))  
    	        		{
    	        			    scheduler.addServiceableRequests(item);
    	        			    
    	        			    
    	        			    // setting these to 0 since our elevator is stationary
    	        			    // since it is neither going up nor down - it is not servicing anything yet
    	        			    upState = 0;
    	        			    downState = 0;
    	        		}
    	        		else {
    	        			// Otherwise, output a message that the request cannot be serviced at the moment
    	        			System.out.println("Request at time: " + item.getTime() + " cannot be processed at the moment.");
    	        		}
    	        	}
        		}
	        	while(!scheduler.getServiceableRequests().isEmpty()) {
	        		// Go through the serviceableRequests queue
	        		// until all requests have been serviced by the elevator
	        		
   	            	System.out.println("\nElevator: processing request");
   	                System.out.println("\n\tElevator Received Request: " +
   	                		"\n\t\tInitial Floor: " + ((FloorData) scheduler.getServiceableRequests().element()).getInitialFloor() +
	                		" Destination Floor: " + ((FloorData) scheduler.getServiceableRequests().element()).getDestinationFloor() +
	                		" Floor Button: " + ((FloorData)scheduler.getServiceableRequests().element()).getFloorButton() +
	                		" Time: " + ((FloorData)scheduler.getServiceableRequests().element()).getTime() + "\n");
   	                
   	          
   	                // calling this method to update the states
  	                executeRequest(scheduler.getServiceableRequests().element());
   	 			    System.out.println("Elevator upState = " + upState);
   	 			    System.out.println("Elevator downState = " + downState);
   	                System.out.println("Elevator: request was processed. Elevator is Stationary.");
   	               
   	                upState = 0;
   	                downState = 0;
   	                // Elevator is idle only when it executes ALL the requests 
   	                // Therefore, we are setting idle equals 0 since the elevator is stationary, but not idle yet
   	                idle = 0;
   	                
   	                notifyElevatorToScheduler();    // going back to scheduler from elevator, so scheduler can send the data to the floor
   	                break;    	
       	         }
        	} else {
	    		idle = 1;
	    		if (idle == 1 && scheduler.getAllRequests().isEmpty() && scheduler.getServiceableRequests().isEmpty()) {
	    			// idle when all the requests in the allFloorRequests have been serviced
	    			// and both queues are empty
	    			System.out.println("Elevator State = Idle");
	    			
	    			// setting idle equal to 0 to break out of the while loop and stop the elevator from executing
	    			idle = 0;
	    		}
        		try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}
        	}
        }
	}
}
        		

         	
            
    
	

