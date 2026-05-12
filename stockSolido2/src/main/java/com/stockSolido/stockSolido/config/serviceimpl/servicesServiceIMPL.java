package com.stockSolido.stockSolido.config.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.stockSolido.stockSolido.model.servicesModel;
import com.stockSolido.stockSolido.repository.ServicesRepository;
import com.stockSolido.stockSolido.service.servicesService;

@Service
public class servicesServiceIMPL implements servicesService {

    @Autowired
    private ServicesRepository servicesRepo;

    @Override
    public List<servicesModel> listar() {
        return servicesRepo.findAll();
    }

    //buscar x ID
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public servicesModel buscar(String id) {
        return servicesRepo.findById(id).orElse(null);
    }

    //guardar/actualizar
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public servicesModel guardar(servicesModel servicio) {
        return servicesRepo.save(servicio);
    }

    //elimina x ID
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void eliminar(String id) {
        servicesRepo.deleteById(id);
    }

    //busqueda x termino
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<servicesModel> buscarPorTermino(String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            return servicesRepo.findAll();
        }
        return servicesRepo.findByTipoServicioContainingIgnoreCase(termino);
    }

    //listar paginado con busqueda
    /**
     * Devuelve una pagina de servicios con busqueda opcional sobre
     * tipoServicio y precio.
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Page<servicesModel> obtenerPaginados(String busqueda, Pageable pageable) {
        if (busqueda != null && !busqueda.isEmpty()) {
            return servicesRepo.buscarPorTodo(busqueda, pageable);
        }
        return servicesRepo.findAll(pageable);
    }

    //listar paginado sin busqueda
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Page<servicesModel> listarPaginados(Pageable pageable) {
        return servicesRepo.findAll(pageable);
    }

    //verificar unicidad por nombre
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public boolean existePorTipoServicio(String tipoServicio) {
        return servicesRepo.existsByTipoServicioIgnoreCase(tipoServicio);
    }

    //buscar x tipo exacto
    @Override
    public servicesModel buscarPorTipo(String tipoServicio) {
        return servicesRepo.findByTipoServicioIgnoreCase(tipoServicio);
    }
}