package com.danilo.read.controllers;

import com.danilo.read.entity.Parameters;
import com.danilo.read.service.ParametersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api")
public class ParameterController {

    @Autowired
    private ParametersService parametersService;

    @GetMapping(value = "/parameters/{id}", produces = "application/json")
    public Parameters getById(@PathVariable Long id){
        return parametersService.getParametersById(id);
    }

    @GetMapping(value = "/parameters", produces = "application/json")
    public List<Parameters> getAll(){
        return parametersService.getAllParameters();
    }

}
