package com.stockSolido.stockSolido.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "servicios")
public class servicesModel {

    @Id
    private String id;

    private String tipoServicio;
    private Long precioServicio;

    //ARRAY de historial de precios
    private List<PrecioHistorico> historialPrecios = new ArrayList<>();

    public servicesModel() {}

    public servicesModel(String id, Long precioServicio, String tipoServicio) {
        this.id             = id;
        this.precioServicio = precioServicio;
        this.tipoServicio   = tipoServicio;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTipoServicio() { return tipoServicio; }
    public void setTipoServicio(String tipoServicio) { this.tipoServicio = tipoServicio; }
    public Long getPrecioServicio() { return precioServicio; }
    public void setPrecioServicio(Long precioServicio) { this.precioServicio = precioServicio; }

    public List<PrecioHistorico> getHistorialPrecios() { return historialPrecios; }
    public void setHistorialPrecios(List<PrecioHistorico> historialPrecios) {
        this.historialPrecios = historialPrecios;
    }
}