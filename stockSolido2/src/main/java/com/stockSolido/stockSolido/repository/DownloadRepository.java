package com.stockSolido.stockSolido.repository;

import org.bson.Document;
import org.bson.types.Decimal128;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;


@Repository
public class DownloadRepository {

    @Autowired
    private MongoTemplate mongo;

    private static final String COL_SOLICITUDES = "solicitud";
    private static final String COL_CLIENTES    = "clientes";

    // Formatos de fecha que puede tener el campo en MongoDB
    private static final List<DateTimeFormatter> FORMATOS_FECHA = Arrays.asList(
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd")
    );

    //historial completo
    public List<Map<String, Object>> findHistorialCompleto() {
        Query q = new Query(Criteria.where("estado").is("Finalizado"))
                .with(Sort.by(Sort.Direction.DESC, "fecha"));
        return mapearSolicitudes(mongo.find(q, Document.class, COL_SOLICITUDES));
    }

    //solicitudes x estado
    public List<Map<String, Object>> findPorEstado(String estado) {
        Query q = (estado != null && !estado.isBlank())
                ? new Query(Criteria.where("estado").is(estado))
                : new Query();
        q.with(Sort.by(Sort.Direction.DESC, "fecha"));
        return mapearSolicitudes(mongo.find(q, Document.class, COL_SOLICITUDES));
    }

    //servicios x periodo 
    public List<Map<String, Object>> findIngresosPorPeriodo(LocalDate inicio, LocalDate fin) {
        Query q = new Query(Criteria.where("estado").is("Finalizado"))
                .with(Sort.by(Sort.Direction.ASC, "fecha"));
        List<Map<String, Object>> todos = mapearSolicitudes(
                mongo.find(q, Document.class, COL_SOLICITUDES));

        // Filtrar por fecha en memoria
        return todos.stream().filter(fila -> {
            LocalDate fecha = (LocalDate) fila.get("_fechaLocal");
            if (fecha == null) return true; // incluir si no se pudo parsear
            if (inicio != null && fecha.isBefore(inicio)) return false;
            if (fin    != null && fecha.isAfter(fin))     return false;
            return true;
        }).toList();
    }

    //servicios más solicitados
    public List<Map<String, Object>> findRankingServicios() {
        List<Document> pipeline = Arrays.asList(
            new Document("$group", new Document("_id", "$servicio.tipoServicio")
                    .append("cantidad", new Document("$sum", 1))),
            new Document("$sort", new Document("cantidad", -1))
        );

        List<Document> results = new ArrayList<>();
        mongo.getDb()
             .getCollection(COL_SOLICITUDES)
             .aggregate(pipeline)
             .into(results);

        List<Map<String, Object>> list = new ArrayList<>();
        for (Document d : results) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("tipo_servicio", nvl(d.getString("_id")));
            row.put("cantidad",      String.valueOf(d.getInteger("cantidad", 0)));
            list.add(row);
        }
        return list;
    }

    //TODOS los clientes registrados
    public List<Map<String, Object>> findClientesRegistrados() {
        Query q = new Query().with(Sort.by(Sort.Direction.ASC, "nombre"));
        List<Document> docs = mongo.find(q, Document.class, COL_CLIENTES);

        List<Map<String, Object>> list = new ArrayList<>();
        for (Document d : docs) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("nombre",    nvl(d.getString("nombre")));
            row.put("tipo",      nvl(d.getString("tipo")));
            row.put("documento", nvl(d.getString("documento")));
            row.put("telefono",  nvl(d.getString("telefono")));
            row.put("correo",    nvl(d.getString("correo")));
            list.add(row);
        }
        return list;
    }

    //proximos servicios desde la fecha en la que se genero el pdf
    public List<Map<String, Object>> findProximosServicios() {
        Query q = new Query(Criteria.where("estado").ne("Finalizado"))
                .with(Sort.by(Sort.Direction.ASC, "fecha"));
        List<Map<String, Object>> todos = mapearSolicitudes(
                mongo.find(q, Document.class, COL_SOLICITUDES));

        LocalDate hoy = LocalDate.now();
        return todos.stream().filter(fila -> {
            LocalDate fecha = (LocalDate) fila.get("_fechaLocal");
            return fecha != null && fecha.isAfter(hoy);
        }).toList();
    }

    //servicios pendientes (en espera o proceso)
    public List<Map<String, Object>> findServiciosPendientes() {
        Criteria c = Criteria.where("estado").in("En espera", "En proceso");
        Query q = new Query(c).with(Sort.by(Sort.Direction.ASC, "fecha"));
        return mapearSolicitudes(mongo.find(q, Document.class, COL_SOLICITUDES));
    }

    //info. del cliente x doc
    public Map<String, Object> findInfoCliente(String documento) {
        Query q = new Query(Criteria.where("documento").is(documento));
        Document d = mongo.findOne(q, Document.class, COL_CLIENTES);
        if (d == null) return null;

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("nombre",    nvl(d.getString("nombre")));
        row.put("tipo",      nvl(d.getString("tipo")));
        row.put("documento", nvl(d.getString("documento")));
        row.put("telefono",  nvl(d.getString("telefono")));
        row.put("correo",    nvl(d.getString("correo")));
        return row;
    }

    //solicitudes de un cliente
    public List<Map<String, Object>> findSolicitudesPorCliente(String documento) {
        Query q = new Query(Criteria.where("cliente.documento").is(documento))
                .with(Sort.by(Sort.Direction.DESC, "fecha"));
        return mapearSolicitudes(mongo.find(q, Document.class, COL_SOLICITUDES));
    }


    //HELPERS
    private List<Map<String, Object>> mapearSolicitudes(List<Document> docs) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Document d : docs) {
            Map<String, Object> row = new LinkedHashMap<>();

            //objeto embebido cliente
            Document cliente = d.get("cliente", Document.class);
            row.put("cliente",   cliente != null ? nvl(cliente.getString("nombre"))   : "–");
            row.put("documento", cliente != null ? nvl(cliente.getString("documento")) : "–");

            //objeto embebido servicio
            Document servicio = d.get("servicio", Document.class);
            row.put("tipo_servicio", servicio != null ? nvl(servicio.getString("tipoServicio")) : "–");

            row.put("estado", nvl(d.getString("estado")));

            //fecha= guardada como String en MongoDB 
            LocalDate fechaLocal = extraerFecha(d);
            row.put("_fechaLocal", fechaLocal); // para filtros internos
            row.put("fecha", fechaLocal != null
                    ? Date.from(fechaLocal.atStartOfDay(ZoneId.systemDefault()).toInstant())
                    : null);

            row.put("total", extraerDecimal(d, "total"));

            list.add(row);
        }
        return list;
    }

    /**
     *lee el campo "fecha" del documento independientemente de si esta guardado
     * como String, Date, o cualquier otro tipo que Spring pueda poner ahí.
     */
    private LocalDate extraerFecha(Document d) {
        Object val = d.get("fecha");
        if (val == null) return null;

        //si ya es un Date
        if (val instanceof Date) {
            return ((Date) val).toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }
        
        if (val instanceof String) {
            String s = ((String) val).trim();
            //quitar hora si viene "2025-03-15T00:00:00" o similar
            if (s.contains("T")) s = s.substring(0, s.indexOf('T'));
            for (DateTimeFormatter fmt : FORMATOS_FECHA) {
                try { return LocalDate.parse(s, fmt); }
                catch (DateTimeParseException ignored) {}
            }
        }
        return null;
    }

    /**
     * Extrae un campo numerico manejando Decimal128, BigDecimal, Integer,
     * Long, Double y String. Devuelve BigDecimal para consistencia.
     */
    private BigDecimal extraerDecimal(Document d, String campo) {
        Object val = d.get(campo);
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof Decimal128)  return ((Decimal128) val).bigDecimalValue();
        if (val instanceof BigDecimal)  return (BigDecimal) val;
        if (val instanceof Double)      return BigDecimal.valueOf((Double) val);
        if (val instanceof Long)        return BigDecimal.valueOf((Long) val);
        if (val instanceof Integer)     return BigDecimal.valueOf((Integer) val);
        try { return new BigDecimal(val.toString()); }
        catch (NumberFormatException e) { return BigDecimal.ZERO; }
    }

    /** Null-safe: devuelve "–" si el string es null */
    private String nvl(String s) {
        return s != null ? s.trim() : "–";
    }
}