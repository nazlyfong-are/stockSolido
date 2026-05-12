package com.stockSolido.stockSolido.config;

import org.springframework.stereotype.Component;

import com.stockSolido.stockSolido.model.customersModel;

@Component
public class ClienteValidator {

    public String validar(customersModel cliente) {

        String documento = cliente.getDocumento();
        String telefono  = cliente.getTelefono();
        String nombre    = cliente.getNombre();
        String correo    = cliente.getCorreo();
        String tipo      = cliente.getTipo();

        // Nombre
        if (nombre == null || nombre.trim().length() < 3)
            return "El nombre debe tener al menos 3 caracteres.";

        // Correo
        if (correo == null || !correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
            return "El correo no tiene un formato válido.";

        // Telefono 
        if (telefono == null || !telefono.matches("^3\\d{9}$"))
            return "El teléfono debe tener 10 dígitos y empezar por 3.";

        // Documento segun tipo
        if ("Persona Natural".equals(tipo)) {
            if (documento == null || !documento.matches("^\\d{6,10}$"))
                return "La cédula debe tener entre 6 y 10 dígitos.";
        } else if ("Empresa".equals(tipo)) {
            if (documento == null || !documento.matches("^\\d{9,10}(-\\d{1})?$"))
                return "El NIT debe tener 9-10 dígitos (ej: 900123456-1).";
        }

        return null; // null = sin errores
    }
}