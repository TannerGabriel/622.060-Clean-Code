package at.aau.utils;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

class LoggerTest {

    @Test
    void testSingletonInstance() {
        Logger instance1 = Logger.getInstance();
        Logger instance2 = Logger.getInstance();

        assertSame(instance1, instance2);
    }

    @Test
    void testLogError() {
        Logger logger = Logger.getInstance();
        logger.getLogs().clear(); // Ensure the log is clear before the test

        String errorMessage = "This is an error message";
        logger.logError(errorMessage);

        ArrayList<String> logs = (ArrayList<String>) logger.getLogs();
        assertEquals(1, logs.size());
        assertEquals(errorMessage, logs.get(0));
    }

    @Test
    void testGetLogsString() {
        Logger logger = Logger.getInstance();
        logger.getLogs().clear(); // Ensure the log is clear before the test

        String errorMessage1 = "Error message 1";
        String errorMessage2 = "Error message 2";
        logger.logError(errorMessage1);
        logger.logError(errorMessage2);

        String expectedLogString = "# ERRORS <br>\n- Error message 1<br>\n- Error message 2<br>\n";
        assertEquals(expectedLogString, logger.getLogsString());
    }

    @Test
    void testThreadSafety() throws InterruptedException {
        final Logger logger = Logger.getInstance();
        logger.getLogs().clear(); // Ensure the log is clear before the test

        Thread thread1 = new Thread(() -> logger.logError("Error from thread 1"));
        Thread thread2 = new Thread(() -> logger.logError("Error from thread 2"));

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        ArrayList<String> logs = (ArrayList<String>) logger.getLogs();
        assertEquals(2, logs.size());
        assertTrue(logs.contains("Error from thread 1"));
        assertTrue(logs.contains("Error from thread 2"));
    }
}
