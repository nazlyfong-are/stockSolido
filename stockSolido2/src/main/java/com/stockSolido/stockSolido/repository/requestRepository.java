package com.stockSolido.stockSolido.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.stockSolido.stockSolido.model.ConteoPorServicio;
import com.stockSolido.stockSolido.model.TotalGeneral;
import com.stockSolido.stockSolido.model.requestModel;

public interface requestRepository extends MongoRepository<requestModel, String> {

    //busqueda confiltros combinados a la base de datos
    @Query("{ $or: [ " +
            "{ 'cliente.nombre':        { $regex: ?0, $options: 'i' } }, " +
            "{ 'servicio.tipoServicio': { $regex: ?0, $options: 'i' } }, " +
            "{ 'descripcion':           { $regex: ?0, $options: 'i' } }, " +
            "{ 'estado':                { $regex: ?0, $options: 'i' } }, " +
            "{ 'cliente.documento':     { $regex: ?0, $options: 'i' } }  " +
            "] }")
    Page<requestModel> buscarPorTexto(String busqueda, Pageable pageable);

    // Solo estado
    Page<requestModel> findByEstado(String estado, Pageable pageable);

    // Solo tipoServicio
    @Query("{ 'servicio.tipoServicio': ?0 }")
    Page<requestModel> findByTipoServicio(String tipoServicio, Pageable pageable);

    // Estado + texto
    @Query("{ $and: [ " +
            "{ 'estado': ?0 }, " +
            "{ $or: [ " +
            "{ 'cliente.nombre':        { $regex: ?1, $options: 'i' } }, " +
            "{ 'servicio.tipoServicio': { $regex: ?1, $options: 'i' } }, " +
            "{ 'descripcion':           { $regex: ?1, $options: 'i' } }, " +
            "{ 'cliente.documento':     { $regex: ?1, $options: 'i' } }  " +
            "] } " +
            "] }")
    Page<requestModel> buscarPorEstadoYTexto(String estado, String busqueda, Pageable pageable);

    // TipoServicio + texto
    @Query("{ $and: [ " +
            "{ 'servicio.tipoServicio': ?0 }, " +
            "{ $or: [ " +
            "{ 'cliente.nombre':        { $regex: ?1, $options: 'i' } }, " +
            "{ 'servicio.tipoServicio': { $regex: ?1, $options: 'i' } }, " +
            "{ 'descripcion':           { $regex: ?1, $options: 'i' } }, " +
            "{ 'cliente.documento':     { $regex: ?1, $options: 'i' } }  " +
            "] } " +
            "] }")
    Page<requestModel> buscarPorTipoYTexto(String tipoServicio, String busqueda, Pageable pageable);

    // Estado + tipoServicio (sin texto)
    @Query("{ 'estado': ?0, 'servicio.tipoServicio': ?1 }")
    Page<requestModel> findByEstadoAndTipoServicio(String estado, String tipoServicio, Pageable pageable);

    // Estado + tipoServicio + texto
    @Query("{ $and: [ " +
            "{ 'estado': ?0 }, " +
            "{ 'servicio.tipoServicio': ?1 }, " +
            "{ $or: [ " +
            "{ 'cliente.nombre':        { $regex: ?2, $options: 'i' } }, " +
            "{ 'servicio.tipoServicio': { $regex: ?2, $options: 'i' } }, " +
            "{ 'descripcion':           { $regex: ?2, $options: 'i' } }, " +
            "{ 'cliente.documento':     { $regex: ?2, $options: 'i' } }  " +
            "] } " +
            "] }")
    Page<requestModel> buscarPorEstadoTipoYTexto(String estado, String tipoServicio, String busqueda, Pageable pageable);

    //buscar finalizado x todos los campos
    @Query("{ $and: [ " +
        "{ 'estado': 'Finalizado' }, " +
        "{ $or: [ " +
            "{ 'cliente.nombre':        { $regex: ?0, $options: 'i' } }, " +
            "{ 'servicio.tipoServicio': { $regex: ?0, $options: 'i' } }, " +
            "{ 'descripcion':           { $regex: ?0, $options: 'i' } }, " +
            "{ 'cliente.documento':     { $regex: ?0, $options: 'i' } }  " +
        "] } " +
    "] }")
    Page<requestModel> buscarFinalizadosPorTodo(String termino, Pageable pageable);

    //buscar finalizados x tipo de servicio Y texto
    @Query("{ $and: [ " +
        "{ 'estado': 'Finalizado' }, " +
        "{ 'servicio.tipoServicio': ?0 }, " +
        "{ $or: [ " +
            "{ 'cliente.nombre':        { $regex: ?1, $options: 'i' } }, " +
            "{ 'servicio.tipoServicio': { $regex: ?1, $options: 'i' } }, " +
            "{ 'descripcion':           { $regex: ?1, $options: 'i' } }, " +
            "{ 'cliente.documento':     { $regex: ?1, $options: 'i' } }  " +
        "] } " +
    "] }")
    Page<requestModel> buscarFinalizadosConTipoYTexto(String tipoServicio, String busqueda, Pageable pageable);


    //conteo x tipo de servicio
    @Aggregation(pipeline = {
        "{ $group: { _id: '$servicio.tipoServicio', total: { $sum: 1 } } }",
        "{ $project: { tipoServicio: '$_id', total: 1, _id: 0 } }"
    })
    List<ConteoPorServicio> obtenerConteoPorTipoServicio();

    //total acumulado de solicitudes finalizadas
    @Aggregation(pipeline = {
        "{ $match: { estado: 'Finalizado' } }",
        "{ $group: { _id: null, total: { $sum: '$total' } } }",
        "{ $project: { total: 1, _id: 0 } }"
    })
    TotalGeneral obtenerTotalFinalizados();

    //proyeccion auxiliar para la agregacion $max
    interface MaxIdResult {
        Integer getMaxId();
    }
}