package com.vidasalud.catalog.service;

import com.vidasalud.catalog.dto.BoxRequest;
import com.vidasalud.catalog.exception.InsufficientCapacityException;
import com.vidasalud.catalog.exception.ResourceNotFoundException;
import com.vidasalud.catalog.model.Box;
import com.vidasalud.catalog.repository.BoxRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoxService {

    private final BoxRepository repository;

    public Box create(BoxRequest request) {
        Box box = Box.builder()
                .name(request.getName())
                .location(request.getLocation())
                .totalCapacity(request.getTotalCapacity())
                .availableCapacity(request.getTotalCapacity())
                .active(true)
                .build();
        return repository.save(box);
    }

    public List<Box> findAll() {
        return repository.findAll();
    }

    public Box getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el box con id " + id));
    }

    public Box update(Long id, BoxRequest request) {
        Box box = getById(id);
        box.setName(request.getName());
        box.setLocation(request.getLocation());
        box.setTotalCapacity(request.getTotalCapacity());
        return repository.save(box);
    }

    // Regla del caso: el cupo del box disminuye al confirmar la atención.
    // delta negativo = descuenta cupo (confirmar), positivo = libera cupo (cancelar).
    // @Transactional evita condiciones de carrera si dos confirmaciones
    // llegan casi al mismo tiempo para el mismo box.
    @Transactional
    public Box adjustCapacity(Long id, Integer delta) {
        Box box = getById(id);
        int nuevaDisponibilidad = box.getAvailableCapacity() + delta;

        if (nuevaDisponibilidad < 0) {
            throw new InsufficientCapacityException(
                    "No hay cupo suficiente en el box " + box.getId() +
                            " (disponible: " + box.getAvailableCapacity() + ", solicitado: " + Math.abs(delta) + ")");
        }
        if (nuevaDisponibilidad > box.getTotalCapacity()) {
            nuevaDisponibilidad = box.getTotalCapacity();
        }

        box.setAvailableCapacity(nuevaDisponibilidad);
        return repository.save(box);
    }

    public void delete(Long id) {
        Box box = getById(id);
        repository.delete(box);
    }
}
