package com.stockSolido.stockSolido.model;

public class ConteoPorServicio {

    private String tipoServicio;
    private long total;

    public ConteoPorServicio() {}

    public ConteoPorServicio(String tipoServicio, long total) {
        this.tipoServicio = tipoServicio;
        this.total = total;
    }

    public String getTipoServicio() { return tipoServicio; }
    public void setTipoServicio(String tipoServicio) { this.tipoServicio = tipoServicio; }
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

}
