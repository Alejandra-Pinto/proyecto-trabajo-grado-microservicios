package com.example.notification.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EmailSimulator {
    private static final Logger logger = LoggerFactory.getLogger(EmailSimulator.class);

    public void simulate(String to, String subject, String body) {
        logger.info("[Simulado] Para: {}\nAsunto: {}\nMensaje: {}\n", to, subject, body);
    }
}
