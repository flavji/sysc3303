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

	Scheduler s = new Scheduler();
	Elevator e = new Elevator(s);
	FloorData fd = new FloorData(5);
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterEach
	void tearDown() throws Exception {
	}

	/**
	 * Tests the notifyElevatorToScheduler method.
	 */
	@Test
	void testNotifyElevatorToScheduler() {
		assertEquals(e.notifyElevatorToScheduler(), 1);
		assertEquals(s.getSchedulerToElevatorCondition(), 0);
	}
	
	/**
	 * Tests the executeRequest method if the elevator is going up.
	 * @throws IOException 
	 */
	@Test
	void testExecuteRequestUp() throws IOException {
		fd.setInitialFloor(0);
		fd.setDestinationFloor(5);
		assertTrue(e.executeRequest(fd));
	}
	
	/**
	 * Tests the executeRequest method if the elevator is going down after going up.
	 * @throws IOException 
	 */
	@Test
	void testExecuteRequestDown() throws IOException {		
		fd.setInitialFloor(3);
		fd.setDestinationFloor(1);
		assertTrue(e.executeRequest(fd));
	}
	
	/**
	 * Tests the executeRequest method if the elevator is going down after going up.
	 * @throws IOException 
	 */
	@Test
	void testExecuteRequestUpDown() throws IOException {
		fd.setInitialFloor(0);
		fd.setDestinationFloor(5);
		e.executeRequest(fd);
		
		fd.setInitialFloor(5);
		fd.setDestinationFloor(3);
		assertTrue(e.executeRequest(fd));
	}
	
	/**
	 * Tests the executeRequest method if the elevator is going down after going up.
	 * @throws IOException 
	 */
	@Test
	void testExecuteRequestDownUp() throws IOException {
		fd.setInitialFloor(2);
		fd.setDestinationFloor(1);
		e.executeRequest(fd);
		
		fd.setInitialFloor(1);
		fd.setDestinationFloor(3);
		assertTrue(e.executeRequest(fd));
	}
	
	/**
	 * Tests the executeRequest method if the elevator cannot go anywhere.
	 * @throws IOException 
	 */
	@Test
	void testExecuteRequestSame() throws IOException {
		fd.setInitialFloor(0);
		fd.setDestinationFloor(2);
		assertFalse(e.executeRequest(fd));
	}
}
