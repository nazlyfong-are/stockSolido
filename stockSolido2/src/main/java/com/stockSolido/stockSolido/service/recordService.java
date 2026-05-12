package com.stockSolido.stockSolido.service;

import com.stockSolido.stockSolido.model.ConteoPorServicio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.stockSolido.stockSolido.model.requestModel;
import java.util.List;

public interface recordService {

    Page<requestModel> buscarConFiltros(String tipoServicio, String busqueda, Pageable pageable);
    Long obtenerTotalFinalizados();
    List<ConteoPorServicio> obtenerConteoServicios();
}