import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Elevator Class that consists of the elevator thread that will execute after the scheduler sends the request.
 * 
 * @author Yash Kapoor
 * @author Faiaz Ahsan
 * @author Zeid Alwash
 * @author Fareen Lavji
 * @author Harishan Amutheesan
 * 
 * @version 03.10.2023
 */
public class Elevator implements Runnable {
	private Scheduler scheduler; // can potentially remove
	private int elevatorToSchedulerCondition; // can potentially remove
	
	private Direction direction;
	private static long travelTime = 1000; // average time elevator takes to move 1 floor
	Collection<Integer> requestsQueue;
	private Integer currentFloor;
	
	
	/**
	 * Constructor for Elevator.
	 * 
	 * @param s	A Scheduler object that is used to communicate between the two clients (i.e., floor and elevator).
	 */
	public Elevator(Scheduler s) {
		this.scheduler = s;
		this.elevatorToSchedulerCondition = 0;
		this.direction = Direction.stationary; // elevator always starts stationary
		this.requestsQueue = Collections.synchronizedCollection(new LinkedList<>());
		this.currentFloor = 2; // assuming that elevator always starts at floor 2
	}
	
	/**
	 * Returns the direction of the elevator.
	 * 
	 * @return Up if moving up, down if moving down, stationary if not moving.
	 */
	public Direction getDirection() { return this.direction; }
	
	/**
	 * Returns the size of the elevators queue.
	 * 
	 * @return An integer value with the elevator's queue size.
	 */
	public int getQueueSize() { return requestsQueue.size(); }
	
	/**
	 * Adds request to queue based on most efficient position.
	 * 
	 * @param destinationFloor The destination floor of the given request.
	 */
	public void addRequest(int destinationFloor, int position) {
		requestsQueue.add(destinationFloor, position);
	}
	
	// can potentially remove
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
	 * Used to run the Elevator threads.
	 */
	@Override
    public synchronized void run() { 
        while(true) {
        	
            if (!requestsQueue.isEmpty()) {
            	// if the queue is not empty, execute the next available request
            	executeRequest(requestsQueue.remove());
                System.out.println("\tElevator Received Request: " +
                		"\n\t\tInitial Floor: " + scheduler.getFloorData().getInitialFloor() +
                		" Destination Floor: " + scheduler.getFloorData().getDestinationFloor() +
                		" Floor Button: " + scheduler.getFloorData().getFloorButton() +
                		" Time: " + scheduler.getFloorData().getTime());
                notifyElevatorToScheduler();
            }
            else {
            	
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}
            }
        }
            
    }
	
	/**
	 * Executes a request based on the destination floor. Switches the state to active and calculates how long the
	 * elevator would take to reach the destination floor. If there are no more requests in the queue, state stays
	 * active.
	 * 
	 * @param destinationFloor The destination floor that the elevator needs to go to.
	 */
	private void executeRequest(Integer destinationFloor) {
		
		// switch the state of the elevator
		int comparisor = destinationFloor.compareTo(this.currentFloor);
		
		if (comparisor < 0) { this.direction = Direction.up; }
		else if (comparisor > 0) {this.direction = Direction.down; }
		else { this.direction = Direction.stationary; }
		
		// processing request
		try {
			Thread.sleep(Elevator.travelTime * (destinationFloor - this.currentFloor));
		}
		catch (InterruptedException e) {}
		
		// switch elevator back to stationary if there are no more requests
		if (!this.requestsQueue.isEmpty()) { this.direction = Direction.stationary; }
		
		this.currentFloor = destinationFloor; // update current floor
		
	}
}
