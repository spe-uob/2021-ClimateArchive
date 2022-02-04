package org.climatearchive.climatearchive;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class ClimateArchiveApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {}

    @Test
    void testBasicDataGetting() throws Exception {
        mockMvc.perform(get("/getData?model=tEyea&lat=50&lon=50"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("field,temp,rain\nann,283.55814,1.0425966E-5\njan,267.05478,1.7415154E-5\nfeb,269.44598,1.44473015E-5\nmar,274.88287,7.2331854E-6\napr,283.03055,8.1359985E-6\nmay,292.10703,7.907912E-6\njun,299.283,8.6910995E-6\njul,301.8999,9.718774E-6\naug,299.81317,3.5989037E-6\nsep,292.23615,3.6016913E-6\noct,281.3395,1.07414835E-5\nnov,272.86063,1.5174153E-5\ndec,268.7442,1.8445933E-5")));
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
    
    @Test
    void testIndexOutOfBounds() throws Exception {
        mockMvc.perform(get("/getData?model=tEyea&lat=91&lon=50"))
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(content().string(containsString("Please select a value for latitude between -90.0 & 90.0")));
        mockMvc.perform(get("/getData?model=tEyea&lat=-91&lon=50"))
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(content().string(containsString("Please select a value for latitude between -90.0 & 90.0")));
        mockMvc.perform(get("/getData?model=tEyea&lat=90&lon=357"))
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(content().string(containsString("Please select a value for longitude between 0.0 & 356.25")));
        mockMvc.perform(get("/getData?model=tEyea&lat=90&lon=-1"))
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(content().string(containsString("Please select a value for longitude between 0.0 & 356.25")));
        mockMvc.perform(get("/getData?model=tEyea&lat=91&lon=-1"))
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(content().string(containsString("Please select a value for latitude between -90.0 & 90.0")));
        mockMvc.perform(get("/getData?model=tEyea&lat=91&lon=357"))
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(content().string(containsString("Please select a value for latitude between -90.0 & 90.0")));
        mockMvc.perform(get("/getData?model=tEyea&lat=-91&lon=-1"))
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(content().string(containsString("Please select a value for latitude between -90.0 & 90.0")));
        mockMvc.perform(get("/getData?model=tEyea&lat=-91&lon=357"))
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(content().string(containsString("Please selct a value for latitude between -90.0 & 90.0")));
    }
}
