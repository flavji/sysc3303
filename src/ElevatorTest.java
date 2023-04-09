import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * JUnit tests for Elevator class.
 * 
 * @author Yash Kapoor
 * @author Faiaz Ahsan
 * @author Zeid Alwash
 * @author Fareen Lavji
 * @author Harishan Amutheesan
 * @version 02.27.2023
 */
class ElevatorTest {

    static int portNumber = 8000;
    Elevator e1, e2, e3, e4;

    /**
     * Set up method initializes Elevator objects before each test.
     * @throws java.lang.Exception
     */
    @BeforeEach
    void setUp() throws Exception {
        e1 = new Elevator(portNumber++, "Elevator 1");
        e2 = new Elevator(portNumber++, "Elevator 2");
        e3 = new Elevator(portNumber++, "Elevator 3");
        e4 = new Elevator(portNumber++, "Elevator 4");
    }

    /**
     * Tear down method cleans up after each test.
     * @throws java.lang.Exception
     */
    @AfterEach
    void tearDown() throws Exception {
    }

    /**
     * Test to verify if the elevator handles doors correctly when time exceeds 10 seconds.
     */
    @Test
    void testHandleDoorsFailure() {
        e1.setTimeHandleDoors(15000);
        e1.setCurrentFloor(2);
        assertFalse(e1.handleDoors());
    }

    /**
     * Test to verify if the elevator handles doors correctly when time is within 10 seconds.
     */
    @Test
    void testHandleDoorsSuccess() {
        e1.setTimeHandleDoors(2500);
        e1.setCurrentFloor(2);
        assertTrue(e1.handleDoors());
    }

    /**
     * Test to verify if the elevator moves between floors correctly.
     */
    @Test
    void testMoveElevator() {
        // Test moving between floors when time exceeds 20 seconds
        e1.setCurrentFloor(2);
        e1.setTimeBetweenFloors(21000);
        assertFalse(e1.moveElevator(5, true));

        // Test moving between floors when time doesn't exceed 20 seconds
        e1.setTimeBetweenFloors(10000);
        e1.setCurrentFloor(2);
        assertTrue(e1.moveElevator(4, true));

        // Test moving to the same floor
        e1.setCurrentFloor(2);
        assertTrue(e1.moveElevator(2, true));

        // Test moving to a higher floor
        e1.setCurrentFloor(2);
        assertTrue(e1.moveElevator(5, true));

        // Test moving to a lower floor
        e1.setCurrentFloor(5);
        assertTrue(e1.moveElevator(2, true));
    }

    /**
     * Test to verify if the elevator returns the correct current floor.
     */
    @Test
    void testGetCurrentFloor() {
        assertEquals(2, e1.getCurrentFloor());
    }

    /**
     * Test to verify if the elevator sets the current floor correctly.
     */
    @Test
    void testSetCurrentFloor() {
        e1.setCurrentFloor(5);
        assertEquals(5, e1.getCurrentFloor());
    }

    /**
     * Test to verify if multiple elevators handle doors correctly.
     */
    @Test
    void testHandleDoorsMultipleElevators() {
        // Test doors for Elevator 1
        e1.setTimeHandleDoors(2500);
        e1.setCurrentFloor(1);
        assertTrue(e1.handleDoors());

        // Test doors for Elevator 2
	    e2.setTimeHandleDoors(5000);
	    e2.setCurrentFloor(3);
	    assertTrue(e2.handleDoors());
	    // Test doors for Elevator 3
	    e3.setTimeHandleDoors(15000);
	    e3.setCurrentFloor(5);
	    assertFalse(e3.handleDoors());

	    // Test doors for Elevator 4
	    e4.setTimeHandleDoors(7000);
	    e4.setCurrentFloor(2);
	    assertTrue(e4.handleDoors());
	}

	/**
	 * Test to verify if multiple elevators move between floors correctly.
	 */
	@Test
	void testMoveElevatorMultipleElevators() {
	    // Test moving Elevator 1 to a higher floor
	    e1.setTimeBetweenFloors(5000);
	    e1.setCurrentFloor(1);
	    assertTrue(e1.moveElevator(4, true));

	    // Test moving Elevator 2 to a lower floor
	    e2.setTimeBetweenFloors(10000);
	    e2.setCurrentFloor(4);
	    assertTrue(e2.moveElevator(2, true));

	    // Test moving Elevator 3 to the same floor
	    e3.setTimeBetweenFloors(2000);
	    e3.setCurrentFloor(5);
	    assertTrue(e3.moveElevator(5, true));

	    // Test moving Elevator 4 to a higher floor
	    e4.setTimeBetweenFloors(15000);
	    e4.setCurrentFloor(3);
	    assertTrue(e4.moveElevator(5, true));

	    // Test moving Elevator 1 with time exceeding 20 seconds
	    e1.setTimeBetweenFloors(21000);
	    e1.setCurrentFloor(1);
	    assertFalse(e1.moveElevator(4, true));
	}

	/**
	 * Test to verify if multiple elevators return the correct current floor.
	 */
	@Test
	void testGetCurrentFloorMultipleElevators() {
	    e1.setCurrentFloor(2);
	    e2.setCurrentFloor(3);
	    e3.setCurrentFloor(5);
	    e4.setCurrentFloor(1);

	    assertEquals(2, e1.getCurrentFloor());
	    assertEquals(3, e2.getCurrentFloor());
	    assertEquals(5, e3.getCurrentFloor());
	    assertEquals(1, e4.getCurrentFloor());
	}

	/**
	 * Test to verify if multiple elevators set the current floor correctly.
	 */
	@Test
	void testSetCurrentFloorMultipleElevators() {
	    e1.setCurrentFloor(1);
	    e2.setCurrentFloor(2);
	    e3.setCurrentFloor(5);
	    e4.setCurrentFloor(4);

	    assertEquals(1, e1.getCurrentFloor());
	    assertEquals(2, e2.getCurrentFloor());
	    assertEquals(5, e3.getCurrentFloor());
	    assertEquals(4, e4.getCurrentFloor());
	}	
}