// hexagonal/port/in/DegreeWorkEvaluationListenerPort.java
package co.unicauca.degreework.hexagonal.port.in.messaging;

import co.unicauca.degreework.hexagonal.application.dto.DegreeWorkUpdateDTO;
import co.unicauca.degreework.hexagonal.application.dto.EvaluacionEventDTO;

public interface DegreeWorkEvaluationListenerPort {
    void onUpdate(DegreeWorkUpdateDTO dto);
    void onEvaluadores(EvaluacionEventDTO dto);
}