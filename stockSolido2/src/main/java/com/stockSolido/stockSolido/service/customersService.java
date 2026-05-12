package com.stockSolido.stockSolido.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.stockSolido.stockSolido.model.customersModel;

public interface customersService {

    /**devuelve todos los clientes (sin paginacion, para selects) */
    List<customersModel> listar();

    /**devuelve una pagina de clientes, con busqueda opcional */
    Page<customersModel> obtenerPaginados(String busqueda, Pageable pageable);

    /**persiste un cliente (insert o update)*/
    customersModel guardar(customersModel cliente);

    /**busca un cliente por su ID de Mongo, evuelve null si no existe */
    customersModel buscar(String id);

    /**elimina un cliente por su ID */
    void eliminar(String id);

    /**filtra clientes por tipo (Persona Natural / Empresa) */
    Page<customersModel> listarPorTipo(String tipo, Pageable pageable);

    /**combina filtros de tipo con busqueda de texto libre */
    Page<customersModel> buscarPorTipoYNombre(String tipo, String busqueda, Pageable pageable);

    /**verifica si ya existe un cliente con el documento dado*/
    boolean existePorDocumento(String documento);
}