package elfak.masterrad.analyticsservice.services.impl;


import elfak.masterrad.analyticsservice.models.dto.SensorMeasurementDTO;
import elfak.masterrad.analyticsservice.services.SensorService;
import net.sf.javaml.classification.Classifier;
import net.sf.javaml.classification.KNearestNeighbors;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import net.sf.javaml.tools.data.FileHandler;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class SensorServiceImpl implements SensorService {

    @Value("${influx.host}")
    private String host;

    @Value("${influx.username}")
    private String username;

    @Value("${influx.password}")
    private String password;

    private static Classifier classifier;

    private final static Object shouldActuateIfPredicted = "Bad";

    @EventListener(ApplicationReadyEvent.class)
    public void trainOrAcquireModel() throws IOException {
        File model =
                new File("C:\\Users\\danil\\Desktop\\Master_studije\\Master rad\\Projekat\\BE\\analyticsservice\\src\\main\\resources\\classifier.ser");

        if(model.exists()) {
            try {
                FileInputStream fileIn = new FileInputStream("/tmp/employee.ser");
                ObjectInputStream in = new ObjectInputStream(fileIn);
                classifier = (Classifier) in.readObject();
                in.close();
                fileIn.close();
            } catch (IOException i) {
                i.printStackTrace();
                return;
            } catch (ClassNotFoundException c) {
                System.out.println("Employee class not found");
                c.printStackTrace();
                return;
            }
            return;
        }

        File file = new File("C:\\Users\\danil\\Desktop\\Master_studije\\Master rad\\Projekat\\BE\\analyticsservice\\src\\main\\resources\\iris.data");
        Dataset data = FileHandler.loadDataset(file, 4, ",");
        classifier = new KNearestNeighbors(5);
        classifier.buildClassifier(data);

        try {
            FileOutputStream fileOut =
                    new FileOutputStream("C:\\Users\\danil\\Desktop\\Master_studije\\Master rad\\Projekat\\BE\\analyticsservice\\src\\main\\resources\\classifier.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(classifier);
            out.close();
            fileOut.close();
            System.out.printf("Serialized data is saved in /tmp/classifier.ser");
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    @Override
    public SensorMeasurementDTO createSensorMeasurement(SensorMeasurementDTO sensorMeasurement) {
        InfluxDB influxDB = InfluxDBFactory.connect(host, username, password);

        BatchPoints batchPoints = BatchPoints
                .database("sensorMeasurement")
                .retentionPolicy("defaultPolicy")
                .build();

        Point point1 = Point.measurement("sensorMeasurement")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("soilHumidity", sensorMeasurement.getSoilHumidity())
                .addField("groundMoisture", sensorMeasurement.getGroundMoisture())
                .addField("lightIntensity", sensorMeasurement.getLightIntensity())
                .addField("windIntensity", sensorMeasurement.getWindIntensity())
                .build();

        batchPoints.point(point1);
        influxDB.write(batchPoints);

        influxDB.close();

        return sensorMeasurement;
    }

    @Override
    public Object analyzeMeasurement(SensorMeasurementDTO sensorMeasurementDTO) {
        double[] doubles = new double[]{};
        Instance instance = new DenseInstance(doubles);
        return classifier.classify(instance);
    }

    @Override
    public void analyzeMeasurementAndActuateIfNeeded(SensorMeasurementDTO sensorMeasurementDTO) {
        Object predictedClass = analyzeMeasurement(sensorMeasurementDTO);
        if(predictedClass.equals(shouldActuateIfPredicted)) {
            sendActuationMessage(new Date(), 5000L, "GPIO_01");
        }
    }

    @Override
    public void sendActuationMessage(Date activationDate, long length, String pinToActivate) {
        //send message to a Kafka topic with these params
    }
}
