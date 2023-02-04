/**
 * @author Yash Kapoor
 * @author Faiaz Ahsan
 * @author Zeid Alwash
 * @author Flo Lavji
 * @author Harishan Amutheesan
 */

import java.util.Date;
import java.io.*;
import java.text.*;

/**
 * Floor Class that consists of the floor thread that will execute first to send a request to the scheduler
 *
 */
public class Floor implements Runnable {
	private Scheduler scheduler;
	private FloorData floorData; 
	
	/**
	 * Constructor for Floor
	 * @param s		a Scheduler Object, the server that is used to communicate between the two clients (i.e., floor and elevator)
	 */
	public Floor(Scheduler s) {
		this.scheduler = s;
		this.floorData = new FloorData();
	}
	
	/**
	 * Reads the CSV file, sets the floor data, and notifies the scheduler
	 */ 
	public void unwrapData() {
		scheduler.notifyFloorToScheduler();
		try 
	    {
			// parsing a CSV file into BufferedReader class constructor
			File csvFile = new File("C:\\Users\\yashk\\Desktop\\Assignment1\\Iteration1Yash\\Data.csv");
		    BufferedReader br = new BufferedReader(new FileReader(csvFile));

		    String line = "";
		    while ((line = br.readLine()) != null)   //returns a Boolean value
		    {
			    
		    	String[] ElevatorData = new String[4];
			    ElevatorData = line.split(",");
			
			    String start_date = ElevatorData[0];
			    DateFormat formatter = new SimpleDateFormat("hh:mm:ss");
			    Date date = (Date) formatter.parse(start_date);
			   
			    floorData.setTime(date); // Time Format
			    floorData.setInitialFloor(Integer.parseInt(ElevatorData[1])); // Int
			    floorData.setFloorButton(ElevatorData[2]);// Up & Down
			    floorData.setDestinationFloor(Integer.parseInt(ElevatorData[3])); // Int

		    }
		    br.close();
	    }
	    catch (IOException e) {} 
		catch (ParseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Used to run the Floor thread
	 */
	@Override
	public void run() {
		System.out.println("Starting at Floor");
		unwrapData();
		while (scheduler.getFloorCondition() == 1 || scheduler.getSchedulerToFloorCondition() == 1) {
			
			scheduler.setFloorData(floorData);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
			if(scheduler.getSchedulerToFloorCondition() != 1 && scheduler.getSchedulerToElevatorCondition() != 1) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {}
			} else {
				System.out.println("Ending at Floor: " + "Initial Floor: " + floorData.getInitialFloor() + " Destination Floor: " + floorData.getDestinationFloor() + " Floor Button: " + floorData.getFloorButton() + " Time: " + floorData.getTime());
				break;
			}
			
		}
	}
	
}

// 