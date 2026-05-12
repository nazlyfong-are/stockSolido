package com.stockSolido.stockSolido.config.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.stockSolido.stockSolido.model.requestModel;
import com.stockSolido.stockSolido.repository.requestRepository;
import com.stockSolido.stockSolido.service.requestService;

@Service
public class requestServiceIMPL implements requestService {

    @Autowired
    private requestRepository RequestRepository;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<requestModel> listar() {
        return RequestRepository.findAll();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public requestModel buscar(String id) {
        return RequestRepository.findById(id).orElse(null);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public requestModel guardar(requestModel solicitud) {
        return RequestRepository.save(solicitud);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void eliminar(String id) {
        RequestRepository.deleteById(id);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Page<requestModel> listarPaginados(Pageable pageable) {
        return RequestRepository.findAll(pageable);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Page<requestModel> obtenerPaginados(String busqueda, Pageable pageable) {
        return buscarConFiltros(null, null, busqueda, pageable);
    }

    //busqueda con filtros dinamicos
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Page<requestModel> buscarConFiltros(
            String estado, String tipoServicio, String busqueda, Pageable pageable) {

        boolean hayEstado   = estado       != null && !estado.isEmpty();
        boolean hayTipo     = tipoServicio != null && !tipoServicio.isEmpty();
        boolean hayBusqueda = busqueda     != null && !busqueda.isEmpty();

        // 7 ramas = todas las combinaciones posibles
        if (hayEstado && hayTipo && hayBusqueda)
            return RequestRepository.buscarPorEstadoTipoYTexto(estado, tipoServicio, busqueda, pageable);

        if (hayEstado && hayTipo)
            return RequestRepository.findByEstadoAndTipoServicio(estado, tipoServicio, pageable);

        if (hayEstado && hayBusqueda)
            return RequestRepository.buscarPorEstadoYTexto(estado, busqueda, pageable);

        if (hayTipo && hayBusqueda)
            return RequestRepository.buscarPorTipoYTexto(tipoServicio, busqueda, pageable);

        if (hayEstado)
            return RequestRepository.findByEstado(estado, pageable);

        if (hayTipo)
            return RequestRepository.findByTipoServicio(tipoServicio, pageable);

        if (hayBusqueda)
            return RequestRepository.buscarPorTexto(busqueda, pageable);

        return RequestRepository.findAll(pageable);
    }
}