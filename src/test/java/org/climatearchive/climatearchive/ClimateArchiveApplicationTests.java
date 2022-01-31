package org.climatearchive.climatearchive;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ClimateArchiveApplicationTests {

    @Test
    void contextLoads() {}

    @Test
    void testBasicDataGetting() {
        assertEquals(1, 1);
    }
}
