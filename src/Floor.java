import java.util.Date;
import java.io.*;
import java.text.*;

/**
 * Floor Class that consists of the floor thread that executes first to send a request to the scheduler.
 * 
 * @author Yash Kapoor
 * @author Faiaz Ahsan
 * @author Zeid Alwash
 * @author Fareen Lavji
 * @author Harishan Amutheesan
 * 
 * @version 02.04.2023
 */
public class Floor implements Runnable {
	private Scheduler scheduler;
	private FloorData floorData; 
	private String floorRequests;
	
	/**
	 * Constructor for Floor that initializes a scheduler and floor data.
	 * 
	 * @param s	A Scheduler Object, the server that is used to communicate between the two clients (i.e., floor and elevator).
	 */
	public Floor(Scheduler s, int floors, String floorRequests) {
		this.scheduler = s;
		this.floorData = new FloorData(floors); //setting default floors to 10 floors
		this.floorRequests = floorRequests;
	}
	
	/**
	 * Reads the floorRequests.csv file that contains instructions for the elevator to execute.
	 * Sets the floor data, and notifies the scheduler.
	 */ 
	public void unwrapData() {
		try 
	    {
			// parsing a CSV file into BufferedReader class constructor
			File csvFile = new File(floorRequests);
		    BufferedReader br = new BufferedReader(new FileReader(csvFile));

		    String line = "";
		    while ((line = br.readLine()) != null)   //returns a Boolean value
		    {
		    	String[] elevatorData = new String[4];
			    elevatorData = line.split(",");
			
			    String start_date = elevatorData[0];
			    DateFormat formatter = new SimpleDateFormat("hh:mm:ss");
			    Date date = (Date) formatter.parse(start_date);
			   
			    setFloorData(date,
			    		Integer.parseInt(elevatorData[1]),
			    		elevatorData[2],
			    		Integer.parseInt(elevatorData[3]));
			    
			    scheduler.addRequests(floorData);
		    }
		    br.close();
	    }
	    catch (IOException e) {} 
		catch (ParseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Used to run the Floor thread.
	 */
	@Override
	public void run() {
        System.out.println("Starting at Floor");
        unwrapData();
        scheduler.setFloorData(floorData);
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}

        while(true) {
            if(scheduler.getSchedulerToFloorCondition() == 1) {
                System.out.println("\tArrived At Floor:\n" +
                		"\t\tInitial Floor: " + floorData.getInitialFloor() +
                		" Destination Floor: " + floorData.getDestinationFloor() +
                		" Floor Button: " + floorData.getFloorButton() +
                		" Time: " + floorData.getTime());
                System.exit(1);
            } else {
            	
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}
            }
        }    
    }
	
	private void setFloorData(Date date, int iFloor, String direction, int dFloor) throws IOException {
		floorData.setTime(date);
	    floorData.setInitialFloor(iFloor);
	    floorData.setFloorButton(direction); // Up & Down
	    floorData.setDestinationFloor(dFloor);
	}
	
}