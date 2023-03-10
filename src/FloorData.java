import java.io.IOException;
import java.util.Date;

/**
 * FloorData Class that stores the data defined in the floorRequests.csv file.
 * 
 * @author Yash Kapoor
 * @author Faiaz Ahsan
 * @author Zeid Alwash
 * @author Fareen Lavji
 * @author Harishan Amutheesan
 * 
 * @version 03.10.2023
 */
public class FloorData {
	
	private int floors; // the number of floors the building has
	private Date time;
	private int initialFloor;
	private String floorButton;
	private int destinationFloor;
	
	public FloorData(int floors) {
		this.floors = floors;
	}

	/**
	 * Gets the time stamp of the floor request. 
	 * 
	 * @return	The Date time stamp.
	 */
	public Date getTime() {
		return time;
	}
	
	/**
	 * Gets the initial floor the elevator is at.
	 * 
	 * @return	The integer value of the initial floor.
	 */
	public int getInitialFloor() {
		return initialFloor;
	}
	
	/**
	 * Gets the direction the elevator is going in (either up or down).
	 * 
	 * @return	The String value of the elevator's direction.
	 */
	public String getFloorButton() {
		return floorButton;
	}
	
	/**
	 * Gets the final floor the elevator arrives at.
	 * 
	 * @return	The integer value of the destination floor.
	 */
	public int getDestinationFloor() {
		return destinationFloor;
	}
	
	/**
	 * Sets the time.
	 * 
	 * @param time A Date object representing the time stamp.
	 */
	public void setTime(Date time) {
		this.time = time;
	}
	
	/**
	 * Sets the initial floor the elevator is at.
	 * 
	 * @param initialFloor The integer value of the initial floor the elevator is at.
	 * @throws IOException Throws exception if the floor is out of range of the floor plan.
	 */
	public void setInitialFloor(int initialFloor) throws IOException {
		if (initialFloor > -1 && initialFloor <= this.floors) {
			this.initialFloor = initialFloor;
		}
		else {
			throw new IOException("The entered floor is out of range.");
		}
	}
	
	/**
	 * Sets the final floor the elevator arrives at.
	 * 
	 * @param destinationFloor The integer value of the final floor the elevator arrives at.
	 * @throws IOException  Throws exception if the floor is out of range of the floor plan.
	 */
	public void setDestinationFloor(int destinationFloor) throws IOException {
		if (destinationFloor > -1 && destinationFloor <=this.floors) {
			this.destinationFloor = destinationFloor;
		}
		else {
			throw new IOException("The entered floor is out of range.");
		}
	}
	
	/**
	 * Sets the direction the elevator is going in (either up or down).
	 * 
	 * @param floorButton The String value of the direction the elevator is going in.
	 * @throws IOException Throws exception if the floor button is incorrect.
	 */
	public void setFloorButton(String floorButton)  {
		this.floorButton = floorButton;
	}
}
