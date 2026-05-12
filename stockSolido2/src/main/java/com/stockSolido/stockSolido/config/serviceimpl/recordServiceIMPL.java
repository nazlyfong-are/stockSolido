package com.stockSolido.stockSolido.config.serviceimpl;

import com.stockSolido.stockSolido.model.ConteoPorServicio;
import com.stockSolido.stockSolido.model.TotalGeneral;
import com.stockSolido.stockSolido.model.requestModel;
import com.stockSolido.stockSolido.repository.requestRepository;
import com.stockSolido.stockSolido.service.recordService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class recordServiceIMPL implements recordService {

    @Autowired
    private requestRepository requestRepo;


    //busqueda combinada con filtros
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Page<requestModel> buscarConFiltros(
            String tipoServicio, String busqueda, Pageable pageable) {

        boolean hayTipo     = tipoServicio != null && !tipoServicio.isEmpty();
        boolean hayBusqueda = busqueda     != null && !busqueda.isEmpty();

        if (hayTipo && hayBusqueda) {
            return requestRepo.buscarFinalizadosConTipoYTexto(tipoServicio, busqueda, pageable);
        }
        if (hayTipo) {
            return requestRepo.findByEstadoAndTipoServicio("Finalizado", tipoServicio, pageable);
        }
        if (hayBusqueda) {
            return requestRepo.buscarFinalizadosPorTodo(busqueda, pageable);
        }

        return requestRepo.findByEstado("Finalizado", pageable);
    }

    //total acumulado de solicitudes finalizadas
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Long obtenerTotalFinalizados() {
        TotalGeneral resultado = requestRepo.obtenerTotalFinalizados();
        return (long) (resultado != null && resultado.getTotal() != null
            ? resultado.getTotal()
            : 0.0);
    }

    // conteo de solicitudes x tipo de servicio
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<ConteoPorServicio> obtenerConteoServicios() {
        return requestRepo.obtenerConteoPorTipoServicio();
    }
}