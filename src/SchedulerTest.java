import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 
 */

/**
 * @author Fareen Lavji
 * 
 * @version 02.04.2023
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
	 * Tests the initial state of the scheduler.
	 */
	@Test
	void testInitializationScheduler() {
		assertNull(s.getFloorData());
		assertEquals(s.getSchedulerToElevatorCondition(), 0);
		assertEquals(s.getSchedulerToFloorCondition(), 0);
	}

	/**
	 * Tests the setFloorData method.
	 */
	@Test
	void testSetFloorData() {
		FloorData fd = new FloorData(5);
		s.setFloorData(fd);
		assertEquals(s.getFloorData(), fd);
	}

	/**
	 * Tests the notifySchedulerToElevator method.
	 */
	@Test
	void testNotifySchedulerToElevator() {
		s.notifySchedulerToElevator();
		assertEquals(s.getSchedulerToElevatorCondition(), 1);
	}

	/**
	 * Tests the notifySchedulerToFloor method.
	 */
	@Test
	void testNotifySchedulerToFloor() {
		s.notifySchedulerToFloor();
		;
		assertEquals(s.getSchedulerToFloorCondition(), 1);
	}

	/**
	 * Tests the setSchedulerToElevatorConditionToFalse method.
	 */
	@Test
	void testSetSchedulerToElevatorConditionToFalse() {
		s.setSchedulerToElevatorConditionToFalse();
		assertEquals(s.getSchedulerToElevatorCondition(), 0);
	}
}
