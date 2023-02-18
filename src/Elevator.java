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
	//private FloorData request;
	private int elevatorToSchedulerCondition;
	private int currentFloor;
	
	private int upState;
	private int downState;
	private int stationary;
	private boolean canService;
	
	
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
		this.stationary = 1;
		currentFloor = 2; //Assume elevator starts at floor 2
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

	public boolean executeRequest(FloorData fd) {
		if(fd.getInitialFloor() < fd.getDestinationFloor() && stationary == 1) {
			upState = 1;
			stationary = 0;
			currentFloor = fd.getDestinationFloor();
			canService = true;
			
		}else if(fd.getInitialFloor() > fd.getDestinationFloor() && stationary == 1) {
			downState = 1;
			stationary = 0;
			currentFloor = fd.getDestinationFloor();
			canService = true;
			
		}else if(upState == 1 && currentFloor > fd.getDestinationFloor()) {
			canService = false;
			
		}else if(upState == 1 && currentFloor < fd.getDestinationFloor()) {
			currentFloor = fd.getDestinationFloor();
			canService = true;
			
		}
		else if(downState == 1 && currentFloor < fd.getDestinationFloor()) {
			canService = false;	
		}
		else if(downState == 1 && currentFloor > fd.getDestinationFloor()) {
			currentFloor = fd.getDestinationFloor();
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
        while( true ) {
        	

        	

        	
        	if (scheduler.getSchedulerToElevatorCondition() == 1 ) {

            	System.out.println("Requests size: " + scheduler.getAllRequests().size());

	        	while( !scheduler.getAllRequests().isEmpty() ) {
	        		
	        		if(executeRequest((FloorData) scheduler.getAllRequests().element())) {
	       			  
	       	            	System.out.println("Elevator State: processing request");
	       	                System.out.println("\tElevator Received Request: " +
	       	                		"\n\t\tInitial Floor: " + ((FloorData) scheduler.getAllRequests().element()).getInitialFloor() +
	    	                		" Destination Floor: " + ((FloorData) scheduler.getAllRequests().element()).getDestinationFloor() +
	    	                		" Floor Button: " + ((FloorData)scheduler.getAllRequests().element()).getFloorButton() +
	    	                		" Time: " + ((FloorData)scheduler.getAllRequests().element()).getTime());
	       	                
	       	                upState = 0;
	       	                downState = 0;
	       	                stationary = 1;
	       	                System.out.println("Elevator State: request was processed, Elevator is Stationary");
	       	               
	       	                notifyElevatorToScheduler();// going back to scheduler
	       	                break;
	       	            }else {
	       	             try {
	                         Thread.sleep(1000);
	                     } catch (InterruptedException e) {}
	                 }
       	            
       	            	
       	            }
        		
        	}else {
        		try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}
        		
        	}
        }
	}
}
        		

         	
            
    
	

