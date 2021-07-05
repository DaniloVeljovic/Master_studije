package com.ubicomp.elfak.usmeni.models;

import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

import java.time.Instant;

@Measurement(name = "irrigation")
public class Irrigation {

    @Column(name = "time")
    private Instant time;

    @Column(name = "length")
    private Long length;

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }
}
