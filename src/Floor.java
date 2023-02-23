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
	private String floorRequests;
	
	/**
	 * Constructor for Floor that initializes a scheduler and floor data.
	 * 
	 * @param s	A Scheduler Object, the server that is used to communicate between the two clients (i.e., floor and elevator).
	 */
	public Floor(Scheduler s, String floorRequests) {
		this.scheduler = s;
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
			    // parses out the date, so the date is in this format: hh:mm:ss
			   
			    setFloorData(date,
			    		Integer.parseInt(elevatorData[1]),
			    		elevatorData[2],
			    		Integer.parseInt(elevatorData[3]));
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
        System.out.println("Starting at Floor\n");
        unwrapData();
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}

        while(true) {
            if(scheduler.getSchedulerToFloorCondition() == 1) {
                System.out.println("\n\tArrived At Floor:" +
	                		"\n\t\tInitial Floor: " + ((FloorData) scheduler.getServiceableRequests().element()).getInitialFloor() +
                		" Destination Floor: " + ((FloorData) scheduler.getServiceableRequests().element()).getDestinationFloor() +
                		" Floor Button: " + ((FloorData)scheduler.getServiceableRequests().element()).getFloorButton() +
                		" Time: " + ((FloorData)scheduler.getServiceableRequests().element()).getTime() + "\n\n");
                
                
                
                // removes all the requests that have already been serviced from the allFloorRequests queue
                scheduler.getAllRequests().removeAll(scheduler.getServiceableRequests());
                
                // remove the request from the head of the serviceableRequests queue
                // since it has already been serviced
                scheduler.removeServiceableRequests();
                
                // prevent the floor from executing multiple times
                scheduler.setSchedulerToFloorConditionToFalse();
                
                if(scheduler.getAllRequests().isEmpty() && scheduler.getServiceableRequests().isEmpty()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {}
                	System.out.println("All requests were processed. The simulation has ended.");
                	System.exit(1);	
                }
               
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}
            }
        }    
    }
	
	/**
	 * Sets the FloorData Object
	 * @param date		a Date object, the time of the request 
	 * @param iFloor	an int, the initial floor the elevator is at
	 * @param direction		a String, the direction the elevator is going in (up or down)
	 * @param dFloor	an int, the destination floor the elevator needs to go to 
	 * @throws IOException	
	 */
	private void setFloorData(Date date, int iFloor, String direction, int dFloor) throws IOException {
		FloorData fd = new FloorData(10);    // setting default floors to 10
		
		fd.setTime(date);
		fd.setInitialFloor(iFloor);
		fd.setFloorButton(direction); // Up & Down
		fd.setDestinationFloor(dFloor);
		
		// adding all the requests to the queue that are in the CSV file
	    scheduler.addRequests(fd);
	    System.out.println("Scheduler: A request has been added to the queue");
	}
}