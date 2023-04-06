public class DestinationFloor {
	private int floorNumber;
	private boolean isInitialFloor;
	
	public DestinationFloor(int floorNumber, boolean isInitialFloor) {
		this.floorNumber = floorNumber;
		this.isInitialFloor = isInitialFloor;
	}
	
	public int getFloorNumber() {
		return floorNumber;
	}
	
	public boolean isInitialFloor() {
		return isInitialFloor;
	}
	
	public void setInitialFloor(boolean initialFloor) {
		isInitialFloor = initialFloor;
	}
}
