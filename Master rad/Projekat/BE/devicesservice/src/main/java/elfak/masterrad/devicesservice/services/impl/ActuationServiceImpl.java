package elfak.masterrad.devicesservice.services.impl;

import elfak.masterrad.devicesservice.kafka.producers.ActuationMessagePublisher;
import elfak.masterrad.devicesservice.mapper.Mapper;
import elfak.masterrad.devicesservice.models.ActuationStatus;
import elfak.masterrad.devicesservice.models.entities.Actuation;
import elfak.masterrad.devicesservice.repositories.ActuationRepository;
import elfak.masterrad.devicesservice.services.ActuationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ActuationServiceImpl implements ActuationService {

    @Autowired
    private ActuationRepository actuationRepository;

    @Autowired
    private ActuationMessagePublisher publisher;

    @Autowired
    private Mapper mapper;

    private final Logger logger = LoggerFactory.getLogger(ActuationServiceImpl.class);

    /*
     * Implementiraj jos kontroler koji moze da cuva aktuacije sa endpointa
     * Implementiraj job koji ce da se okida na par minuta i da proverava
     * da li postoji neka aktuacija koja nije running (dodaj taj status)
     * ako postoji dodaj polje address ili device id ili topic id
     * i mqtt protokolom ili kafkom saljes informacije o aktuaciji do uredjaja
     * ta aktuacija prelazi u status running
     * dodaj listener koji ce da ocekuje i slusa povratne poruke o tome da je neka aktuacija zavrsena i kad je zavrsena
     * u bazi joj menja status u finished
     * */

    @Override
    public Actuation saveActuation(Actuation actuation) {
        Actuation savedActuation = actuationRepository.save(actuation);
        logger.info("Stored actuation: " + savedActuation);
        return savedActuation;
    }

    @Override
    public Actuation getActuationById(Integer id) {
        Optional<Actuation> foundActuation = actuationRepository.findById(id);
        return foundActuation.orElseThrow(NoSuchElementException::new);
    }

    @Override
    public List<Actuation> getActuationsInStatusAndBeforeDate(ActuationStatus status, Date date) {
        return actuationRepository.findByStatusAndDateBefore(status, date);
    }

    @Override
    public void sendActuationToDevice(Actuation actuation) {
        publisher.publishMessage(actuation);
    }
}
