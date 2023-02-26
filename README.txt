Author of this README file: Zeid Alwash 
Email: ZeidAlwash@cmail.carleton.ca

Description:
------------
There are three subsystems in this project: Floor, Elevator, and Scheduler.
The scheduler is used as a communication channel between the clients (i.e., floor and elevator).
The data in the CSV file gets passed from the floor -> scheduler -> elevator -> scheduler -> floor. 
Specifically, the Floor subsystem reads the CSV file and sends the values to the scheduler.
The scheduler stores those values in a queue AllFloorRequests, the elevator determines which requests are serviceable based on the first request and notifies the scheduler to store those requests in another queue serviceableRequests. The elevator then iterates through both queues until they're empty. The  execution of this program is simple. The Floor subsystem executes first and prints "Starting at Floor". Then, The Floor subsystem sends the data that it reads from the CSV file to the scheduler class. The scheduler notifies the elevator, which then begins executing and prints out "Elevator Success", along with the data in the CSV file. Then, the elevator sends a request back to the scheduler that it is done. Hence, the scheduler sends a request back to the floor, telling it to start executing again. Once the floor starts executing again, it prints out "Ending at Floor", along with the data in the CSV file. 

This program is made up of 5 files:

	Main.java: A class that consists of the main method. 
		     It is used to control the program and start the floor, elevator, and scheduler threads.
	Floor.java: A class that consists of the floor thread that will execute first to send a request to
			the scheduler. Also, it is responsible for reading the CSV file and setting the FloorData
			Object, which notifies the scheduler to send a request to the elevator.
	Elevator.java: A class that consists of the elevator thread that will execute after the scheduler
			   sends the requests.
	Scheduler.java: A class that consists of the scheduler thread (i.e., server) that is used to as a
			    a communication channel between the clients (i.e., floor and elevator).
	FloorData.java: A class that stores the data defined in the CSV file (i.e., time, initial floor
			    the elevator is at, the direction the elevator is going in (up or down), and the final
                      floor the elevator arrives at). 

Installation:
-------------
Most versions of Java will be able to run this program, but JDK 18 is recommended. 

A Java IDE such as Eclipse is recommended as well. 

If Eclipse is not already installed, use the following link that provides step-by-step instructions
on how to install it for popular operating systems:

https://www.eclipse.org/downloads/packages/installer

Usage:
-------
Step 1: Save A3G8_milestone_2.zip to a folder of your choice.

Step 2: Open Eclipse and ensure the "Java Browsing" perspective is selected
	  by going to Window > Perspective > Open Perspective > Java Browsing.

Step 3: Click on File > Import from the Eclipse main menu.

Step 4: Expand General, click on "Existing Projects into Workspace", and click Next.

Step 5: Ensure that Select Archive File is checked and browse for A3G8_milestone_2.zip.

Step 6: Click Finish. 

Step 7: The project should now be in Package Explorer.

Step 8: Expand project and expand src.

Step 9: Right click on the project package, click on "Run as", then select 
	  "1 Java Application". The program should now run and you should
	  see text being printed to the console that indicates whether the floor 
	  or elevator is running. 

To find the sequence, UML diagram and State machine diagrams that we constructed for this assignment,
extract all files from A3G8_milestone_2.zip. there should be elevatorSimulation_UML.png, elevatorSimulation_sequenceDiagram.png, Elevator_State_Machine_Diagram and Scheduler_State_Machine_Diagram in the root folder. 

Credits:
-------
- Yash Kapoor 		(Worked on code, refactoring code, UML)
- Faiaz Ahsan 		(Worked on code, rough draft of UML, and Sequence Diagram)
- Zeid Alwash 		(Worked on code and rough draft of UML and README)
- Fareen Lavji	  	(Worked on refactoring code, JUnit Tests, UML, setting up git repository,
				initial deployment to GitHub, and proofreading README.txt) 
- Harishan Amutheesan	(Worked on code and rough draft of UML)
