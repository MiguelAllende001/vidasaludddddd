package com.vidasalud.catalog.service;

import com.vidasalud.catalog.dto.PrestacionRequest;
import com.vidasalud.catalog.exception.ResourceNotFoundException;
import com.vidasalud.catalog.model.Prestacion;
import com.vidasalud.catalog.repository.PrestacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrestacionServiceTest {

    @Mock
    private PrestacionRepository repository;

    private PrestacionService service;

    @BeforeEach
    void setUp() {
        service = new PrestacionService(repository);
    }

    private Prestacion prestacionExistente() {
        return Prestacion.builder()
                .id(1L)
                .name("Consulta general")
                .description("Consulta médica general")
                .price(new BigDecimal("15000"))
                .availableSlots(20)
                .active(true)
                .build();
    }

    @Test
    void crearPrestacionQuedaActivaPorDefecto() {
        PrestacionRequest request = new PrestacionRequest();
        request.setName("Consulta dental");
        request.setDescription("Revisión dental general");
        request.setPrice(new BigDecimal("20000"));
        request.setAvailableSlots(10);

        when(repository.save(any(Prestacion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Prestacion created = service.create(request);

        assertThat(created.getActive()).isTrue();
        assertThat(created.getName()).isEqualTo("Consulta dental");
    }

    @Test
    void actualizarPrestacionSobrescribeLosDatos() {
        Prestacion existente = prestacionExistente();
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Prestacion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PrestacionRequest request = new PrestacionRequest();
        request.setName("Consulta general (actualizada)");
        request.setDescription("Nueva descripción");
        request.setPrice(new BigDecimal("18000"));
        request.setAvailableSlots(15);

        Prestacion actualizada = service.update(1L, request);

        assertThat(actualizada.getName()).isEqualTo("Consulta general (actualizada)");
        assertThat(actualizada.getPrice()).isEqualByComparingTo("18000");
        assertThat(actualizada.getAvailableSlots()).isEqualTo(15);
    }

    @Test
    void getByIdLanzaExcepcionSiNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void eliminarPrestacionDelegaEnElRepositorio() {
        Prestacion existente = prestacionExistente();
        when(repository.findById(1L)).thenReturn(Optional.of(existente));

        service.delete(1L);

        verify(repository).delete(existente);
    }

    @Test
    void findAllDevuelveLoQueRetornaElRepositorio() {
        when(repository.findAll()).thenReturn(List.of(prestacionExistente()));

        List<Prestacion> resultado = service.findAll();

        assertThat(resultado).hasSize(1);
    }
}
