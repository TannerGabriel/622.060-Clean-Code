import at.aau.Main;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class MainTest {
    @Test
    void testRemoveFragment_WithFragment() throws Exception {
        String url = "http://example.com/page#section";
        String expected = "http://example.com/page";

        Method method = Main.class.getDeclaredMethod("removeFragment", String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(null, url);

        assertEquals(expected, result, "URL with fragment should have fragment removed.");
    }
}
