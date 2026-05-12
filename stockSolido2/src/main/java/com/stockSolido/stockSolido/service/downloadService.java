package com.stockSolido.stockSolido.service;


import java.time.LocalDate;

public interface downloadService {


    byte[] generarHistorialCompleto() throws Exception;
    byte[] generarPorEstado(String estado) throws Exception;
    byte[] generarIngresosPorPeriodo(LocalDate fechaInicio, LocalDate fechaFin) throws Exception;
    byte[] generarRankingServicios() throws Exception;
    byte[] generarClientesRegistrados() throws Exception;
    byte[] generarProximosServicios() throws Exception;
    byte[] generarServiciosPendientes() throws Exception;
    byte[] generarReportePorCliente(String documento) throws Exception;
}

