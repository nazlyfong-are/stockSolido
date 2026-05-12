package com.stockSolido.stockSolido.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.stockSolido.stockSolido.model.servicesModel;

public interface servicesService {

    List<servicesModel> listar();
    Page<servicesModel> obtenerPaginados(String busqueda, Pageable pageable);
    Page<servicesModel> listarPaginados(Pageable pageable);
    List<servicesModel> buscarPorTermino(String termino);
    servicesModel guardar(servicesModel servicio);
    servicesModel buscar(String id);
    void eliminar(String id);
    boolean existePorTipoServicio(String tipoServicio);
    servicesModel buscarPorTipo(String tipoServicio);

}
