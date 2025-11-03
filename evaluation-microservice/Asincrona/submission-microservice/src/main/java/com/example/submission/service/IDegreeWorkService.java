package com.example.submission.service;

import com.example.submission.entity.DegreeWork;
import com.example.submission.infra.dto.DegreeWorkRequest;
import java.util.List;

public interface IDegreeWorkService {
    DegreeWork createDegreeWork(DegreeWorkRequest request);
    List<DegreeWork> getAllDegreeWorks();
    DegreeWork getDegreeWorkById(Long id);
}
