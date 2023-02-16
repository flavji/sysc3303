import java.util.ArrayList;
import java.util.Collection;
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
	
	private FloorData floorData;
	private ArrayList<Elevator> elevators; // collection of elevators
	private int schedulerToElevatorCondition; // equals to 1 if elevator class can work
	private int schedulerToFloorCondition; // equals to 1 if floor class can work
	
	//private String [] states;
	private int running;
	private int idle;
	private boolean doneExecuting;
	
	
	//private ArrayList<FloorData> floorRequests; // a queue of all floor requests
	private Queue<FloorData> floorRequests;
	
	private Queue<FloorData> serviceableRequests; //a queue of all serviceable requests
	
	
	/**
	 * Constructor for Scheduler.
	 */
	public Scheduler() {
		
		//initialization
		this.schedulerToElevatorCondition = 0;
		this.schedulerToFloorCondition = 0;
		this.elevators = new ArrayList<Elevator>();
		this.idle = 1;
		this.running = 0;
		this.floorRequests = new PriorityQueue<FloorData>();
		this.serviceableRequests = new PriorityQueue<FloorData>();
		
		
		
		
		
		this.doneExecuting = false;
		
		

		
		elevators.add(new Elevator(this)); //adding one default elevator to elevator list
		
	}
	
	/**
	 * Gets the FloorData Object.
	 * 
	 * @return A FloorData Object that contains all the values that are defined in the floorRequests.csv file.
	 */
	public FloorData getFloorData() {
		return floorData; 
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
	 * Sets the FloorData Object.
	 * 
	 * @param fd A FloorData Object that contains all the values that are defined in the floorRequests.csv file.
	 */
	public void setFloorData(FloorData fd) {
		this.floorData = fd; 
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
	 * Add floorData object to the queue
	 * @param fd1
	 */
	public void addRequests(FloorData fd1) {
		floorRequests.add(fd1);
		
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

//        	switch(idle) {
//        	case 0:
//        		System.out.println("The Scheduler is Running");
//        		break;
//        	case 1:
//        		System.out.println("The Scheduler is Idle");
//        		break;
//        	}
//        	
//        	switch(running) {
//        	case 0:
//        		System.out.println("The Scheduler is waiting to add to queue");
//        		break;
//        	case 1:
//        		System.out.println("The Scheduler is adding to queue");
//        		break;
//        	
//        	}
        	
        	//call the executeRequest method here and if it returns true we add to q else we don't (stay Idle)
        	FloorData fd1 = floorRequests.element();
        	
//
//        	if (idle == 1 && doneExecuting) {
//        		System.out.println("Scheduler State = Idle");
//        		break;
//        	}
//        	
//        	if (idle == 1) {
//        		System.out.println("Scheduler State = Idle");
//        	}

        	if (elevatorNotExecuted ) {
            	
                System.out.println("Request received from floor");
                
                //checking if request received from floor is serviceable
            	if(elevators.get(0).executeRequest(fd1) && idle == 1) {
            		floorRequests.remove();
            		serviceableRequests.add(fd1);
            		idle = 0;
            		running = 1;
            		
            	}// need if statements one for scheduler running and request is serviceable, one for scheduler is running and request isnt serviceable
            	//one for scheudler idle and its serviceable and one for idle and not serviceable
            	
                
                System.out.println("Scheduler State = Processing Requests from floor ");
                
                
                
//                idle = 0;
//                running = 1;
                //add request to queue
                notifySchedulerToElevator();
                
                
                elevatorNotExecuted = false;
                System.out.println("Request sent to elevator");
                
                
                try {
                	System.out.println("Scheduler State = Waiting ");
                	
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}
                
                
            }
        	//add && states condition here
            else if (getSchedulerToElevatorCondition() == 0 ){
            	System.out.println("Scheduler State = Processing Requests from Elevator ");
            
                System.out.println("Request received from elevator");

                notifySchedulerToFloor();
                System.out.println("Request sent to floor");
                doneExecuting = true;
                break;
                
            }
            else {
            	
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}
            }
        }
        
    }
}
