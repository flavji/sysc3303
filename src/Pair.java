/**
 * Pair Class that the Elevator Class uses to 
 * clearly differentiate one request from another
 * and store Pair Objects in a queue. 
 * 
 * @author yashk
 *
 * @param <Initial>		the initial floor of the request
 * @param <Destination>		the destination floor of the request
 */
public class Pair<Initial, Destination> {
    private final Initial initialFloor;
    private final Destination destinationFloor;
    private boolean executed;
    private boolean passengersInElevator;

    /**
     * Constructor for Pair.
     * @param initialFloor	an Initial Object, the initial floor of the request
     * @param destinationFloor	a Destination Object, the destination floor of the request
     */
    public Pair(Initial initialFloor, Destination destinationFloor) {
        this.initialFloor = initialFloor;
        this.destinationFloor = destinationFloor;
        this.executed = false;
        this.passengersInElevator = false;
    }

    /**
     * Get the initial floor of the request
     * @return	an Initial Object, the initial floor of the request
     */
    public Initial getInitialFloor() {
        return initialFloor;
    }

    /**
     * Get the destination floor of the request
     * @return	a Destination Object, the destination floor of the request
     */
    public Destination getDestinationFloor() {
        return destinationFloor;
    }
    
    /**
     * Get the status of the request
     * @return	a boolean, the status of the request
     * True if the request has been executed, false otherwise
     */
    public boolean isExecuted() {
    	return executed;
    }
    
    /**
     * Set the status of the request
     * @param executed	a boolean, the status of the request
     * True if the request has been executed, false otherwise
     */
    public void setExecuted(boolean executed) {
    	this.executed = executed;
    }
    
    /**
     * Get the status of the passengers
     * @return	a boolean, true if the passengers have already been picked up, false otherwise
     */
    public boolean passengersPickedUp() {
    	return passengersInElevator;
    }
    
    /**
     * Set the status of the passengers
     * @param passengersInElevator	a boolean, true if the passengers have already been picked up, false otherwise
     */
    public void setPassengersInElevator(boolean passengersInElevator) {
    	this.passengersInElevator = passengersInElevator;
    }
}