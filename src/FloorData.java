/**
 * @author Yash Kapoor
 * @author Faiaz Ahsan
 * @author Zeid Alwash
 * @author Flo Lavji
 * @author Harishan Amutheesan
 */

import java.util.Date;

/**
 * FloorData Class that stores the data defined in the CSV file
 *
 */
public class FloorData {
	private Date time;
	private int initialFloor;
	private String floorButton;
	private int destinationFloor;
	
	/**
	 * Gets the time 
	 * @return	a Date Object, consists of the time/date
	 */
	public Date getTime() {
		return time;
	}
	
	/**
	 * Gets the initial floor the elevator is at
	 * @return	an int, the initial floor the elevator is at
	 */
	public int getInitialFloor() {
		return initialFloor;
	}
	
	/**
	 * Gets the direction the elevator is going in (either up or down)
	 * @return	a String, the direction the elevator is going in
	 */
	public String getFloorButton() {
		return floorButton;
	}
	
	/**
	 * Gets the final floor the elevator arrives at
	 * @return	an int, the final floor the elevator arrives at
	 */
	public int getDestinationFloor() {
		return destinationFloor;
	}
	
	/**
	 * Sets the time 
	 * @param time	a Date Object, consists of the time/date
	 */
	public void setTime(Date time) {
		this.time = time;
	}
	
	/**
	 * Sets the initial floor the elevator is at 
	 * @param initialFloor		an int, the initial floor the elevator is at
	 */
	public void setInitialFloor(int initialFloor) {
		this.initialFloor = initialFloor;
	}
	
	/**
	 * Sets the final floor the elevator arrives at
	 * @param destinationFloor		an int, the final floor the elevator arrives at
	 */
	public void setDestinationFloor(int destinationFloor) {
		this.destinationFloor = destinationFloor;
	}
	
	/**
	 * Sets the direction the elevator is going in (either up or down)
	 * @param floorButton		a String, the direction the elevator is going in
	 */
	public void setFloorButton(String floorButton) {
		this.floorButton = floorButton;
	}
	
	
}
