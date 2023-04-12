/**
 * DestinationFloor Class that is used by the Elevator Class
 * to move passengers to a specific destination floor.
 * 
 * It is used to differentiate destination floors of requests
 * from initial floors of requests
 *  
 * For example, request: 2, 8 -> initial floor is 2 (passengers get picked up)
 * and destination floor is 8 (passengers get dropped off)
 * @author Yash Kapoor (101163338)
 *
 */
public class DestinationFloor {
	private int floorNumber;
	private boolean isInitialFloor;
	
	/**
	 * Constructor for DestinationFloor
	 * @param floorNumber	an int, the floor number the elevator needs to go to
	 * @param isInitialFloor	a boolean, true if the elevator needs to go 
	 * to that floor to pick up passengers, false otherwise (i.e, false if it needs to go to that floor to 
	 * drop off passengers)
	 */
	public DestinationFloor(int floorNumber, boolean isInitialFloor) {
		this.floorNumber = floorNumber;
		this.isInitialFloor = isInitialFloor;
	}
	
	/**
	 * Get the floor number of the destination floor the elevator needs to go to
	 * @return	an int, the floor number of the destination floor the elevator needs to go to
	 */
	public int getFloorNumber() {
		return floorNumber;
	}
	
	/**
	 * Get the status of the floor the elevator needs to go to (whether it is 
	 * the initial floor or destination floor of the request the elevator is processing)
	 * 
	 * If it is the initial floor, then the elevator needs to pick up passengers
	 * Otherwise, it needs to drop off passengers.
	 * @return	a boolean, true if the floor is an initial floor of the request, false if it is 
	 * the destination floor of the request
	 */
	public boolean isInitialFloor() {
		return isInitialFloor;
	}
	
	/**
	 * Set the status of the floor the elevator needs to go to (whether it is 
	 * the initial floor or destination floor of the request the elevator is processing)
	 * @param initialFloor		a boolean, true if the floor is an initial floor of the request, false if it is 
	 * the destination floor of the request
	 */
	public void setInitialFloor(boolean initialFloor) {
		isInitialFloor = initialFloor;
	}
}
