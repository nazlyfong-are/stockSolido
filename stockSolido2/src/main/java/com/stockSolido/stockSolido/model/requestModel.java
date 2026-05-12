package com.stockSolido.stockSolido.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "solicitud")
public class requestModel {

    @Id
    private String id;

    private int idSolicitud;

    //Objeto embebido del cliente 
    private ClienteEmbebido cliente;

    //Objeto embebido del servicio
    private ServicioEmbebido servicio;

    private LocalDate fecha;
    private LocalTime hora;
    private int noServicios;
    private BigDecimal total;
    private String descripcion;
    private String estado;

    public requestModel() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(int idSolicitud) { this.idSolicitud = idSolicitud; }
    public ClienteEmbebido getCliente() { return cliente; }
    public void setCliente(ClienteEmbebido cliente) { this.cliente = cliente; }
    public ServicioEmbebido getServicio() { return servicio; }
    public void setServicio(ServicioEmbebido servicio) { this.servicio = servicio; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }
    public int getNoServicios() { return noServicios; }
    public void setNoServicios(int noServicios) { this.noServicios = noServicios; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}