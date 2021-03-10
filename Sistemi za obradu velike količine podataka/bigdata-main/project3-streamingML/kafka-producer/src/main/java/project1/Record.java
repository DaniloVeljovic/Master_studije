package project1;



import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;

public class Record implements Serializable {
    private final Integer tripDuration;
    private final LocalDateTime startTime;
    private final LocalDateTime stopTime;
    private final Long startStationId;
    private final String startStationName;
    private final BigDecimal startStationLatitude;
    private final BigDecimal startStationLongitude;
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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getStopTime() {
        return stopTime;
    }

    public Long getStartStationId() {
        return startStationId;
    }

    public String getStartStationName() {
        return startStationName;
    }

    public BigDecimal getStartStationLatitude() {
        return startStationLatitude;
    }

    public BigDecimal getStartStationLongitude() {
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
        private LocalDateTime startTime = LocalDateTime.MAX;
        private LocalDateTime stopTime = LocalDateTime.MAX;
        private Long startStationId = -1L;
        private String startStationName = "";
        private BigDecimal startStationLatitude = null;
        private BigDecimal startStationLongitude = null;
        private Long endStationId = -1L;
        private String endStationName = "";
        private String endStationLatitude = "";
        private String endStationLongitude = "";
        private Long bikeId = -1L;
        private String userType = "";
        private Long birthYear = -1L;
        private Integer gender = -1;

        public Builder() {
        }

        public Builder tripDuration(Integer val) {
            this.tripDuration = val;
            return this;
        }

        public Builder startTime(String val) {
            String dateTimePattern = "yyyy-MM-dd HH:mm:ss";
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimePattern);
            LocalDateTime toSet = LocalDateTime.MAX;

            if(!val.equals("NULL"))
                toSet = LocalDateTime.parse(val, dateTimeFormatter);

            this.startTime = toSet;
            return this;
        }

        public Builder stopTime(String val) {
            String dateTimePattern = "yyyy-MM-dd HH:mm:ss";
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimePattern);
            LocalDateTime toSet = LocalDateTime.MAX;

            if(!val.equals("NULL"))
                toSet = LocalDateTime.parse(val, dateTimeFormatter);

            this.stopTime = toSet;
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
            BigDecimal bd = null;
            if(!val.equals("NULL")){
                bd = new BigDecimal(val);
            }
            this.startStationLatitude = bd;
            return this;
        }

        public Builder startStationLongitude(String val) {
            BigDecimal bd = null;
            if(!val.equals("NULL")){
                bd = new BigDecimal(val);
            }
            this.startStationLongitude = bd;
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
            this.endStationLongitude = val;
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
