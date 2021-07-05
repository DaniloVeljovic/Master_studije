package com.ubicomp.elfak.usmeni.services;

import com.ubicomp.elfak.usmeni.models.Irrigation;

public interface IrrigationService {

    Irrigation tryToIrrigate(Long userId) throws InterruptedException;

    void irrigate() throws InterruptedException;
}
