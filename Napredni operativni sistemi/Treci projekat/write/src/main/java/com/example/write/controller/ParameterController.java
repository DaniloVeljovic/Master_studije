package com.example.write.controller;

import com.example.write.entity.Parameters;
import com.example.write.service.IParametersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ParameterController {

    @Autowired
    private IParametersService service;

    @Autowired
    private KafkaTemplate<String, Parameters> kafkaTemplate;

    @PostMapping(produces = "application/json", consumes = "application/json", value = "/parameters")
    public Parameters createParameters(@RequestBody Parameters parameters){
        Parameters savedParams = service.saveParameters(parameters);
        kafkaTemplate.send("params", savedParams);
        System.gc();
        return parameters;
    }

}
