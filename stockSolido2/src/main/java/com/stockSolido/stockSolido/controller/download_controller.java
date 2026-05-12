package com.stockSolido.stockSolido.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.stockSolido.stockSolido.service.downloadService;

import java.time.LocalDate;


@Controller
@RequestMapping("/private/admin")
public class download_controller {

    @Autowired
    private downloadService downloadService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/download")
    public String mostrarReportes() {
        return "download";
    }

    //historial
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reportes/historial")
    public ResponseEntity<byte[]> historialPdf() throws Exception {
        return buildResponse(downloadService.generarHistorialCompleto(), "historial_completo.pdf");
    }

    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reportes/historial/preview")
    public ResponseEntity<byte[]> historialPreview() throws Exception {
        return buildPreviewResponse(downloadService.generarHistorialCompleto());
    }

    //estado
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reportes/estado")
    public ResponseEntity<byte[]> estadoPdf(
            @RequestParam(required = false) String estado) throws Exception {
        String nombre = (estado != null && !estado.isBlank())
            ? "solicitudes_" + estado.replace(" ", "_").toLowerCase() + ".pdf"
            : "solicitudes_todos_estados.pdf";
        return buildResponse(downloadService.generarPorEstado(estado), nombre);
    }

    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reportes/estado/preview")
    public ResponseEntity<byte[]> estadoPreview(
            @RequestParam(required = false) String estado) throws Exception {
        return buildPreviewResponse(downloadService.generarPorEstado(estado));
    }

    // ingreso
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reportes/ingresos")
    public ResponseEntity<byte[]> ingresosPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) throws Exception {
        return buildResponse(downloadService.generarIngresosPorPeriodo(fechaInicio, fechaFin), "ingresos_periodo.pdf");
    }

    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reportes/ingresos/preview")
    public ResponseEntity<byte[]> ingresosPreview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) throws Exception {
        return buildPreviewResponse(downloadService.generarIngresosPorPeriodo(fechaInicio, fechaFin));
    }

    //ranking
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reportes/ranking")
    public ResponseEntity<byte[]> rankingPdf() throws Exception {
        return buildResponse(downloadService.generarRankingServicios(), "ranking_servicios.pdf");
    }

    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reportes/ranking/preview")
    public ResponseEntity<byte[]> rankingPreview() throws Exception {
        return buildPreviewResponse(downloadService.generarRankingServicios());
    }

    //clientes
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reportes/clientes")
    public ResponseEntity<byte[]> clientesPdf() throws Exception {
        return buildResponse(downloadService.generarClientesRegistrados(), "clientes_registrados.pdf");
    }

    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reportes/clientes/preview")
    public ResponseEntity<byte[]> clientesPreview() throws Exception {
        return buildPreviewResponse(downloadService.generarClientesRegistrados());
    }

    //proximos
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reportes/proximos")
    public ResponseEntity<byte[]> proximosPdf() throws Exception {
        return buildResponse(downloadService.generarProximosServicios(), "proximos_servicios.pdf");
    }

    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reportes/proximos/preview")
    public ResponseEntity<byte[]> proximosPreview() throws Exception {
        return buildPreviewResponse(downloadService.generarProximosServicios());
    }

    //pendientes
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reportes/pendientes")
    public ResponseEntity<byte[]> pendientesPdf() throws Exception {
        return buildResponse(downloadService.generarServiciosPendientes(), "servicios_pendientes.pdf");
    }

    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reportes/pendientes/preview")
    public ResponseEntity<byte[]> pendientesPreview() throws Exception {
        return buildPreviewResponse(downloadService.generarServiciosPendientes());
    }

    //x clientes
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reportes/cliente")
    public ResponseEntity<byte[]> clientePdf(
            @RequestParam String documento) throws Exception {
        return buildResponse(downloadService.generarReportePorCliente(documento),
            "reporte_cliente_" + documento + ".pdf");
    }

    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reportes/cliente/preview")
    public ResponseEntity<byte[]> clientePreview(
            @RequestParam String documento) throws Exception {
        return buildPreviewResponse(downloadService.generarReportePorCliente(documento));
    }

    //helpers
    private ResponseEntity<byte[]> buildResponse(byte[] pdf, String filename) {
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.APPLICATION_PDF)
            .contentLength(pdf.length)
            .body(pdf);
    }

    private ResponseEntity<byte[]> buildPreviewResponse(byte[] pdf) {
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
            .contentType(MediaType.APPLICATION_PDF)
            .contentLength(pdf.length)
            .body(pdf);
    }
}