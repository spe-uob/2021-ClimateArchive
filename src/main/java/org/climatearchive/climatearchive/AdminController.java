package org.climatearchive.climatearchive;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {

    @PostMapping
    public ResponseEntity<Object> addModels() { // add models to sqlite db
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

}
