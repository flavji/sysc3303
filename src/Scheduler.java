import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Scheduler Class that consists of a thread that is used as a communication channel between the clients (i.e., floor and elevator).
 *
 * @author Yash Kapoor
 * @author Faiaz Ahsan
 * @author Zeid Alwash
 * @author Fareen Lavji
 * @author Harishan Amutheesan
 * 
 * @version 03.10.2023
 */
public class Scheduler implements Runnable {
	
	private FloorData floorData;
	private ArrayList<Elevator> elevators; // collection of elevators
	private int schedulerToElevatorCondition; // equals to 1 if elevator class can work
	private int schedulerToFloorCondition; // equals to 1 if floor class can work
	
	/**
	 * Constructor for Scheduler.
	 */
	public Scheduler() {
		
		//initialization
		this.schedulerToElevatorCondition = 0;
		this.schedulerToFloorCondition = 0;
		this.elevators = new ArrayList<Elevator>();
		
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
	public void setFloorData(byte[] fdBytes) {
		fdBytes.toString().split(',');
		//String[] fdString = S
		//this.floorData = fd; 
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
	 * Used to run the Scheduler thread.
	 */
	@Override
	public void run() {
        boolean elevatorNotExecuted = true;
        
        // might be redundant
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}
        
        while(true) {

            if (elevatorNotExecuted) {
                System.out.println("Request received from floor");
                notifySchedulerToElevator();
                elevatorNotExecuted = false;
                System.out.println("Request sent to elevator");
            }
            else if (getSchedulerToElevatorCondition() == 0){
                System.out.println("Request received from elevator");
                notifySchedulerToFloor();
                System.out.println("Request sent to floor");
                break;
            }
            else {
            	
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}
            }
        }
    }
	
	private void createFloorData(Date date, int iFloor, String direction, int dFloor) throws IOException {
		floorData.setTime(date);
	    floorData.setInitialFloor(iFloor);
	    floorData.setFloorButton(direction); // Up & Down
	    floorData.setDestinationFloor(dFloor);
	}
}
