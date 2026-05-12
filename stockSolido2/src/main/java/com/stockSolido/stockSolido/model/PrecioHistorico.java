package com.stockSolido.stockSolido.model;

import java.time.LocalDate;

public class PrecioHistorico {

    private Long precio;
    private LocalDate fechaCambio;
    private String motivo;

    public PrecioHistorico() {}

    public PrecioHistorico(Long precio, LocalDate fechaCambio, String motivo) {
        this.precio      = precio;
        this.fechaCambio = fechaCambio;
        this.motivo      = motivo;
    }

    public Long getPrecio() { return precio; }
    public void setPrecio(Long precio) { this.precio = precio; }
    public LocalDate getFechaCambio() { return fechaCambio; }
    public void setFechaCambio(LocalDate fechaCambio) { this.fechaCambio = fechaCambio; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}