package co.unicauca.degreework.hexagonal.port.in.messaging;

import co.unicauca.degreework.hexagonal.application.dto.UserCreatedEvent;

public interface UserCreatedListenerPort {
    void onUserCreated(UserCreatedEvent event);
}