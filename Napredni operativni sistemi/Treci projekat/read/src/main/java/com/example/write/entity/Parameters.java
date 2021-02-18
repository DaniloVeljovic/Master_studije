package com.example.write.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Parameters {

    @Id
    private Long id;

    @Column(name = "temperature")
    private Double temperature;


    public Parameters(){

    }

    public Parameters(Double temperature) {
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


    @Override
    public String toString() {
        return "Parameters{" +
                "id=" + id +
                ", temperature=" + temperature +
                '}';
    }
}
