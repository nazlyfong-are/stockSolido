package com.stockSolido.stockSolido.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.stockSolido.stockSolido.model.servicesModel;

public interface ServicesRepository extends MongoRepository<servicesModel, String> {

    List<servicesModel> findByTipoServicioContainingIgnoreCase(String tipoServicio);

    //consulta a la base de datos
    @Query("{ $or: [ " +
        "{ 'tipoServicio': { $regex: ?0, $options: 'i' } } " +
    "] }")
    Page<servicesModel> buscarPorTodo(String termino, Pageable pageable);

    boolean existsByTipoServicioIgnoreCase(String tipoServicio);

    servicesModel findByTipoServicioIgnoreCase(String tipoServicio);

}