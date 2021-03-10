package project3;

import project1.Record;
import tweet.Parse;

import java.io.*;
import java.util.Scanner;

public class AddParams {
    public static void main(String[] args) throws IOException {

        File fileTrain = new File("src/main/resources/train.csv");
        File fileTest = new File("src/main/resources/test.csv");

        FileWriter fw = new FileWriter("test_with_new.csv");

        Scanner reader = new Scanner(fileTest);

        writeToFile(fw, reader);

        fw = new FileWriter("train_with_new.csv");
        reader = new Scanner(fileTrain);

        writeToFile(fw, reader);

    }

    public static void writeToFile(FileWriter fw, Scanner reader) throws IOException {
        while(reader.hasNextLine()){
            String toParse = reader.nextLine();
            Record record = Parse.parseStringToRecord(toParse);
            String toWrite = toParse + "," + record.getTripDuration() * 1.2 + "\n";
            fw.write(toWrite);
        }
    }
}
