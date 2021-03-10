package tweet;

import com.fasterxml.jackson.databind.ObjectMapper;
import project1.Record;
import project3.ExtendedRecord;

import java.io.IOException;
import java.math.BigDecimal;

public class Parse {

    public static Record parseStringToRecord(String line){
        String[] fields = line.replace("\"","").split(",");
        Record row = null;
        try {
            row = new Record.Builder()
                    .tripDuration(fields[0].equals("NULL") ? null : Integer.parseInt(fields[0]))
                    .startTime(fields[1])
                    .stopTime(fields[2])
                    .startStationId(fields[3].equals("NULL") ? null : Long.parseLong(fields[3]))
                    .startStationName(fields[4])
                    .startStationLatitude(fields[5])
                    .startStationLongitude(fields[6])
                    .endStationId(fields[7].equals("NULL") ? null : Long.parseLong(fields[7]))
                    .endStationName(fields[8])
                    .endStationLatitude(fields[9])
                    .endStationLongitude(fields[10])
                    .bikeId(Long.parseLong(fields[11]))
                    .userType(fields[12])
                    .birthYear(fields[13].equals("NULL") ? null : Long.parseLong(fields[13]))
                    .gender(Integer.parseInt(fields[14]))
                    .build();

        }
        catch (Exception e){
            e.printStackTrace();
        }

        return row;
    }

    public static ExtendedRecord parseStringToExtendedRecord(String line){
        String[] fields = line.replace("\"","").split(",");
        ExtendedRecord row = null;
        try {
            row = new ExtendedRecord.Builder()
                    .tripDuration(fields[0].equals("NULL") ? null : Integer.parseInt(fields[0]))
                    .startTime(fields[1])
                    .stopTime(fields[2])
                    .startStationId(fields[3].equals("NULL") ? null : Long.parseLong(fields[3]))
                    .startStationName(fields[4])
                    .startStationLatitude(fields[5])
                    .startStationLongitude(fields[6])
                    .endStationId(fields[7].equals("NULL") ? null : Long.parseLong(fields[7]))
                    .endStationName(fields[8])
                    .endStationLatitude(fields[9])
                    .endStationLongitude(fields[10])
                    .bikeId(Long.parseLong(fields[11]))
                    .userType(fields[12])
                    .birthYear(fields[13].equals("NULL") ? null : Long.parseLong(fields[13]))
                    .gender(Integer.parseInt(fields[14]))
                    .testBool(Integer.parseInt(fields[0]) > 1000 && Integer.parseInt(fields[14]) == 1 ? 1 : 0)
                    .test(Integer.parseInt(fields[0]) * 1.5 + 7) //test for regression
                    .build();

        }
        catch (Exception e){
            e.printStackTrace();
        }

        return row;
    }
}
