package org.climatearchive.climatearchive;

import org.assertj.core.util.Arrays;
import org.climatearchive.climatearchive.modeldb.Model;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(args={"--add_models", "--models=tEyea,tEyeb,tEyec,tEyed,tEyee,teYEf,teYEg,teYEh,teYEi,teYEj,teYEk,teYEl,teYEm,teYEn,teYEo,teYEp,teYEq,teYEr,teYEs,teYEt,teYEu,teYEv,teYEw,teYEx,teYEy,teYEz", "--model_templates=<Climate_Archive_Model_Template>/climate/<climate_archive_model_template>a.pdcl<Climate_Archive_Field_Template>.nc,<Climate_Archive_Model_Template>/climate/<Climate_Archive_Model_Templat>"})
@AutoConfigureMockMvc
public class ClimateArchiveTestWithModels {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate modelDataBase;

    @BeforeAll
    static void removeOldModels() { // clears the database before adding new models
        if (new File("model.data").delete()) {
            System.out.println("Removed old file");
        }
    }

    @Test
    void contextLoads() {}

    @Test
    void modelsAddedSuccessfully() {
        List<Object> res = modelDataBase.query("SELECT model_name FROM model_data;", (rs, rowNumber) -> rs.getString("model_name"));
        assert res.equals(List.of("tEyea", "tEyeb", "tEyec", "tEyed", "tEyee", "teYEf", "teYEg", "teYEh", "teYEi", "teYEj", "teYEk", "teYEl", "teYEm", "teYEn", "teYEo", "teYEp", "teYEq", "teYEr", "teYEs", "teYEt", "teYEu", "teYEv", "teYEw", "teYEx", "teYEy", "teYEz"));
    }

    @Test
    void testBasicDataGetting() throws Exception {
        mockMvc.perform(get("/getData?model=tEyea&lat=50&lon=50"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("field,temp_mm_1_5m,precip_mm_srf\nann,283.55814,1.0425966E-5\njan,267.05478,1.7415154E-5\nfeb,269.44598,1.44473015E-5\nmar,274.88287,7.2331854E-6\napr,283.03055,8.1359985E-6\nmay,292.10703,7.907912E-6\njun,299.283,8.6910995E-6\njul,301.8999,9.718774E-6\naug,299.81317,3.5989037E-6\nsep,292.23615,3.6016913E-6\noct,281.3395,1.07414835E-5\nnov,272.86063,1.5174153E-5\ndec,268.7442,1.8445933E-5")));
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

