package com.stockSolido.stockSolido.controller;

import com.stockSolido.stockSolido.repository.DownloadRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * ─────────────────────────────────────────────────────────────────
 * API REST para la integración con Power BI.
 *
 * Expone datos en formato JSON para que Power BI los consuma
 * mediante HTTP Basic con el usuario "powerbii" (rol POWERBI).
 *
 * BUG CORREGIDO:
 *   - La ruta base era "/private/admin/api" pero el SecurityConfig
 *     protege "/api/powerbi/**" con HTTP Basic + STATELESS.
 *     Se corrige la ruta base a "/api/powerbi" para que coincida
 *     con la cadena de filtros dedicada (powerBiFilterChain).
 *   - El rol requerido era ADMIN en lugar de POWERBI, lo que
 *     impedía que el usuario de Power BI pudiera autenticarse.
 *
 * Endpoints disponibles:
 *   GET /api/powerbi/ingresos          → historial completo de solicitudes
 *   GET /api/powerbi/clientes          → lista de clientes registrados
 *   GET /api/powerbi/ranking-servicios → conteo y ranking por tipo de servicio
 *   GET /api/powerbi/pendientes        → solicitudes en estado pendiente
 *   GET /api/powerbi/proximos          → próximas solicitudes programadas
 * ─────────────────────────────────────────────────────────────────
 */
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