package com.softinstigate.ermes.mail;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    private final MockMailMessage mockMessage = new MockMailMessage();

    @Test
    void sendMockMessage() {
        String from = "omar@softinstigate.com";
        String to = "maurizio@softinstigate.com";
        String result = mockMessage.send(from, to, "Test message");
        assertEquals("{from='omar@softinstigate.com', to='maurizio@softinstigate.com', message='Test message'}", result);
    }

}