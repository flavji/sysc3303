import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.PriorityQueue;
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
 * @version 02.04.2023
 */
public class Scheduler implements Runnable {
	
	private ArrayList<Elevator> elevators; // collection of elevators
	private int schedulerToElevatorCondition; // equals to 1 if elevator class can work
	private int schedulerToFloorCondition; // equals to 1 if floor class can work
	
	private int idle;

	private Queue<FloorData> allFloorRequests;   // a queue of all requests in the CSV file
	private Queue<FloorData> serviceableFloorRequests;    // a queue of serviceable requests at the moment
	
	// Assume all the requests in the CSV file come in simultaneously or around roughly the same time.
	// Based off that, we check whether the request is serviceable.
	// If it is, then add it to the serviceableFloorRequests queue, service it,
	// and remove it from both the queues. 
	// If it is not serviceable at the moment, skip over it 
	// and do NOT add it to the serviceableFloorRequests queue.
	// Come back to the non-serviceable requests after the serviceable requests have been serviced
	// and check whether they are serviceable again. 

	/**
	 * Constructor for Scheduler.
	 */
	public Scheduler() {
		
		//initialization
		this.schedulerToElevatorCondition = 0;
		this.schedulerToFloorCondition = 0;
		this.elevators = new ArrayList<Elevator>();
		this.idle = 1;
		this.allFloorRequests = new LinkedList<FloorData>();
		this.serviceableFloorRequests = new LinkedList<FloorData>();

		elevators.add(new Elevator(this)); //adding one default elevator to elevator list
	}

	/**
	 * Get the schedulerToElevatorCondition.
	 * 
	 * @return	An integer value (either 0 or 1) that is used to communicate from the scheduler to the elevator thread.
	 */
	public int getSchedulerToElevatorCondition() {
		return schedulerToElevatorCondition;
	}
	
	/**
	 * Get the schedulerToFloorCondition.
	 * 
	 * @return	An integer value (either 0 or 1) that is used to communicate from the scheduler back to the floor.
	 */
	public int getSchedulerToFloorCondition() {
		return schedulerToFloorCondition;
	}
	
	/**
	 * Sets the schedulerToElevatorCondition to 0 to prevent the elevator from executing when
	 * we are going back to the scheduler, then eventually, we are going from the scheduler back to the floor thread.
	 */
	public void setSchedulerToElevatorConditionToFalse() {
		schedulerToElevatorCondition = 0;
	}
	
	/**
	 * Sets the schedulerToElevatorCondition to 1 to communicate from the scheduler to the elevator thread.
	 * Sets the floor condition to 0 so the floor thread doesn't execute while the elevator is executing.
	 */
	public void notifySchedulerToElevator() {
		schedulerToElevatorCondition = 1;
	}

	/**
	 * Sets the schedulerToFloorCondition to 1.
	 */
	public void notifySchedulerToFloor() {
 		schedulerToFloorCondition = 1;
	}
	
	/**
	 * Add requests to the allFloorRequests queue
	 * @param fd	a FloorData Object that gets added to the queue
	 */
	public void addRequests(FloorData fd) {
		allFloorRequests.add(fd);
	}
	
	/**
	 * Add requests to the servicableFloorRequests queue 
	 * @param fd	a FloorData Object that gets added to the queue
	 */
	public void addServiceableRequests(FloorData fd) {
		serviceableFloorRequests.add(fd);
	}
	
	/**
	 * remove the first FloorData Object from the servicableFloorRequests queue
	 */
	public void removeServiceableRequests() {
		serviceableFloorRequests.remove();
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
	 * Get the serviceableFloorRequests queue 
	 * @return	a Queue, the serviceableFloorRequests queue
	 */
	public Queue<FloorData> getServiceableRequests() {
		return serviceableFloorRequests;
	}
	
	/**
	 * Sets the schedulerToFloorCondition to 0 to prevent the floor from executing when
	 * we are going back to the scheduler
	 */
	public void setSchedulerToFloorConditionToFalse() {
		schedulerToFloorCondition = 0;	
	}

	/**
	 * Used to run the Scheduler thread.
	 */
	@Override
	public void run() {
		
        boolean elevatorNotExecuted = true;
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}
        
        while(true) {
        	System.out.println("SERVICEABLE REQUESTS QUEUE: " + getServiceableRequests());
        	System.out.println("ALL REQUESTS QUEUE: " + getAllRequests());
        	
        	while(!getAllRequests().isEmpty() || !getServiceableRequests().isEmpty()) {
	        	if (elevatorNotExecuted && getSchedulerToFloorCondition() == 0) {
	        		// tell the elevator to start executing
	                try {
	                    Thread.sleep(1000);
	                } catch (InterruptedException e) {}
	        		idle = 0;
	                System.out.println("\nScheduler: Request received from floor");	                
	                System.out.println("Scheduler State = processing Requests from floor ");
	                
	                notifySchedulerToElevator();
	                    
	                elevatorNotExecuted = false;
	                System.out.println("Scheduler: Request sent to elevator\n");     
	                
	            }
	            else if (getSchedulerToElevatorCondition() == 0 && idle == 0) {
	            	// tell the floor to start executing
	            	System.out.println("\nScheduler State = Processing Requests from elevator ");
	                System.out.println("Scheduler: Request received from elevator");
	
	                System.out.println("Scheduler: Request sent to floor");
	               
	                notifySchedulerToFloor();
	                
	                elevatorNotExecuted = true;
	          
	                break;
	            }
	            else {
	                try {
	                    Thread.sleep(1000);
	                } catch (InterruptedException e) {}
	            }
	        }
    		idle = 1;
    		if (idle == 1 && getAllRequests().isEmpty() && getServiceableRequests().isEmpty()) {
    			// idle when all the requests in the allFloorRequests have been serviced
    			// and both queues are empty
    			System.out.println("Scheduler State = Idle");
    			break;
    		}
        }
        
    }
}
