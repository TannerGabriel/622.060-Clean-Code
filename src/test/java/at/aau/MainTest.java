package at.aau;

import at.aau.core.CrawlerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

public class MainTest {

    private final PrintStream originalSystemOut = System.out;
    private ByteArrayOutputStream testOut;

    @BeforeEach
    void setUp() {
        testOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOut));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalSystemOut);
    }

    @Test
    void testGetConfigWithArgs() {
        String[] args = {"https://google.com", "3", ".*\\.google\\.com.*", "en"};

        CrawlerConfig config = Main.getConfig(args);

        assertEquals("https://google.com", config.getStartUrl());
        assertEquals(3, config.getDepthLimit());
        assertEquals(".*\\.google\\.com.*", config.getDomainFilter());
        assertEquals("en", config.getTargetLang());
    }

    @Test
    void testGetConfigWithUserInput() {
        mockScanner("https://google.com\n3\n.*\\.google\\.com.*\nen\n");
        String[] args = {};

        CrawlerConfig config = Main.getConfig(args);

        assertEquals("https://google.com", config.getStartUrl());
        assertEquals(3, config.getDepthLimit());
        assertEquals(".*\\.google\\.com.*", config.getDomainFilter());
        assertEquals("en", config.getTargetLang());
        assertTrue(testOut.toString().contains("Enter the starting URL:"));
        assertTrue(testOut.toString().contains("Enter depth limit:"));
        assertTrue(testOut.toString().contains("Enter domain filter:"));
        assertTrue(testOut.toString().contains("Enter target Language:"));
    }

    @Test
    void testGetInputWithValidThenInvalidInput() {
        mockScanner("invalid\nvalid\n");

        String result = Main.getInput("Enter a valid string:", s -> s.equals("valid"));

        assertEquals("valid", result);
        assertTrue(testOut.toString().contains("Enter a valid string:"));
        assertTrue(testOut.toString().contains("Invalid input, please try again."));
    }

    @Test
    void testGetInputWithMultipleInvalidInputs() {
        mockScanner("wrong\nincorrect\nvalid\n");

        String result = Main.getInput("Enter a valid string:", s -> s.equals("valid"));

        assertEquals("valid", result);
        assertTrue(testOut.toString().contains("Invalid input, please try again."));
    }

    @Test
    void testGetIntInputWithValidInput() {
        mockScanner("200\n");

        int result = Main.getIntInput("Enter a valid integer:", x -> x > 100);

        assertEquals(200, result);
    }

    @Test
    void testGetIntInputWithInvalidThenValidInput() {
        mockScanner(Long.MAX_VALUE+"\n102\n");

        int result = Main.getIntInput("Enter a valid integer:", x -> x > 100);

        assertEquals(102, result);
        assertTrue(testOut.toString().contains("Invalid number format, please try again."));
    }

    @Test
    void testGetIntInputWithInvalidRange() {
        mockScanner("95\n102\n");

        int result = Main.getIntInput("Enter a valid integer above 100:", x -> x > 100);

        assertEquals(102, result);
        assertTrue(testOut.toString().contains("Invalid input, please try again."));
    }

    @Test
    void testValidateUrl() {
        assertTrue(Main.validateUrl("https://google.com"));
        assertTrue(Main.validateUrl("https://google.com/hello-world"));
        assertTrue(Main.validateUrl("http://google.com"));
        assertFalse(Main.validateUrl("google.com"));
        assertFalse(Main.validateUrl("htt:/google.com"));
    }

    @Test
    void testPositiveNumber() {
        assertTrue(Main.validatePositiveNumber(10));
        assertFalse(Main.validatePositiveNumber(-10));
    }

    private void mockScanner(String testInput) {
        Main.scanner = new Scanner(new ByteArrayInputStream(testInput.getBytes()));
    }
}
