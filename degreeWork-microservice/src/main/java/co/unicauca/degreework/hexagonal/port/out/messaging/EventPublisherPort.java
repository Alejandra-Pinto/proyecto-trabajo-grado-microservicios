// hexagonal/port/out/EventPublisherPort.java
package co.unicauca.degreework.hexagonal.port.out.messaging;

import co.unicauca.degreework.hexagonal.application.dto.DegreeWorkCreatedEvent;
import co.unicauca.degreework.hexagonal.application.dto.NotificationEventDTO;

public interface EventPublisherPort {
    void sendDegreeWorkCreated(DegreeWorkCreatedEvent event);
    void sendNotification(NotificationEventDTO event);
}