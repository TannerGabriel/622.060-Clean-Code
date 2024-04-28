package at.aau.io;

import at.aau.Main;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class MarkdownWriterTest {

    MarkdownWriter markdownWriter = new MarkdownWriter("test.md");

    @Test
    void createIndentation() {

        // Test indentation with depth 0
        assertEquals("", markdownWriter.createIndentation(0));

        // Test indentation with depth 1
        assertEquals("  ", markdownWriter.createIndentation(1));

        // Test indentation with depth 2
        assertEquals("    ", markdownWriter.createIndentation(2));

    }
}