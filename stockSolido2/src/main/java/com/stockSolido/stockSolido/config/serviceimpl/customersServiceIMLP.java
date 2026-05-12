package com.stockSolido.stockSolido.config.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.stockSolido.stockSolido.model.customersModel;
import com.stockSolido.stockSolido.repository.customersRepository;
import com.stockSolido.stockSolido.service.customersService;

@Service
public class customersServiceIMLP implements customersService {

    @Autowired
    private customersRepository customersRepo;

    //lista todos los clientes
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<customersModel> listar() {
        return customersRepo.findAll();
    }

    //buscar x ID
    /**
     * Busca un cliente por su ID de MongoDB
     * Devuelve null si no existe
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public customersModel buscar(String id) {
        return customersRepo.findById(id).orElse(null);
    }

    // guardar / actualizar
    /**
     * Persiste un cliente nuevo o actualiza uno existente
     * MongoDB distingue entre insert y update segun si el ID esta presente
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public customersModel guardar(customersModel cliente) {
        return customersRepo.save(cliente);
    }

    // eliminacion X ID
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void eliminar(String id) {
        customersRepo.deleteById(id);
    }

    // lista paginado con busqueda
    /**
     * Devuelve una pag. de clientes
     * Si se proporciona un termino de busqueda, aplica regex sobre
     * nombre, doc, correo, tel y tipo
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Page<customersModel> obtenerPaginados(String busqueda, Pageable pageable) {
        if (busqueda != null && !busqueda.isEmpty()) {
            return customersRepo.buscarPorTodo(busqueda, pageable);
        }
        return customersRepo.findAll(pageable);
    }

    //filtrar x tipo de cliente
    /**
     * Devuelve clientes filtrados por tipo (Persona Natural / Empresa)
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Page<customersModel> listarPorTipo(String tipo, Pageable pageable) {
        return customersRepo.findByTipo(tipo, pageable);
    }

    //filtrar x tipo + busqueda general
    /**
     * Combina filtro de tipo con busqueda de texto libre
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Page<customersModel> buscarPorTipoYNombre(String tipo, String busqueda, Pageable pageable) {
        return customersRepo.buscarPorTipoYTodo(tipo, busqueda, pageable);
    }

    // verificar existencia del documento
    /**
     * Verifica si ya existe un cliente con el no. de documento dado
     * Usado para evitar duplicados al registrar clientes nuevos
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public boolean existePorDocumento(String documento) {
        return customersRepo.existsByDocumento(documento);
    }
}