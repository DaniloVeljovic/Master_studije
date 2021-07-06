package com.ubicomp.elfak.usmeni.services;

import com.ubicomp.elfak.usmeni.models.Irrigation;
import com.ubicomp.elfak.usmeni.models.dto.IrrigationDTO;

public interface IrrigationService {

    Irrigation tryToIrrigate(IrrigationDTO irrigationDTO) throws InterruptedException;

    void irrigate() throws InterruptedException;
}
