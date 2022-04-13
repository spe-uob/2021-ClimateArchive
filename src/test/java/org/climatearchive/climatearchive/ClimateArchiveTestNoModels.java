package org.climatearchive.climatearchive;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class ClimateArchiveTestNoModels {

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void removeOldModels() {
        if (new File("model.data").delete()) {
            System.out.println("Removed old file");
        }
    }

    @Test
    void contextLoads() {}

    @Test
    void testBasicDataGetting() throws Exception {
        mockMvc.perform(get("/getData?model=tEyea&lat=50&lon=50"))
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(content().string(containsString("Model " + '"' + "tEyea" + '"' + " not found")));
    }

    @Test
    void testParameterNotProvided() throws Exception {
        mockMvc.perform(get("/getData"))
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(content().string(containsString("Parameter " + '"' + "model" + '"' + " not provided.")));
        mockMvc.perform(get("/getData?model=tEyea"))
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(content().string(containsString("Parameter " + '"' + "lat" + '"' + " not provided.")));
        mockMvc.perform(get("/getData?model=tEyea&lat=50"))
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(content().string(containsString("Parameter " + '"' + "lon" + '"' + " not provided.")));
    }

    @Test
    void testModelNotFound() throws Exception {
        mockMvc.perform(get("/getData?model=modelNameThatDoesntExist&lat=50&lon=50"))
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(content().string(containsString("Model " + '"'  + "modelNameThatDoesntExist" + '"' + " not found.")));
    }


}
