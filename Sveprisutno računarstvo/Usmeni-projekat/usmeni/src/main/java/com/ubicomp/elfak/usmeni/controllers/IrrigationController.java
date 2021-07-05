package com.ubicomp.elfak.usmeni.controllers;

import com.ubicomp.elfak.usmeni.models.Irrigation;
import com.ubicomp.elfak.usmeni.services.IrrigationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/irrigation")
public class IrrigationController {

    @Autowired
    private IrrigationService irrigationService;

    @PostMapping("/{userId}")
    public ResponseEntity irrigate(@PathVariable Long userId) throws InterruptedException {
        Irrigation irrigation = irrigationService.tryToIrrigate(userId);
        if(irrigation == null)
            return ResponseEntity.badRequest().body("{ \"error\" : \"Cannot irrigate at the moment. Try again later. \" }");
        else
            return ResponseEntity.ok(irrigation);
    }

}
