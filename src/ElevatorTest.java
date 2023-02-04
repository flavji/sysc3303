import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * JUnit Tests for Elevator class.
 * 
 * @author Fareen Lavji
 *
 *@version 02.04.2023
 */
class ElevatorTest {

	Scheduler s = new Scheduler();
	Elevator e = new Elevator(s);
	
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

}
