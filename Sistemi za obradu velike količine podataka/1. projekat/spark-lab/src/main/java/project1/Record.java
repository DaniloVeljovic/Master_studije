package project1;

import org.apache.spark.sql.sources.In;

import java.io.Serializable;
import java.math.BigDecimal;

public class Record implements Serializable {
    private final Integer tripDuration;
    private final String startTime;
    private final String stopTime;
    private final Long startStationId;
    private final String startStationName;
    private final String startStationLatitude;
    private final String startStationLongitude;
    private final Long endStationId;
    private final String endStationName;
    private final String endStationLatitude;
    private final String endStationLongitude;
    private final Long bikeId;
    private final String userType;
    private final Long birthYear;
    private final Integer gender;

    public Integer getTripDuration() {
        return tripDuration;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getStopTime() {
        return stopTime;
    }

    public Long getStartStationId() {
        return startStationId;
    }

    public String getStartStationName() {
        return startStationName;
    }

    public String getStartStationLatitude() {
        return startStationLatitude;
    }

    public String getStartStationLongitude() {
        return startStationLongitude;
    }

    public Long getEndStationId() {
        return endStationId;
    }

    public String getEndStationName() {
        return endStationName;
    }

    public String getEndStationLatitude() {
        return endStationLatitude;
    }

    public String getEndStationLongitude() {
        return endStationLongitude;
    }

    public Long getBikeId() {
        return bikeId;
    }

    public String getUserType() {
        return userType;
    }

    public Long getBirthYear() {
        return birthYear;
    }

    public Integer getGender() {
        return gender;
    }

    @Override
    public String toString() {
        return "Record{" +
                "tripDuration=" + tripDuration +
                ", startStationName='" + startStationName + '\'' +
                ", endStationName='" + endStationName + '\'' +
                '}';
    }

    public Record(Builder builder) {
        this.tripDuration = builder.tripDuration;
        this.startTime = builder.startTime;
        this.stopTime = builder.stopTime;
        this.startStationId = builder.startStationId;
        this.startStationLatitude = builder.startStationLatitude;
        this.startStationLongitude = builder.startStationLongitude;
        this.startStationName = builder.startStationName;
        this.endStationId = builder.endStationId;
        this.endStationName = builder.endStationName;
        this.endStationLatitude = builder.endStationLatitude;
        this.endStationLongitude = builder.endStationLongitude;
        this.bikeId = builder.bikeId;
        this.birthYear = builder.birthYear;
        this.gender = builder.gender;
        this.userType = builder.userType;
    }

    public static class Builder {
        private Integer tripDuration = -1;
        private String startTime = "";
        private String stopTime = "";
        private Long startStationId = -1l;
        private String startStationName = "";
        private String startStationLatitude = "";
        private String startStationLongitude = "";
        private Long endStationId = -1l;
        private String endStationName = "";
        private String endStationLatitude = "";
        private String endStationLongitude = "";
        private Long bikeId = -1l;
        private String userType = "";
        private Long birthYear = -1l;
        private Integer gender = -1;

        public Builder() {
        }

        public Builder tripDuration(Integer val) {
            this.tripDuration = val;
            return this;
        }

        public Builder startTime(String val) {
            //TODO: Treba konvertovati iz Stringa u DateTime, treba videti koji.
            // Ovo vazi za sve stringove koji su datumi.
            this.startTime = val;
            return this;
        }

        public Builder stopTime(String val) {
            this.stopTime = val;
            return this;
        }

        public Builder startStationId(Long val) {
            this.startStationId = val;
            return this;
        }

        public Builder startStationName(String val) {
            this.startStationName = val;
            return this;
        }

        public Builder startStationLatitude(String val) {
            this.startStationLatitude = val;
            return this;
        }

        public Builder startStationLongitude(String val) {
            this.startStationLongitude = val;
            return this;
        }

        public Builder endStationId(Long val) {
            this.endStationId = val;
            return this;
        }

        public Builder endStationName(String val) {
            this.endStationName = val;
            return this;
        }

        public Builder endStationLatitude(String val) {
            this.endStationLatitude = val;
            return this;
        }

        public Builder endStationLongitude(String val) {
            this.startStationLongitude = val;
            return this;
        }

        public Builder bikeId(Long val) {
            this.bikeId = val;
            return this;
        }

        public Builder userType(String val) {
            this.userType = val;
            return this;
        }

        public Builder birthYear(Long val) {
            this.birthYear = val;
            return this;
        }

        public Builder gender(Integer val) {
            this.gender = val;
            return this;
        }

        public Record build() {
            return new Record(this);
        }

    }

}
