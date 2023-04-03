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
	 * Tests all the Scheuduler methods. 
	 */
	@Test
	void testAddRequests() {
		
		//addRequest test
		FloorData fd = new FloorData(10);
		s.addRequests(fd);
		assertEquals(fd, s.getAllRequests().peek());
		
		
		//remove
		FloorData fd4 = new FloorData(10);
		s.addRequests(fd4);
		s.removeRequests();
		s.removeRequests();
		assertTrue(s.getAllRequests().isEmpty());
		
		//parsePacket test
		String arr = "2022-03-26 12:00:00,1,up,5/2022-03-26 12:00:01,2,down,1/";
		s.parsePacket(arr);
		assertEquals(2, s.getAllRequests().size());
		
		//setFloorData test
		try {
			fd.setInitialFloor(1);
			fd.setDestinationFloor(5);
			fd.setFloorButton("down");
			s.addRequests(fd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		//test
		FloorData fd2 = new FloorData(10);
		try {
			fd2.setInitialFloor(2);
			fd2.setDestinationFloor(4);
			fd2.setFloorButton("down");
			s.addRequests(fd2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		int portNumber = s.checkServiceableRequest(fd);
		assertEquals(5000, portNumber);
		
		portNumber = s.checkServiceableRequest(fd2);
		assertEquals(6000, portNumber);
		
		
	}

	
}
