import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 
 */

/**
 * @author Fareen Lavji
 * @author Harishan Amutheesan
 * 
 * @version 02.27.2023
 */
class SchedulerTest {

	Scheduler s = new Scheduler();

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
	 * Tests the addRequests method. The method should add a new FloorData object to
	 * the scheduler's list of all requests and return true.
	 */
	@Test
	void testAddRequests() {
		FloorData fd = new FloorData(10);
		s.addRequests(fd);
		assertEquals(fd, s.getAllRequests().peek());
	}
	
	/**
	 * Tests the removeRequests method. The method should remove the first FloorData
	 * object from the scheduler's list of all requests and return true if successful.
	 */
	@Test
	void testRemoveRequests() {
		FloorData fd = new FloorData(10);
		s.addRequests(fd);
		s.removeRequests();
		assertTrue(s.getAllRequests().isEmpty());
	}
	
	/**
	 * Tests the parsePacket method. The method should parse the given packet string and
	 * create new FloorData objects for each request, then add them to the scheduler's list
	 * of all requests.
	 */
	@Test
	void testParsePacket() {
		String arr = "2022-03-26 12:00:00,1,up,5/2022-03-26 12:00:01,2,down,1/";
		s.parsePacket(arr);
		assertEquals(2, s.getAllRequests().size());
	}
	/**

	This test verifies the functionality of the checkServiceableRequest method in the Scheduler class.
	It creates two floor data objects with different initial and destination floors and floor buttons and 
	adds them to the scheduler's request queue. Then, it calls the checkServiceableRequest method on each floor 
	data object and checks if the returned port numbers are correct.
	*/
	@Test
	void testCheckServiceableRequest() throws IOException{
		FloorData fd1 = new FloorData(10);
		fd1.setInitialFloor(1);
		fd1.setDestinationFloor(5);
		fd1.setFloorButton("up");
		s.addRequests(fd1);
		
		FloorData fd2 = new FloorData(10);
		fd2.setInitialFloor(2);
		fd2.setDestinationFloor(4);
		fd2.setFloorButton("down");
		s.addRequests(fd2);
		
		int portNumber = s.checkServiceableRequest(fd1);
		assertEquals(5000, portNumber);
		
		portNumber = s.checkServiceableRequest(fd2);
		assertEquals(6000, portNumber);
	}
	
}
