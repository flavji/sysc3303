public class Pair<T, U> {
    private final T first;
    private final U second;
    private boolean executed;
    private boolean passengersInElevator;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
        this.executed = false;
        this.passengersInElevator = false;
    }

    public T getFirst() {
        return first;
    }

    public U getSecond() {
        return second;
    }
    
    public boolean isExecuted() {
    	return executed;
    }
    
    public void setExecuted(boolean executed) {
    	this.executed = executed;
    }
    
    public boolean passengersPickedUp() {
    	return passengersInElevator;
    }
    
    public void setPassengersInElevator(boolean passengersInElevator) {
    	this.passengersInElevator = passengersInElevator;
    }
}