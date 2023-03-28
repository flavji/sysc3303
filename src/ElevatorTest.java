import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * JUnit Tests for Elevator class.
 * 
 * @author Fareen Lavji
 *
 *@version 02.27.2023
 */
class ElevatorTest {

	static int portNumber = 8000;
	Elevator e;
	Scheduler s;
	FloorData fd;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		e = new Elevator(portNumber++);
		s = new Scheduler();
		fd = new FloorData(5);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterEach
	void tearDown() throws Exception {
	}
	
	@Test
	void testHandleDoors() {
		System.out.println("\nTesting handling of doors (Time exceeds 10 seconds to open/close doors): \n");
		// Time exceeds 10 seconds to open/close doors
		e.setTimeHandleDoors(15000);
		e.setCurrentFloor(2);
		
		assertFalse(e.handleDoors());
		
		System.out.println("\nTesting handling of doors 2 (Time doesn't exceed 10 seconds, so successfully opens/closes doors): \n");
		// Time doesn't exceed 10 seconds, so successfully opens/closes doors
		e.setTimeHandleDoors(2500);
		e.setCurrentFloor(2);
		
		assertTrue(e.handleDoors());			
	}
	
	@Test
	void testMoveElevator() {
		System.out.println("\nTesting moving between floors (Time exceeds 20 seconds to move from one floor to another): \n");
		// Time exceeds 20 seconds to move from one floor to another
		e.setTimeBetweenFloors(22000);
		e.setCurrentFloor(2);
		
		assertFalse(e.moveElevator(5));
		
		System.out.println("\nTesting moving between floors (Time doesn't exceed 20 seconds, so successfully goes to the destination floor): \n");
		// Time doesn't exceed 20 seconds, so successfully goes to the destination floor
		e.setTimeBetweenFloors(10000);
		e.setCurrentFloor(2);
		
		assertTrue(e.moveElevator(4));
	}

	@Test
	void testGetCurrentFloor() {
		assertEquals(2, e.getCurrentFloor());
	}

	@Test
	void testSetCurrentFloor() {
		e.setCurrentFloor(5);
		assertEquals(5, e.getCurrentFloor());
	}

	@Test
	void testGetQueueSize() {
		assertEquals(0, e.getQueueSize());
	}
	
}
