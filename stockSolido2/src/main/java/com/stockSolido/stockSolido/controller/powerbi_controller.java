package com.stockSolido.stockSolido.controller;

import com.stockSolido.stockSolido.repository.DownloadRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/powerbi")
public class powerbi_controller {

    @Autowired
    private DownloadRepository downloadRepository;

    /**
     * Devuelve el historial completo de solicitudes finalizadas.
     * Usado en Power BI para reportes de ingresos.
     */
    @PreAuthorize("hasRole('POWERBI')")
    @GetMapping("/ingresos")
    public List<Map<String, Object>> ingresos() {
        return downloadRepository.findHistorialCompleto();
    }

    /**
     * Devuelve la lista de clientes registrados con sus datos principales.
     */
    @PreAuthorize("hasRole('POWERBI')")
    @GetMapping("/clientes")
    public List<Map<String, Object>> clientes() {
        return downloadRepository.findClientesRegistrados();
    }

    /**
     * Devuelve el ranking de servicios ordenado por frecuencia de uso.
     */
    @PreAuthorize("hasRole('POWERBI')")
    @GetMapping("/ranking-servicios")
    public List<Map<String, Object>> rankingServicios() {
        return downloadRepository.findRankingServicios();
    }

    /**
     * Devuelve las solicitudes en estado "Pendiente".
     */
    @PreAuthorize("hasRole('POWERBI')")
    @GetMapping("/pendientes")
    public List<Map<String, Object>> pendientes() {
        return downloadRepository.findServiciosPendientes();
    }

    /**
     * Devuelve las solicitudes próximas ordenadas por fecha ascendente.
     */
    @PreAuthorize("hasRole('POWERBI')")
    @GetMapping("/proximos")
    public List<Map<String, Object>> proximos() {
        return downloadRepository.findProximosServicios();
    }
}