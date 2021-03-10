package project3;

import project1.Record;
import tweet.Parse;

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

public class AddParams {
    public static void main(String[] args) throws IOException {

        File fileTrain = new File("src/main/resources/train.csv");
        File fileTest = new File("src/main/resources/test.csv");

        FileWriter fw = new FileWriter("test_with_new.csv");

        Scanner reader = new Scanner(fileTest);

        writeToFile(fw, reader, "trip_duration", "test");

        fw = new FileWriter("train_with_new.csv");
        reader = new Scanner(fileTrain);

        writeToFile(fw, reader, "trip_duration", "test");

    }

    public static void writeToFile(FileWriter fw, Scanner reader, String... columnNames) throws IOException {
        for (int i=0; i<columnNames.length; i++){
            fw.write(columnNames[i]);
            if(i< columnNames.length-1)
                fw.write(",");
        }

        fw.write("\n");

        while(reader.hasNextLine()){
            String toParse = reader.nextLine();
            Record record = Parse.parseStringToRecord(toParse);
            String toWrite = record.getTripDuration() + "," + record.getTripDuration() * 1.2 + "\n";
            fw.write(toWrite);
        }
    }
}
