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
	
	/**
	 * Constructor for Floor that initializes a scheduler and floor data.
	 * 
	 * @param s	A Scheduler Object, the server that is used to communicate between the two clients (i.e., floor and elevator).
	 */
	public Floor(Scheduler s, int floors) {
		this.scheduler = s;
		this.floorData = new FloorData(floors); //todo: setting default floors to 10 floors
	}
	
	/**
	 * Reads the floorRequests.csv file that contains instructions for the elevator to execute.
	 * Sets the floor data, and notifies the scheduler.
	 */ 
	public void unwrapData() {
		try 
	    {
			// parsing a CSV file into BufferedReader class constructor
			File csvFile = new File("./floorRequests.csv");
		    BufferedReader br = new BufferedReader(new FileReader(csvFile));

		    String line = "";
		    while ((line = br.readLine()) != null)   //returns a Boolean value
		    {
			    
		    	String[] elevatorData = new String[4];
			    elevatorData = line.split(",");
			
			    String start_date = elevatorData[0];
			    DateFormat formatter = new SimpleDateFormat("hh:mm:ss");
			    Date date = (Date) formatter.parse(start_date);
			   
			    floorData.setTime(date);
			    floorData.setInitialFloor(Integer.parseInt(elevatorData[1]));
			    floorData.setFloorButton(elevatorData[2]); // Up & Down
			    floorData.setDestinationFloor(Integer.parseInt(elevatorData[3]));

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
	
}