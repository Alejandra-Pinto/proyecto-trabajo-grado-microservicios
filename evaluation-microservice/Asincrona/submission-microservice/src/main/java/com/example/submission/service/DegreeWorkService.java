package com.example.submission.service;

import com.example.submission.entity.*;
import com.example.submission.infra.dto.DegreeWorkRequest;
import com.example.submission.repository.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DegreeWorkService implements IDegreeWorkService {

    private final DegreeWorkRepository degreeRepo;
    private final StudentRepository studentRepo;
    private final ProfessorRepository professorRepo;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;
    @Value("${app.rabbitmq.routingkey}")
    private String routingKey;

    public DegreeWorkService(DegreeWorkRepository degreeRepo, StudentRepository studentRepo, 
                             ProfessorRepository professorRepo, RabbitTemplate rabbitTemplate) {
        this.degreeRepo = degreeRepo;
        this.studentRepo = studentRepo;
        this.professorRepo = professorRepo;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public DegreeWork createDegreeWork(DegreeWorkRequest request) {
        DegreeWork dw = new DegreeWork();
        dw.setTitle(request.getTitle());
        dw.setModality(request.getModality());
        dw.setCreationDate(request.getCreationDate());
        dw.setGeneralObjective(request.getGeneralObjective());
        dw.setSpecificObjectives(request.getSpecificObjectives());
        dw.setEstadoP(request.getEstadoP());

        // Asociaciones
        dw.setStudent1(studentRepo.findById(request.getStudent1Id()).orElseThrow());
        if (request.getStudent2Id() != null) {
            dw.setStudent2(studentRepo.findById(request.getStudent2Id()).orElse(null));
        }

        dw.setDirector(professorRepo.findById(request.getDirectorId()).orElseThrow());
        dw.setCoDirector1(professorRepo.findById(request.getCoDirector1Id()).orElseThrow());
        if (request.getCoDirector2Id() != null) {
            dw.setCoDirector2(professorRepo.findById(request.getCoDirector2Id()).orElse(null));
        }

        DegreeWork saved = degreeRepo.save(dw);

        // Enviar mensaje asíncrono
        rabbitTemplate.convertAndSend(exchange, routingKey, saved);

        return saved;
    }
    @Override
    public List<DegreeWork> getAllDegreeWorks() {
        return degreeRepo.findAll();
    }

    @Override
    public DegreeWork getDegreeWorkById(Long id) {
        return degreeRepo.findById(id).orElse(null);
    }
}
