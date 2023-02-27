import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.PrintWriter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * JUnit Tests for Floor Class.
 * 
 * todo: fix this class.
 * 
 * @author Fareen Lavji
 * 
 * @version 02.04.2023
 */
class FloorTest {
	
	String date = "14:05:15";
	int iFloor = 0;
	int dFloor = 1;
	String direction = "up";
	
	Scheduler s = new Scheduler();

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		PrintWriter pw = new PrintWriter(new File("testFile.csv"));
		StringBuffer csvData = new StringBuffer("");
		
		csvData.append(date + ",");
		csvData.append(iFloor + ",");
		csvData.append(direction + ",");
		csvData.append(dFloor);
		
		pw.write(csvData.toString());
		pw.close();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterEach
	void tearDown() throws Exception {
		File f = new File("testFile.csv");
		f.delete();
	}

	@Test
	void testUnwrapData() {
	}
}
