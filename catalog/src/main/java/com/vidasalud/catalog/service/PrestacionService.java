package com.vidasalud.catalog.service;

import com.vidasalud.catalog.dto.PrestacionRequest;
import com.vidasalud.catalog.exception.ResourceNotFoundException;
import com.vidasalud.catalog.model.Prestacion;
import com.vidasalud.catalog.repository.PrestacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PrestacionService {

    private final PrestacionRepository repository;

    public Prestacion create(PrestacionRequest request) {
        Prestacion prestacion = Prestacion.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .availableSlots(request.getAvailableSlots())
                .active(true)
                .build();
        return repository.save(prestacion);
    }

    public List<Prestacion> findAll() {
        return repository.findAll();
    }

    public Prestacion getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la prestación con id " + id));
    }

    // PUT /api/catalog/services/{id} (precio/cupo)
    public Prestacion update(Long id, PrestacionRequest request) {
        Prestacion prestacion = getById(id);
        prestacion.setName(request.getName());
        prestacion.setDescription(request.getDescription());
        prestacion.setPrice(request.getPrice());
        prestacion.setAvailableSlots(request.getAvailableSlots());
        return repository.save(prestacion);
    }

    public void delete(Long id) {
        Prestacion prestacion = getById(id);
        repository.delete(prestacion);
    }
}
