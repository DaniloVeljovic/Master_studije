package elfak.masterrad.devicesservice.jobs;

import elfak.masterrad.devicesservice.models.ActuationStatus;
import elfak.masterrad.devicesservice.models.entities.Actuation;
import elfak.masterrad.devicesservice.services.ActuationService;
import elfak.masterrad.devicesservice.services.impl.ActuationServiceImpl;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
public class SendActuationToDeviceJob {

    private final Logger logger = LoggerFactory.getLogger(SendActuationToDeviceJob.class);

    @Autowired
    private ActuationService actuationService;

    @Scheduled(fixedRate = 5000)
    public void execute() {
        logger.info("Started execution of SendActuationToDeviceJob");
        List<Actuation> actuationList = actuationService.getActuationsInStatusAndBeforeDate(ActuationStatus.NEW, new Date());
        for (Actuation actuation : actuationList) {
            actuation.setStatus(ActuationStatus.RUNNING);
            actuationService.sendActuationToDevice(actuation);
            logger.info("Sent actuation message to device ");
            actuationService.saveActuation(actuation);
        }
    }
}
