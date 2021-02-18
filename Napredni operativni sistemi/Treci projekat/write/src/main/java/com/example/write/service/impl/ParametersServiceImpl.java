package com.example.write.service.impl;

import com.example.write.entity.Parameters;
import com.example.write.repository.ParametersRepository;
import com.example.write.service.IParametersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParametersServiceImpl implements IParametersService {

    @Autowired
    private ParametersRepository repository;

    @Override
    public Parameters saveParameters(Parameters parameters) {
        return repository.save(parameters);
    }
}
