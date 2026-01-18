package com.biblioteca.prestamos.scheduling;

import com.biblioteca.prestamos.service.PrestamoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrestamoScheduler {

    private final PrestamoService prestamoService;

    @Scheduled(cron = "0 0 6 * * *") // Ejecutar diariamente a las 6 AM
    public void actualizarEstadosPrestamos() {
        log.info("Iniciando actualización automática de estados de préstamos");
        try {
            prestamoService.actualizarEstadosAutomaticamente();
            log.info("Actualización de estados completada exitosamente");
        } catch (Exception e) {
            log.error("Error al actualizar estados de préstamos: {}", e.getMessage(), e);
        }
    }
}