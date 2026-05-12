package com.stockSolido.stockSolido.model;

public class ClienteEmbebido {

    private String clienteId;
    private String nombre;
    private String documento;
    private String tipo;
    private String correo;
    private String telefono;

    public ClienteEmbebido() {}

    public ClienteEmbebido(customersModel c) {
        this.clienteId  = c.getId();
        this.nombre     = c.getNombre();
        this.documento  = c.getDocumento();
        this.tipo       = c.getTipo();
        this.correo     = c.getCorreo();
        this.telefono   = c.getTelefono();
    }

    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}