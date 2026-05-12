package com.stockSolido.stockSolido.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class SolicitudEmbebida {

    private int idSolicitud;
    private String tipoServicio;
    private String descripcion;
    private String estado;
    private LocalDate fecha;
    private LocalTime hora;
    private int noServicios;
    private BigDecimal total;

    public SolicitudEmbebida() {}

    public int getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(int idSolicitud) { this.idSolicitud = idSolicitud; }

    public String getTipoServicio() { return tipoServicio; }
    public void setTipoServicio(String tipoServicio) { this.tipoServicio = tipoServicio; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }

    public int getNoServicios() { return noServicios; }
    public void setNoServicios(int noServicios) { this.noServicios = noServicios; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}