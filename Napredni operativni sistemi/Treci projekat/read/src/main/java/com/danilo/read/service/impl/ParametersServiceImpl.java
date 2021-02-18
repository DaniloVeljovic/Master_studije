package com.danilo.read.service.impl;

import com.danilo.read.entity.Parameters;
import com.danilo.read.repository.ParametersRepository;
import com.danilo.read.service.ParametersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.tags.Param;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ParametersServiceImpl implements ParametersService {

    @Autowired
    private ParametersRepository repository;

    @Override
    public Parameters getParametersById(Long id) {
        return repository.findById(id).get();
    }

    @Override
    public List<Parameters> getAllParameters() {
        List<Parameters> ret = new ArrayList<>();
        repository.findAll().forEach(ret::add);
        return ret;
    }
}
