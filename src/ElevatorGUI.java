import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.File;
import javax.swing.*;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class ElevatorGUI extends JFrame{
    private JTextArea logArea;

    /* elevator arrows*/
    
    private ImageIcon UP;    
    private ImageIcon DOWN;    
    private ImageIcon STATIONARY;
    private ImageIcon DOORFAULT;
    private ImageIcon FLOORFAULT;
    private ImageIcon OUTOFSERVICE;
    private ImageIcon DOORSOPENING;
    private ImageIcon DOORSCLOSING;
    
      
    private ImageIcon currentIcon;
    private static String elevatorNumber;
    
   



    /* window setup */
    private JTextField elevatorFloors; // Labels for the current floors of each elevator
    private JTextField elevatorStats; // Labels for the stats of each elevator
    private JLabel elevatorDirections; // Labels for elevator directions

    public ElevatorGUI(String elevatorNumber) {
    	
    	//Initialize the ImageIcons
    	UP =  new ImageIcon(getClass().getResource("elevator_arrow.png"));
    	DOWN = new ImageIcon(getClass().getResource("elevatordown_arrow.png"));
    	STATIONARY = new ImageIcon(getClass().getResource("Stationary.png"));
    	DOORFAULT = new ImageIcon(getClass().getResource("DoorFault.jpg"));
    	FLOORFAULT = new ImageIcon(getClass().getResource("FloorFault.jpg"));
    	OUTOFSERVICE = new ImageIcon(getClass().getResource("outofService.jpg"));
    	DOORSOPENING = new ImageIcon(getClass().getResource("doorsOpening.png"));
    	DOORSCLOSING = new ImageIcon(getClass().getResource("doorsClosing.png"));
    	
    	  	
    	
        setTitle(elevatorNumber);
        setSize(400, 300);   
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        Image upImage = UP.getImage().getScaledInstance(75, 75, Image.SCALE_SMOOTH);
        UP = new ImageIcon(upImage);
        
        Image downImage = DOWN.getImage().getScaledInstance(75, 75, Image.SCALE_SMOOTH);
        DOWN = new ImageIcon(downImage);

        Image stationaryImage = STATIONARY.getImage().getScaledInstance(75, 75, Image.SCALE_SMOOTH);
        STATIONARY = new ImageIcon(stationaryImage);
        
        Image doorImage = DOORFAULT.getImage().getScaledInstance(75, 75, Image.SCALE_SMOOTH);
        DOORFAULT = new ImageIcon(doorImage);
        
        Image floorFault = FLOORFAULT.getImage().getScaledInstance(75, 75, Image.SCALE_SMOOTH);
        FLOORFAULT = new ImageIcon(floorFault);

        Image outofService = OUTOFSERVICE.getImage().getScaledInstance(75, 75, Image.SCALE_SMOOTH);
        OUTOFSERVICE = new ImageIcon(outofService);
        
        Image doorsOpening = DOORSOPENING.getImage().getScaledInstance(75, 75, Image.SCALE_SMOOTH);
        DOORSOPENING = new ImageIcon(doorsOpening);
        
        Image doorsClosing = DOORSCLOSING.getImage().getScaledInstance(75, 75, Image.SCALE_SMOOTH);
        DOORSCLOSING = new ImageIcon(doorsClosing);
        

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
        elevatorDirections.setIcon(STATIONARY);
        displayPanel.add(elevatorDirections);

        // add elevator status per elevator
        elevatorStats = new JTextField();
        elevatorStats = new JTextField("Stationary");
        displayPanel.add(elevatorStats);
       
//        logArea = new JTextArea();
//        JScrollPane logScrollPane = new JScrollPane(logArea);
//        add(logScrollPane, BorderLayout.SOUTH);

        setVisible(true);
    }
    
    
    public void updateElevatorFloor(int currentFloor) {
    	elevatorFloors.setText("Floor: " + currentFloor);
    }

    public void updateElevatorStatus(int status) {
    	     
        //later change this to Icon
        if(status == 0) {
        	elevatorDirections.setIcon(STATIONARY);
        	//elevatorDirections.setText("Direction: " + STATIONARY);
        }else if(status == 1) {
        	elevatorDirections.setIcon(UP);
        	elevatorStats.setText("Status: Elevator going up");
        	//elevatorDirections.setText("Direction: " + UP);
        	
        }else if(status == 2) {
        	elevatorDirections.setIcon(DOWN);
        	elevatorStats.setText("Status: Elevator going down");
        	//elevatorDirections.setText("Direction: " + DOWN);
        }else if(status == 3) {
        	elevatorDirections.setIcon(DOORSOPENING);
        	//elevatorDirections.setText("Direction: " + STATIONARY);
        	elevatorStats.setText("Status: Elevator doors opening");
        	
        }else if(status == 4) {  	
        	elevatorDirections.setIcon(DOORSCLOSING);
        	//elevatorDirections.setText("Direction: " + STATIONARY);
        	elevatorStats.setText("Status: Elevator doors closing");
        	
        }else if(status == 5) {
        	elevatorDirections.setIcon(FLOORFAULT);
        	//elevatorDirections.setText("Direction: " + STATIONARY);
        	elevatorStats.setText("Status: Elevator floor fault");
        	
        }else if(status == 6) {
        	elevatorDirections.setIcon(DOORFAULT);
        	//elevatorDirections.setText("Direction: " + STATIONARY);
        	elevatorStats.setText("Status: Elevator door fault");
        	
        }else if(status == 7) {
        	elevatorDirections.setIcon(OUTOFSERVICE);
        	//elevatorDirections.setText("Direction: " + STATIONARY);
        	elevatorStats.setText("Status: Elevator is out of service");
        	
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