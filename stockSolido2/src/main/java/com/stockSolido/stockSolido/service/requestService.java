package com.stockSolido.stockSolido.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.stockSolido.stockSolido.model.requestModel;

public interface requestService {

    /**devuelve todas las solicitudes (para calcular correlativo) */
    List<requestModel> listar();

    /**devuelve una pagina con busqueda de texto opcional */
    Page<requestModel> obtenerPaginados(String busqueda, Pageable pageable);

    /**devuelve todas las solicitudes paginadas sin filtros adicionales*/
    Page<requestModel> listarPaginados(Pageable pageable);

    /**
     *busqueda con filtros dinamicos opcionales.
     */
    Page<requestModel> buscarConFiltros(
        String estado, String tipoServicio, String busqueda, Pageable pageable);

    /**persiste una solicitud (insert o update)*/
    requestModel guardar(requestModel solicitud);

    /**busca una solicitud por ID. Devuelve null si no existe*/
    requestModel buscar(String id);

    /**elimina una solicitud por ID */
    void eliminar(String id);
}