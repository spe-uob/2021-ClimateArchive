package org.climatearchive.climatearchive;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ClimateArchiveApplicationTests {

    @Test
    void contextLoads() {}

    @Test
    void testBasicDataGetting() {
        assertEquals(1, 1);
    }

    @Test
    void testBasicDataGetting2() {
        assertEquals(1, 2);
    }
}
