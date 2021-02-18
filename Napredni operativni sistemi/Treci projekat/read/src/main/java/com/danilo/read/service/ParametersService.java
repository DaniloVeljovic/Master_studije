package com.danilo.read.service;

import com.danilo.read.entity.Parameters;

import java.util.List;
import java.util.Optional;

public interface ParametersService {

    Parameters getParametersById(Long id);

    List<Parameters> getAllParameters();
}
