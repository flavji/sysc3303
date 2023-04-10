import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.File;
import javax.swing.*;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class ElevatorGUI extends JFrame{
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
        elevatorFloors = new JTextField("2");
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
    
    public void updateLogArea(Pair<Integer, Integer> pair) {
    	logArea.setText("Current Request: Initial Floor: " + pair.getInitialFloor() + " and Destination Floor: " + pair.getDestinationFloor());
    }
    
    public void updateFinishedRequests(String requests) {
    	finishedRequests.setText(requests);
    }
    
    public void updateElevatorFloor(int currentFloor) {
    	elevatorFloors.setText("Floor: " + currentFloor);
    }

    public void updateElevatorStatus(int status) {
    	     
        //later change this to Icon
        if(status == 0) {
        	elevatorDirections.setIcon(stationary);
        	//elevatorDirections.setText("Direction: " + STATIONARY);
        }else if(status == 1) {
        	elevatorDirections.setIcon(up);
        	elevatorStats.setText("Status: Elevator going up");
        	//elevatorDirections.setText("Direction: " + UP);
        	
        }else if(status == 2) {
        	elevatorDirections.setIcon(down);
        	elevatorStats.setText("Status: Elevator going down");
        	//elevatorDirections.setText("Direction: " + DOWN);
        }else if(status == 3) {
        	elevatorDirections.setIcon(doorsOpening);
        	//elevatorDirections.setText("Direction: " + STATIONARY);
        	elevatorStats.setText("Status: Elevator doors opening");
        	
        }else if(status == 4) {  	
        	elevatorDirections.setIcon(doorsClosing);
        	//elevatorDirections.setText("Direction: " + STATIONARY);
        	elevatorStats.setText("Status: Elevator doors closing");
        	
        }else if(status == 5) {
        	elevatorDirections.setIcon(floorFault);
        	//elevatorDirections.setText("Direction: " + STATIONARY);
        	elevatorStats.setText("Status: Elevator floor fault");
        	
        }else if(status == 6) {
        	elevatorDirections.setIcon(doorFault);
        	//elevatorDirections.setText("Direction: " + STATIONARY);
        	elevatorStats.setText("Status: Elevator door fault");
        	
        }else if(status == 7) {
        	elevatorDirections.setIcon(outOfService);
        	//elevatorDirections.setText("Direction: " + STATIONARY);
        	elevatorStats.setText("Status: Elevator is out of service");
        	
        } else if (status == 8) {
        	elevatorStats.setText("Status: Elevator finished request!");
        }
        
    }
    
    
 
    //Method to close additional windows
    public void closeGUI() { 	
    	dispose();
    }

    public void logError(String errorMessage) {
        logArea.append("Error: " + errorMessage + "\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ElevatorGUI(elevatorNumber);
        });
    }
}