import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * JUnit Tests for FloorData Class.
 * 
 * @author Fareen Lavji
 * 
 * @version Final Project Submission
 * Tests were written in Iteration 1 - No need to be updated
 */
class FloorDataTest {

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
	 * Test set and get methods for initialFloor property.
	 * @throws IOException
	 */
	@Test
	void testInitialFloor() throws IOException {
		fd.setInitialFloor(1);
		assertEquals(1, fd.getInitialFloor());
	}
	
	/**
	 * Test out of range handling for initialFloor property.
	 */
	@Test
	public void testInitialFloorOutOfRange() {
	    @SuppressWarnings("unused")
		Throwable exception = assertThrows(IOException.class,
	            ()->{fd.setInitialFloor(-1);} );
	}
	
	/**
	 * Test set and get methods for destinationFloor property.
	 * @throws IOException
	 */
	@Test
	void testDestinationFloor() throws IOException {
		fd.setDestinationFloor(5);
		assertEquals(5, fd.getDestinationFloor());
	}
	
	/**
	 * Test out of range handling for destinationFloor property.
	 * @throws IOException
	 */
	@Test
	void testDestinationFloorOutOfRange() throws IOException {
		@SuppressWarnings("unused")
		Throwable exception = assertThrows(IOException.class,
	            ()->{fd.setDestinationFloor(6);} );
	}
	
	/**
	 * Test set and get methods for floorButton property.
	 * @throws IOException
	 */
	@Test
	void testFloorButton() throws IOException {
		fd.setFloorButton("up");
		assertEquals("up", fd.getFloorButton());
	}
	
}
