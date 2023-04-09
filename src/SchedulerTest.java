import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * JUnit tests for Scheduler class.
 * 
 * @author Yash Kapoor
 * @author Faiaz Ahsan
 * @author Zeid Alwash
 * @author Fareen Lavji
 * @author Harishan Amutheesan
 * 
 * @version 02.27.2023
 */
class SchedulerTest {

    Scheduler s = new Scheduler();

    /**
     * Sets up the test fixture.
     * 
     * @throws java.lang.Exception
     */
    @BeforeEach
    void setUp() throws Exception {
    }

    /**
     * Tears down the test fixture.
     * 
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
        // Test adding a request
        FloorData fd = new FloorData(10);
        s.addRequests(fd);
        assertEquals(fd, s.getAllRequests().peek());

        // Test removing a request
        s.removeRequests();
        assertTrue(s.getAllRequests().isEmpty());

        // Test parsing a packet
        String arr = "2022-03-26 12:00:00,1,up,5/2022-03-26 12:00:01,2,down,1/";
        s.parsePacket(arr);
        assertEquals(2, s.getAllRequests().size());

        // Test checking serviceable requests
        try {
            fd.setInitialFloor(1);
            fd.setDestinationFloor(5);
            fd.setFloorButton("down");
            s.addRequests(fd);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FloorData fd2 = new FloorData(10);
        try {
            fd2.setInitialFloor(2);
            fd2.setDestinationFloor(4);
            fd2.setFloorButton("down");
            s.addRequests(fd2);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FloorData fd3 = new FloorData(10);
        try {
            fd3.setInitialFloor(3);
            fd3.setDestinationFloor(8);
            fd3.setFloorButton("up");
            s.addRequests(fd3);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FloorData fd4 = new FloorData(10);
        try {
            fd4.setInitialFloor(7);
            fd4.setDestinationFloor(1);
            fd4.setFloorButton("down");
            s.addRequests(fd4);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Test verifying serviceable requests
        int portNumber = s.checkServiceableRequest(fd);
        assertEquals(5000, portNumber);

        portNumber = s.checkServiceableRequest(fd2);
        assertEquals(6000, portNumber);

        portNumber = s.checkServiceableRequest(fd3);
        assertEquals(7000, portNumber);

        portNumber = s.checkServiceableRequest(fd4);
        assertEquals(8000, portNumber);
    }
}