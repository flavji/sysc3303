/**
 * @author Yash Kapoor
 * @author Faiaz Ahsan
 * @author Zeid Alwash
 * @author Flo Lavji
 * @author Harishan Amutheesan
 */

import java.util.Objects;

/**
 * Scheduler Class that consists of a thread that is used as a communication channel between the clients (i.e., floor and elevator)
 *
 */
public class Scheduler implements Runnable {
	
	private FloorData floorData;
	private Elevator[] numberOfElevators = new Elevator[1];
	private int schedulerToElevatorCondition;
	private int schedulerToFloorCondition;
	private int floorCondition;
	
	/**
	 * Constructor for Scheduler
	 */
	public Scheduler() {
		this.schedulerToElevatorCondition = 0;
		this.schedulerToFloorCondition = 0;
		this.floorCondition = 0;
		this.floorData = new FloorData();
	}
	
	/**
	 * Gets the FloorData Object 
	 * @return		a FloorData Object, contains all the values that are defined in the CSV file
	 */
	public FloorData getFloorData() {
		return floorData; 
	}
	
	/**
	 * Sets the FloorData Object
	 * @param fd	a FloorData Object, contains all the values that are defined in the CSV file
	 */
	public void setFloorData(FloorData fd) {
		this.floorData = fd; 
	}
	
	/**
	 * Gets the floorCondition
	 * @return	an int, the condition (either 0 or 1) that is used to determine whether the floor thread should run or not
	 */
	public int getFloorCondition() {
		return floorCondition;
	}
	
	/**
	 * Sets the floorCondition to 1, so the floor thread can set the FloorData Object in the scheduler class,
	 * so the elevator can access the FloorData Object from the scheduler class and print it
	 */
	public void notifyFloorToScheduler() {
		floorCondition = 1;
	}
	
	/**
	 * Get the schedulerToElevatorCondition
	 * @return	an int, the condition (either 0 or 1) that is used to communicate from the scheduler to the elevator thread
	 */
	public int getSchedulerToElevatorCondition() {
		return schedulerToElevatorCondition;
	}
	
	/**
	 * Sets the schedulerToElevatorCondition to 1 to communicate from the scheduler to the elevator thread
	 * Sets the floor condition to 0, so the floor thread doesn't execute while the elevator is executing
	 */
	public void notifySchedulerToElevator() {
		schedulerToElevatorCondition = 1;
		floorCondition = 0;
	}
	
	/**
	 * Get the schedulerToFloorCondition
	 * @return	an int, the condition (either 0 or 1) that is used to communicate from the scheduler back to the floor
	 */
	public int getSchedulerToFloorCondition() {
		return schedulerToFloorCondition;
	}
	
	/**
	 * Sets the schedulerToFloorCondition to 1
	 */
	public void notifySchedulerToFloor() {
 		schedulerToFloorCondition = 1;
	}
	
	/**
	 * Sets the schedulerToElevatorCondition to 0 to prevent the elevator from executing when we are going back to the scheduler,
	 * then eventually, we are going from the scheduler back to the floor thread
	 */
	public void setSchedulerToElevatorConditionToFalse() {
		schedulerToElevatorCondition = 0;
	}
	

	/**
	 * Used to run the Scheduler thread
	 */
	@Override
	public void run() {
		boolean elevatorNotExecuted = true;
		while(true) {
			if (!Objects.isNull(floorData) && getSchedulerToElevatorCondition() != 1 && elevatorNotExecuted) {
				numberOfElevators[0] = new Elevator(this);
				notifySchedulerToElevator();
				elevatorNotExecuted = false;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {}
			}
			else {
				notifySchedulerToFloor();
				break;
			}
		}
	}
}
