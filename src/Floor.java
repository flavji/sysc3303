import java.util.Arrays;
import java.util.Date;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
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
 * @version 02.27.2023
 */
public class Floor implements Runnable {
	private Scheduler scheduler;
	private String floorRequests;
	int counter;
	DatagramPacket sendPacket, receivePacket, receiveAckPacket;
	DatagramSocket Socket, socket2;
	
	/**
	 * Constructor for Floor that initializes a scheduler and floor data.
	 * 
	 * @param s	A Scheduler Object, the server that is used to communicate between the two clients (i.e., floor and elevator).
	 */
	public Floor(Scheduler s, String floorRequests) {
		this.scheduler = s;
		this.floorRequests = floorRequests;
		this.counter = 0;
		
		 try {
	         Socket = new DatagramSocket();
	      } catch (SocketException se) {
	         se.printStackTrace();
	         System.exit(1);
	      }
		 
		 try {
	         socket2 = new DatagramSocket(26);
	      } catch (SocketException se) {
	         se.printStackTrace();
	         System.exit(1);
	      }
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
			    
			    //counter gets updated for every request
			    counter++;
			    
		    }
	    
		    br.close();
	    }
	    catch (IOException e) {} 
		catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public void sendReceive() {
		for(int i = 0; i < counter; i++) {
			byte floorData[] = new byte[100];
			byte floorReply[] = new byte[100];
			
		     //form packet to send to scheduler
		      //floorData = method(); call an internal method to form the array of bytes
		      try {
		      sendPacket = new DatagramPacket(floorData, floorData.length, InetAddress.getLocalHost(), 23);
		      }catch(UnknownHostException e) {
			         e.printStackTrace();
			         System.exit(1);
			      }
		      
		      
		      //send the packet to the scheduler
		      try {
		    	  Socket.send(sendPacket);
		      } catch (IOException e) {
		         e.printStackTrace();
		         System.exit(1);
		      }
		      
		      //print the sent packet
		      System.out.println("Floor: Data Request Packet sent to scheduler:");
		      System.out.println("From host: " + sendPacket.getAddress());
		      System.out.println("Host port: " + sendPacket.getPort());
		      System.out.println("Length: " + sendPacket.getLength());
		      System.out.println("Containing: " + new String(sendPacket.getData(),0,sendPacket.getLength()));
		      System.out.println("Containing in bytes: " + Arrays.toString(sendPacket.getData()));
		      
		      
		      //form packet to receive reply from scheduler
		      receivePacket = new DatagramPacket(floorReply, floorReply.length);
		      
		      //receive the reply packet from the scheduler
		      try {        
		         System.out.println("Waiting..."); // so we know we're waiting
		         Socket.receive(receivePacket);
		        
		      } catch (IOException e) {
		         System.out.print("IO Exception: likely:");
		         System.out.println("Receive Socket Timed Out.\n" + e);
		         e.printStackTrace();
		         System.exit(1);
		      }
		      
		      //print the reply packet received from the scheduler
		      System.out.println("Floor: Reply Packet received from scheduler:");
		      System.out.println("From host: " + receivePacket.getAddress());
		      System.out.println("Host port: " + receivePacket.getPort());
		      System.out.println("Length: " + receivePacket.getLength());
		      System.out.println("Containing: " + new String(receivePacket.getData(),0,receivePacket.getLength()));
		      System.out.println("Containing in bytes: " + Arrays.toString(receivePacket.getData()));
		      
		      
		      //form packet to receive Ack packet
				byte floorAck[] = new byte[100];
			    receiveAckPacket = new DatagramPacket(floorAck, floorAck.length);
		      
		      
		      //receive the Ack packet from the scheduler
		      try {        
		         System.out.println("Waiting..."); // so we know we're waiting
		         Socket.receive(receiveAckPacket);
		        
		      } catch (IOException e) {
		         System.out.print("IO Exception: likely:");
		         System.out.println("Receive Socket Timed Out.\n" + e);
		         e.printStackTrace();
		         System.exit(1);
		      }
		      
		      //print the reply packet received from the scheduler
		      System.out.println("Floor: Reply Packet received from scheduler:");
		      System.out.println("From host: " + receiveAckPacket.getAddress());
		      System.out.println("Host port: " + receiveAckPacket.getPort());
		      System.out.println("Length: " + receiveAckPacket.getLength());
		      System.out.println("Containing: " + new String(receiveAckPacket.getData(),0,receiveAckPacket.getLength()));
		      System.out.println("Containing in bytes: " + Arrays.toString(receiveAckPacket.getData()));
		      		      		      		      		      		      	
		}
		//close the sockets once done
		Socket.close();
		
		
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
		fd.setFloorButton(direction.toLowerCase()); // Up & Down
		fd.setDestinationFloor(dFloor);
		
		fd.toString();
		
		// adding all the requests to the queue that are in the CSV file
	    scheduler.addRequests(fd);
	    System.out.println("Scheduler: A request has been added to the queue");
	}
}