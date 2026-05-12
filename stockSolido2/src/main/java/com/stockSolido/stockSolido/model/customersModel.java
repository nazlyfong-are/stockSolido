package com.stockSolido.stockSolido.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "clientes")
public class customersModel {

    @Id
    private String id;

    private String tipo;
    private String documento;
    private String nombre;
    private String correo;
    private String telefono;

    // ARRAY de solicitudes embebidas dentro del cliente
    private List<SolicitudEmbebida> solicitudes = new ArrayList<>();

    public customersModel() {}

    public customersModel(String correo, String documento, String id,
                           String nombre, String telefono, String tipo) {
        this.correo    = correo;
        this.documento = documento;
        this.id        = id;
        this.nombre    = nombre;
        this.telefono  = telefono;
        this.tipo      = tipo;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    //Getter y Setter del array
    public List<SolicitudEmbebida> getSolicitudes() { return solicitudes; }
    public void setSolicitudes(List<SolicitudEmbebida> solicitudes) {
        this.solicitudes = solicitudes;
    }
}