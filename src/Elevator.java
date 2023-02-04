/**
 * @author Yash Kapoor
 * @author Faiaz Ahsan
 * @author Zeid Alwash
 * @author Flo Lavji
 * @author Harishan Amutheesan
 */

/**
 * Elevator Class that consists of the elevator thread that will execute after the scheduler sends the request
 */
public class Elevator implements Runnable {
	private Scheduler scheduler;
	private int elevatorToSchedulerCondition;
	
	/**
	 * Constructor for Elevator
	 * @param s		a Scheduler Object, the server that is used to communicate between the two clients (i.e., floor and elevator)
	 */
	public Elevator(Scheduler s) {
		this.scheduler = s;
		this.elevatorToSchedulerCondition = 0;
	}
	
	/**
	 * Sets the elevatorToScheduler Condition to 1 when it is time to go back to the scheduler
	 * Sets the schedulerToElevatorCondition to false when it is time to go back to the scheduler, so the elevator thread stops executing
	 * @return	an int, a condition (either 0 or 1) that is used to go back to scheduler from the elevator
	 */
	public int notifyElevatorToScheduler() {
 		elevatorToSchedulerCondition = 1;
 		scheduler.setSchedulerToElevatorConditionToFalse();
 		
 		return elevatorToSchedulerCondition;
	}

	/**
	 * Used to run the Elevator threads
	 */
	@Override
	public void run() { 
		while(true) {
			if (scheduler.getFloorData() != null && scheduler.getSchedulerToElevatorCondition() == 1) { 				
				System.out.println("Elevator Success: " + "Initial Floor: " + scheduler.getFloorData().getInitialFloor() + " Destination Floor: " + scheduler.getFloorData().getDestinationFloor() + " Floor Button: " + scheduler.getFloorData().getFloorButton() + " Time: " + scheduler.getFloorData().getTime());
				notifyElevatorToScheduler();
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {} 
			}
		}
			
	}
	
}
