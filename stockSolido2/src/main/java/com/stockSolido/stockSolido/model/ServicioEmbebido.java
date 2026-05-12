package com.stockSolido.stockSolido.model;

public class ServicioEmbebido {

    private String tipoServicio;
    private Long precioUnitario;

    public ServicioEmbebido() {}

    public ServicioEmbebido(servicesModel s) {
        this.tipoServicio   = s.getTipoServicio();
        this.precioUnitario = s.getPrecioServicio();
    }

    public String getTipoServicio() { return tipoServicio; }
    public void setTipoServicio(String tipoServicio) { this.tipoServicio = tipoServicio; }
    public Long getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(Long precioUnitario) { this.precioUnitario = precioUnitario; }
}