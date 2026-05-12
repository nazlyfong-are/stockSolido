package com.stockSolido.stockSolido.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.stockSolido.stockSolido.model.customersModel;

public interface customersRepository extends MongoRepository<customersModel, String> {

    //consulta a la base de datos
    @Query("{ $or: [ " +
        "{ 'nombre':   { $regex: ?0, $options: 'i' } }, " +
        "{ 'documento':{ $regex: ?0, $options: 'i' } }, " +
        "{ 'correo':   { $regex: ?0, $options: 'i' } }, " +
        "{ 'telefono': { $regex: ?0, $options: 'i' } }, " +
        "{ 'tipo':     { $regex: ?0, $options: 'i' } }  " +
    "] }")
    Page<customersModel> buscarPorTodo(String termino, Pageable pageable);

    Page<customersModel> findByTipo(String tipo, Pageable pageable);

    //consulta a la base de datos
    @Query("{ $and: [ " +
        "{ 'tipo': ?0 }, " +
        "{ $or: [ " +
            "{ 'nombre':   { $regex: ?1, $options: 'i' } }, " +
            "{ 'documento':{ $regex: ?1, $options: 'i' } }, " +
            "{ 'correo':   { $regex: ?1, $options: 'i' } }, " +
            "{ 'telefono': { $regex: ?1, $options: 'i' } }  " +
        "] } " +
    "] }")
    Page<customersModel> buscarPorTipoYTodo(String tipo, String termino, Pageable pageable);

    boolean existsByDocumento(String documento);
}