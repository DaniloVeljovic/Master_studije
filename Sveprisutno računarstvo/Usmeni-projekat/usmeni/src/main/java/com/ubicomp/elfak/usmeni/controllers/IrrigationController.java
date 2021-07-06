package com.ubicomp.elfak.usmeni.controllers;

import com.ubicomp.elfak.usmeni.models.Irrigation;
import com.ubicomp.elfak.usmeni.models.dto.IrrigationDTO;
import com.ubicomp.elfak.usmeni.services.IrrigationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/irrigation")
public class IrrigationController {

    @Autowired
    private IrrigationService irrigationService;

    @CrossOrigin(origins = "http://localhost:3000", allowedHeaders={"*"})
    @PostMapping
    public ResponseEntity irrigate(@RequestBody IrrigationDTO irrigationDTO) throws InterruptedException {
        Irrigation irrigation = irrigationService.tryToIrrigate(irrigationDTO);
        if(irrigation == null)
            return ResponseEntity.badRequest().body("{ \"error\" : \"Cannot irrigate at the moment. Try again later. \" }");
        else
            return ResponseEntity.ok(irrigation);
    }

}
