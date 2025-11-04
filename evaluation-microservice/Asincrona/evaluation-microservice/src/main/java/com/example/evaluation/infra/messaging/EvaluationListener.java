package com.example.evaluation.infra.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EvaluationListener {

    // Escucha mensajes del microservicio de usuarios
    @RabbitListener(queues = "user.queue")
    public void escucharUsuarios(Object mensaje) {
        System.out.println("📥 Mensaje recibido desde USER SERVICE:");
        System.out.println(mensaje);
        // Aquí puedes deserializar y guardar info de evaluadores o estudiantes
        // Ejemplo: actualizar datos del evaluador si cambió su rol o correo
    }

    // Escucha mensajes del microservicio de trabajos de grado
    @RabbitListener(queues = "degreework.queue")
    public void escucharTrabajosGrado(Object mensaje) {
        System.out.println("📥 Mensaje recibido desde DEGREEWORK SERVICE:");
        System.out.println(mensaje);
        // Aquí puedes manejar notificaciones de proyectos o anteproyectos
        // Ejemplo: recibir correcciones o nuevos documentos asignados
    }
}
