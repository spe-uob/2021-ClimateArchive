package org.climatearchive.climatearchive;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class ClimateArchiveApplicationTests {

    @Autowired
    Controller c;

    @Test
    void testBasicDataGetting() {
        assertEquals(1, 1);
    }
}
