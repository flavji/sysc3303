/**
 * Main Class that initializes and starts all the threads and declares the necessary objects.
 *
 * @author Yash Kapoor
 * @author Faiaz Ahsan
 * @author Zeid Alwash
 * @author Fareen Lavji
 * @author Harishan Amutheesan
 * 
 * @version 02.04.2023
 */
public class Main {
	
	/**
	 * Creates all the necessary objects and starts all the threads.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Thread floor, elevator, scheduler;
		Scheduler s = new Scheduler();
		
		floor = new Thread(new Floor(s, "./floorRequests.csv"), "Floor");
		elevator = new Thread(new Elevator(s), "Elevator");
		scheduler = new Thread(s, "Scheduler");
		
		floor.start();
		scheduler.start();
		elevator.start();
	}
}


