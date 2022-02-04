package org.climatearchive.climatearchive;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class AdminController {

    @PostMapping
    public ResponseEntity<Object> addModels(@RequestBody @NotNull List<String> models) { // add models to sqlite db
        List<String> failedModels = new ArrayList<>();
        for (String m: models) {
            // add model m to sql database
        }
        if (failedModels.isEmpty()) {
            return new ResponseEntity<>("OK", HttpStatus.OK);
        } else if (failedModels.size() != models.size()) {
            return new ResponseEntity<>("Failed to add:\n" + String.join("\n", failedModels), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Couldn't add any models to DB", HttpStatus.EXPECTATION_FAILED);
        }
    }

}
