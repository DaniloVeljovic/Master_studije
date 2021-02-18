package com.danilo.read.repository;

import com.danilo.read.entity.Parameters;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ParametersRepository extends CrudRepository<Parameters, Long> {

}
