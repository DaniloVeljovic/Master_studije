package elfak.masterrad.devicesservice.controllers;

import elfak.masterrad.devicesservice.mapper.Mapper;
import elfak.masterrad.devicesservice.models.ActuationStatus;
import elfak.masterrad.devicesservice.models.dtos.ActuationDTO;
import elfak.masterrad.devicesservice.models.entities.Actuation;
import elfak.masterrad.devicesservice.services.ActuationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController("/api/actuation")
public class ActuationController {

    @Autowired
    private ActuationService actuationService;

    @Autowired
    private Mapper mapper;

    @PostMapping
    public ResponseEntity<Actuation> createActuation(ActuationDTO actuationDTO) {
        Actuation actuation = mapper.mapDTOToDomainObject(actuationDTO);
        actuation.setStatus(ActuationStatus.NEW);
        return ResponseEntity.ok(actuationService.saveActuation(actuation));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Actuation> getActuation(Integer id) {
        return ResponseEntity.ok(actuationService.getActuationById(id));
    }

}
