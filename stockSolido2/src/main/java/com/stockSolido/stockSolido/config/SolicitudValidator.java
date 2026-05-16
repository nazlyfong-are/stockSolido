package com.stockSolido.stockSolido.config;

import com.stockSolido.stockSolido.model.requestModel;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Reglas de validación:
 *   - La fecha es obligatoria
 *   - FIX 4: Solo en CREACIÓN se bloquean fechas pasadas.
 *            En EDICIÓN se permite cualquier fecha (para poder cambiar
 *            el estado de solicitudes ya agendadas).
 *   - No se permiten solicitudes en domingo
 *   - La hora debe estar dentro del horario laboral (7:00 - 18:30)
 *   - Se debe seleccionar un cliente
 *   - Se debe seleccionar un tipo de servicio
 *   - El no. de servicios debe ser mayor a 0
 */
@Component
public class SolicitudValidator {

    private static final LocalTime HORA_APERTURA = LocalTime.of(7, 0);
    private static final LocalTime HORA_CIERRE   = LocalTime.of(18, 30);

    /**
     * Valida una solicitud de servicio.
     *
     * @param solicitud la solicitud a validar
     * @return mensaje de error si hay un error, null si es válida
     */
    public String validar(requestModel solicitud) {

        LocalDate fecha = solicitud.getFecha();
        LocalTime hora  = solicitud.getHora();

        // Validar fecha obligatoria
        if (fecha == null)
            return "La fecha es obligatoria.";

        // =========================================================
        // FIX 4 — Verificar si es nueva (id null o vacío) o edición.
        //
        // ANTES: se bloqueaba fecha pasada siempre → imposible editar
        //        una solicitud de días anteriores.
        //
        // AHORA: la restricción de fecha futura solo aplica al CREAR.
        //        Al EDITAR solo se bloquea el domingo (siempre inválido).
        // =========================================================
        boolean esNueva = solicitud.getId() == null || solicitud.getId().trim().isEmpty();

        if (esNueva && fecha.isBefore(LocalDate.now()))
            return "No se pueden agendar solicitudes en fechas pasadas.";

        if (fecha.getDayOfWeek() == DayOfWeek.SUNDAY)
            return "No se pueden agendar solicitudes los domingos.";

        // Validar hora de trabajo
        if (hora == null)
            return "La hora es obligatoria.";

        if (hora.isBefore(HORA_APERTURA))
            return "El horario de atención inicia a las 7:00 AM.";

        if (hora.isAfter(HORA_CIERRE))
            return "El horario de atención finaliza a las 6:30 PM.";

        // Validar cliente embebido
        if (solicitud.getCliente() == null
                || solicitud.getCliente().getNombre() == null
                || solicitud.getCliente().getNombre().trim().isEmpty())
            return "Debe seleccionar un cliente.";

        // Validar servicio embebido
        if (solicitud.getServicio() == null
                || solicitud.getServicio().getTipoServicio() == null
                || solicitud.getServicio().getTipoServicio().trim().isEmpty())
            return "Debe seleccionar un tipo de servicio.";

        // Validar cantidad
        if (solicitud.getNoServicios() <= 0)
            return "El número de servicios debe ser mayor a 0.";

        return null; // null = solicitud válida
    }
}