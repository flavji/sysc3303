Authors of this README file: Zeid Alwash and Yash Kapoor
Email: ZeidAlwash@cmail.carleton.ca, YashKapoor@cmail.carleton.ca 

This program is made up of 8 files:
	Floor.java: Floor Class that consists of the floor thread that executes first to send requests to the scheduler at the time of the request.
	Scheduler.java: Scheduler Class that consists of a thread that is used as a communication channel between the clients (i.e., floor and elevator).
	Elevator.java: Elevator Class that consists of the elevator thread that will 			   
	               execute after the scheduler sends the request.
			   Receives the request from the scheduler, processes the 
			   request, and then sends an acknowledgement to the scheduler
		         that indicates the request has been successfully serviced by
			   the elevator. There are a total of 4 elevators (a separate 
			   thread is used for each elevator).
		         There are 8 states for each elevator: 
		         0 (stationary), 1 (moving up), 2 (moving down), 3 (doors 
		         opening), 4 (doors closing), 5 (floor fault), 6 (door fault), 
                     7 (out of service)
	ElevatorGUI.java: The ElevatorGUI class represents a graphical user 
 				interface for an elevator system. It extends the JFrame  
		            class and includes components such as text fields, labels, 
				and icons for displaying elevator information and status.
	Pair.java: Pair Class that the Elevator Class uses to clearly differentiate 
		     one request from another and store Pair Objects in a queue. 
	DestinationFloor.java: DestinationFloor Class that is used by the Elevator 
				     Class to move passengers to a specific destination 
				     floor. It is used to differentiate destination floors 
				     of requests from initial floors of requests
 			           For example, request: 2, 8 -> initial floor is 2 
				     (passengers get picked up) and destination floor is 8 
				     (passengers get dropped off)
	
Installation:
-----------------
Most versions of Java will be able to run this program, but JDK 18 is recommended. 

A Java IDE such as Eclipse is recommended as well. 

If Eclipse is not already installed, use the following link that provides step-by-step instructions
on how to install it for popular operating systems:

https://www.eclipse.org/downloads/packages/installer

Usage:
----------
Step 1: Save A3G8_final_submission.zip to a folder of your choice.

Step 2: Open Eclipse and ensure the "Java Browsing" perspective is selected
	  by going to Window > Perspective > Open Perspective > Java Browsing.

Step 3: Click on File > Import from the Eclipse main menu.

Step 4: Expand General, click on "Existing Projects into Workspace", and click Next.

Step 5: Ensure that Select Archive File is checked and browse for A3G8_final_submission.zip.

Step 6: Click Finish. 

Step 7: The project should now be in Package Explorer.

Step 8: Expand project and expand src.

Step 9: Click on the project package. You need to run the Scheduler Class first, then run the Elevator Class, 
	then finally, run the Floor Class. Also, we included the GUI in the Elevator Class, so we have a GUI
	for each elevator (1, 2, 3, and 4). Hence, open the GUIs and spread them out on your screen to monitor
	which floor/state the elevator is currently on/in. You can also look at the console for each class
	(separate console for Floor, Scheduler, and Elevator since they are supposed to be running on separate computers),
	which provides a detailed description of what each Class is currently doing (e.g., floor sends request to scheduler,
	scheduler sends request to appropriate elevator, then elevator processes that request, and sends an acknowledgement
	to the scheduler, which sends that acknowledgement to the floor, indicating the request has been successfully
	processed by the elevator). 

Step 10: IMPORTANT: to see the output of the floor, scheduler, and the elevator, you must click on “Display Selected Console”
	and click on the console of the class that you want to view the output of. If you cannot find where “Display Selected Console”
	is on Eclipse, then you can click on the console and press ALT + F7 on your keyboard to bring up the menu that allows you to
	switch between the consoles of each class.

Testing:
------------
Once you are able to run the program, simply click on the SchedulerTest/ElevatorTest/ FloorTest/FloorDataTest JUnit classes
to run the JUnit tests for our system. 


Credits:
-------
- Yash Kapoor 		
- Faiaz Ahsan 		
- Zeid Alwash 		
- Fareen Lavji	  	 
- Harishan Amutheesan	
