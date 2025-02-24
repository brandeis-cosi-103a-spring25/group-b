package edu.brandeis.cosi103a.groupb;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import edu.brandeis.cosi103a.groupb.Main;

public class TestMain {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outContent));
    }

    @Test
    public void testMain() {
        String input = "0\n"; // Simulate user input for HumanPlayer
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        Main.main(new String[]{});

        String output = outContent.toString();
        assertTrue(output.contains("Game Over! Final Scores:"));
        assertTrue(output.contains("Alice:"));
        assertTrue(output.contains("Bot:"));
    }
}