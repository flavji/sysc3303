import java.awt.*;
import javax.swing.*;

/**
 * The ElevatorGUI class represents a graphical user interface for an elevator system.
 * It extends the JFrame class and includes components such as text fields, labels, and icons
 * for displaying elevator information and status.
 * 
 * @author Yash Kapoor
 * @author Faiaz Ahsan
 * @author Zeid Alwash
 * @author Fareen Lavji
 * @author Harishan Amutheesan
 *
 */
public class ElevatorGUI extends JFrame{

	private static final long serialVersionUID = 1L;
	private JTextArea logArea;
    private JTextArea finishedRequests;

    /* elevator arrows*/
    
    private ImageIcon up;    
    private ImageIcon down;    
    private ImageIcon stationary;
    private ImageIcon doorFault;
    private ImageIcon floorFault;
    private ImageIcon outOfService;
    private ImageIcon doorsOpening;
    private ImageIcon doorsClosing;
    private static String elevatorNumber;

    /* window setup */
    private JTextField elevatorFloors; // Labels for the current floors of each elevator
    private JTextField elevatorStats; // Labels for the stats of each elevator
    private JLabel elevatorDirections; // Labels for elevator directions

    /**
     * Constructor for ElevatorGUI
     * Constructs an ElevatorGUI object with the given elevator number.
     *
     * @param elevatorNumber The number of the elevator.
     */
    public ElevatorGUI(String elevatorNumber) {
    	
    	//Initialize the ImageIcons
    	up =  new ImageIcon(getClass().getResource("elevator_arrow.png"));
    	down = new ImageIcon(getClass().getResource("elevatordown_arrow.png"));
    	stationary = new ImageIcon(getClass().getResource("Stationary.png"));
    	doorFault = new ImageIcon(getClass().getResource("DoorFault.jpg"));
    	floorFault = new ImageIcon(getClass().getResource("FloorFault.jpg"));
    	outOfService = new ImageIcon(getClass().getResource("outofService.jpg"));
    	doorsOpening = new ImageIcon(getClass().getResource("doorsOpening.png"));
    	doorsClosing = new ImageIcon(getClass().getResource("doorsClosing.png"));  	  	
    	
        setTitle(elevatorNumber);
        setSize(500, 400);   
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        Image upImage = up.getImage().getScaledInstance(75, 75, Image.SCALE_SMOOTH);
        up = new ImageIcon(upImage);
        
        Image downImage = down.getImage().getScaledInstance(75, 75, Image.SCALE_SMOOTH);
        down = new ImageIcon(downImage);

        Image stationaryImage = stationary.getImage().getScaledInstance(75, 75, Image.SCALE_SMOOTH);
        stationary = new ImageIcon(stationaryImage);
        
        Image doorImage = doorFault.getImage().getScaledInstance(75, 75, Image.SCALE_SMOOTH);
        doorFault = new ImageIcon(doorImage);
        
        Image floorImage = floorFault.getImage().getScaledInstance(75, 75, Image.SCALE_SMOOTH);
        floorFault = new ImageIcon(floorImage);

        Image outofService = outOfService.getImage().getScaledInstance(75, 75, Image.SCALE_SMOOTH);
        outOfService = new ImageIcon(outofService);
        
        Image doorsOpeningImage = doorsOpening.getImage().getScaledInstance(75, 75, Image.SCALE_SMOOTH);
        doorsOpening = new ImageIcon(doorsOpeningImage);
        
        Image doorsClosingImage = doorsClosing.getImage().getScaledInstance(75, 75, Image.SCALE_SMOOTH);
        doorsClosing = new ImageIcon(doorsClosingImage);
        

        JPanel displayPanel = new JPanel();
        displayPanel.setLayout(new GridLayout(2, 2));
        add(displayPanel, BorderLayout.CENTER);

 
        elevatorFloors = new JTextField();
        elevatorDirections = new JLabel();

        
        /* add floor numbers per elevator */       
        elevatorFloors = new JTextField("Floor: ");
        displayPanel.add(elevatorFloors);

        /* add direction arrows per elevator */
        elevatorDirections = new JLabel();
        elevatorDirections.setIcon(stationary);
        displayPanel.add(elevatorDirections);

        // add elevator status per elevator
        elevatorStats = new JTextField();
        elevatorStats = new JTextField("Stationary");
        displayPanel.add(elevatorStats);

        logArea = new JTextArea();
        add(logArea, BorderLayout.SOUTH);
        
        finishedRequests = new JTextArea();
    	finishedRequests.setText("Finished Requests\n");
        displayPanel.add(finishedRequests, BorderLayout.EAST);   
        
        setVisible(true);
    }
    
    /**
     * Updates the log area with the current request details.
     *
     * @param pair 	a Pair Object, the pair of initial and destination floors for the current request.
     */
    public void updateLogArea(Pair<Integer, Integer> pair) {
    	logArea.setText("Current Request: Initial Floor: " + pair.getInitialFloor() + " and Destination Floor: " + pair.getDestinationFloor());
    }
    
    /**
     * Updates the finished requests text area with the provided requests.
     *
     * @param requests	a String, the requests to be displayed in the finished requests text area.
     */
    public void updateFinishedRequests(String requests) {
    	finishedRequests.setText("Finished Requests:" + requests);
    }
    
    /**
     * Updates the elevator floor display with the current floor.
     *
     * @param currentFloor 	an int, the current floor of the elevator.
     */
    public void updateElevatorFloor(int currentFloor) {
    	elevatorFloors.setText("Floor: " + currentFloor);
    }

    /**
     * Updates the elevator status display with the provided status.
     *
     * @param status The status of the elevator.
     */
    public void updateElevatorStatus(int status) {
    	     
        if(status == 0) {
        	elevatorDirections.setIcon(stationary);
        }else if(status == 1) {
        	elevatorDirections.setIcon(up);
        	elevatorStats.setText("Status: Elevator going up");        	
        }else if(status == 2) {
        	elevatorDirections.setIcon(down);
        	elevatorStats.setText("Status: Elevator going down");
        }else if(status == 3) {
        	elevatorDirections.setIcon(doorsOpening);
        	elevatorStats.setText("Status: Elevator doors opening");
        	
        }else if(status == 4) {  	
        	elevatorDirections.setIcon(doorsClosing);
        	elevatorStats.setText("Status: Elevator doors closing");
        	
        }else if(status == 5) {
        	elevatorDirections.setIcon(floorFault);
        	elevatorStats.setText("Status: Elevator floor fault");
        	
        }else if(status == 6) {
        	elevatorDirections.setIcon(doorFault);
        	elevatorStats.setText("Status: Elevator door fault");
        	
        }else if(status == 7) {
        	elevatorDirections.setIcon(outOfService);
        	elevatorStats.setText("Status: Elevator is out of service");
        	
        } else if (status == 8) {
        	elevatorStats.setText("Status: Elevator finished request!");
        }
        
    }   
 
    /**
     * Method to close additional windows
     */
    public void closeGUI() { 	
    	dispose();
    }

    /**
     * Logs an error message to the log area.
     *
     * @param errorMessage 	a String, the error message to be logged.
     */
    public void logError(String errorMessage) {
        logArea.append("Error: " + errorMessage + "\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ElevatorGUI(elevatorNumber);
        });
    }
}