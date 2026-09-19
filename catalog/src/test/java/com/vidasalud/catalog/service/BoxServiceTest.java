package com.vidasalud.catalog.service;

import com.vidasalud.catalog.dto.BoxRequest;
import com.vidasalud.catalog.exception.InsufficientCapacityException;
import com.vidasalud.catalog.exception.ResourceNotFoundException;
import com.vidasalud.catalog.model.Box;
import com.vidasalud.catalog.repository.BoxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Cubre la regla de negocio del caso VidaSalud: "el cupo del box disminuye
 * al confirmar la atención" y no puede quedar negativo ni superar el total.
 */
@ExtendWith(MockitoExtension.class)
class BoxServiceTest {

    @Mock
    private BoxRepository repository;

    private BoxService service;

    @BeforeEach
    void setUp() {
        service = new BoxService(repository);
    }

    private Box boxConCupos(int total, int disponible) {
        return Box.builder()
                .id(1L)
                .name("Box 1")
                .location("Piso 1")
                .totalCapacity(total)
                .availableCapacity(disponible)
                .active(true)
                .build();
    }

    @Test
    void crearBoxDejaCupoDisponibleIgualAlTotal() {
        BoxRequest request = new BoxRequest();
        request.setName("Box 2");
        request.setLocation("Piso 2");
        request.setTotalCapacity(10);

        when(repository.save(any(Box.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Box created = service.create(request);

        assertThat(created.getAvailableCapacity()).isEqualTo(10);
        assertThat(created.getActive()).isTrue();
    }

    @Test
    void confirmarUnaAtencionDescuentaCupoDisponible() {
        Box box = boxConCupos(10, 10);
        when(repository.findById(1L)).thenReturn(Optional.of(box));
        when(repository.save(any(Box.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Box actualizado = service.adjustCapacity(1L, -1);

        assertThat(actualizado.getAvailableCapacity()).isEqualTo(9);
    }

    @Test
    void noPermiteDescontarMasCupoDelDisponible() {
        Box box = boxConCupos(10, 0);
        when(repository.findById(1L)).thenReturn(Optional.of(box));

        assertThatThrownBy(() -> service.adjustCapacity(1L, -1))
                .isInstanceOf(InsufficientCapacityException.class);
    }

    @Test
    void liberarCupoNuncaSuperaLaCapacidadTotal() {
        Box box = boxConCupos(10, 9);
        when(repository.findById(1L)).thenReturn(Optional.of(box));
        when(repository.save(any(Box.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Se libera cupo (ej: al cancelar) más de lo que realmente estaba tomado.
        Box actualizado = service.adjustCapacity(1L, 5);

        assertThat(actualizado.getAvailableCapacity()).isEqualTo(10);
    }

    @Test
    void getByIdLanzaExcepcionSiElBoxNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
