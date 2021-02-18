package com.example.write.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Parameters {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "temperature")
    private Double temperature;


    public Parameters(){

    }

    public Parameters(Double temperature, Double humidity, Double pressure) {
        this.temperature = temperature;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

}
