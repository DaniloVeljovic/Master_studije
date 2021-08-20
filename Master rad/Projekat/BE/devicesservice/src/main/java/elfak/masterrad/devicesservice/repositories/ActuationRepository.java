package elfak.masterrad.devicesservice.repositories;

import elfak.masterrad.devicesservice.models.ActuationStatus;
import elfak.masterrad.devicesservice.models.entities.Actuation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;

public interface ActuationRepository extends CrudRepository<Actuation, Integer> {

    @Query("SELECT a FROM Actuation a WHERE a.status=:status AND a.activationDate <= :date")
    List<Actuation> findByStatusAndDateBefore(ActuationStatus status, Date date);
}
