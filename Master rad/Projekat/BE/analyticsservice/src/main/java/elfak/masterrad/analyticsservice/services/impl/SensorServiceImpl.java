package elfak.masterrad.analyticsservice.services.impl;

import elfak.masterrad.analyticsservice.kafka.models.ActuationMessage;
import elfak.masterrad.analyticsservice.kafka.publishers.ActuationMessagePublisher;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class SensorServiceImpl implements SensorService {

    @Autowired
    private ActuationMessagePublisher publisher;

    @Value("${influx.host}")
    private String host;

    @Value("${influx.username}")
    private String username;

    @Value("${influx.password}")
    private String password;

    @Value("${model.location}")
    private String modelPath;

    @Value("${model.dataset}")
    private String datasetPath;

    private static Classifier classifier;

    private final static Object shouldActuateIfPredicted = "Bad";

    private final Logger logger = LoggerFactory.getLogger(SensorServiceImpl.class);

    @EventListener(ApplicationReadyEvent.class)
    public void trainOrAcquireModel() throws IOException {
        File model = new File(modelPath);

        //if exist, read it
        if(model.exists()) {
            classifier = loadModelFromFS(modelPath);
            return;
        }

        //if not, create it
        classifier = createAndTrainModel(datasetPath);
        //evaluate classifier and write the results to a file
        //evaluateClassifier(classifier);
        writeModelToFS(modelPath, classifier);
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
        logger.info("Saved measurement: " + sensorMeasurement);
        influxDB.close();

        return sensorMeasurement;
    }

    @Override
    public Object analyzeMeasurement(SensorMeasurementDTO sensorMeasurementDTO) {
        double[] doubles = new double[]{
                sensorMeasurementDTO.getLightIntensity(),
                sensorMeasurementDTO.getGroundMoisture(),
                sensorMeasurementDTO.getWindIntensity(),
                sensorMeasurementDTO.getSoilHumidity()};

        Instance instance = new DenseInstance(doubles);
        return classifier.classify(instance);
    }

    @Override
    public void analyzeMeasurementAndActuateIfNeeded(SensorMeasurementDTO sensorMeasurementDTO) {
        Object predictedClass = analyzeMeasurement(sensorMeasurementDTO);
        logger.info("Predicted class for sensor measurement " + sensorMeasurementDTO + " is " + predictedClass);
        if(predictedClass.equals(shouldActuateIfPredicted)) {
            logger.info("Performing actuation.");
            sendActuationMessage(new Date(), 5000L, "GPIO_01", "1");
        }
    }

    @Override
    public void sendActuationMessage(Date activationDate, long length, String pinToActivate, String deviceId) {
        publisher.publishMessage(new ActuationMessage(activationDate, length, pinToActivate, deviceId));
    }

    private Classifier loadModelFromFS(String pathToModel) {
        try {
            FileInputStream fileIn = new FileInputStream(pathToModel);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            Classifier classifier = (Classifier) in.readObject();
            in.close();
            fileIn.close();
            return classifier;
        } catch (IOException i) {
            i.printStackTrace();
            return null;
        } catch (ClassNotFoundException c) {
            logger.error("Model not found");
            c.printStackTrace();
            return null;
        }
    }

    private Classifier createAndTrainModel(String datasetPath) throws IOException {
        File file = new File(datasetPath);
        Dataset data = FileHandler.loadDataset(file, 4, ",");
        Classifier classifier = new KNearestNeighbors(5);
        classifier.buildClassifier(data);
        return classifier;
    }

    private void writeModelToFS(String pathToModel, Classifier classifier) {
        try {
            FileOutputStream fileOut =
                    new FileOutputStream(pathToModel);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(classifier);
            out.close();
            fileOut.close();
            logger.info("Serialized data is saved in classifier.ser");
        } catch (IOException i) {
            i.printStackTrace();
        }
    }
}
