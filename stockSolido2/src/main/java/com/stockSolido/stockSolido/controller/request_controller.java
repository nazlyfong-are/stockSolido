package com.stockSolido.stockSolido.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.stockSolido.stockSolido.config.SolicitudValidator;
import com.stockSolido.stockSolido.model.customersModel;
import com.stockSolido.stockSolido.model.requestModel;
import com.stockSolido.stockSolido.model.SolicitudEmbebida;    
import com.stockSolido.stockSolido.model.servicesModel;
import com.stockSolido.stockSolido.service.customersService;
import com.stockSolido.stockSolido.service.requestService;
import com.stockSolido.stockSolido.service.servicesService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/private/admin")
public class request_controller {

    @Autowired
    private requestService RequestService;

    @Autowired
    private servicesService ServicesService;

    @Autowired
    private customersService CustomersService;

    @Autowired
    private SolicitudValidator solicitudValidator;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/request")
    public String mostrarSolicitud(
            Model model,
            @RequestParam(required = false) String idSolicitud,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String orden,
            @RequestParam(required = false, defaultValue = "total") String ordenCampo,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String tipoServicio,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {

        String campoSort = "fecha".equals(ordenCampo) ? "fecha" : "total";
        Pageable pageable;
        if ("asc".equals(orden)) {
            pageable = PageRequest.of(page, size, Sort.by(campoSort).ascending());
        } else if ("desc".equals(orden)) {
            pageable = PageRequest.of(page, size, Sort.by(campoSort).descending());
        } else {
            pageable = PageRequest.of(page, size);
        }

        Page<requestModel> paginadoSolicitudes =
            RequestService.buscarConFiltros(estado, tipoServicio, busqueda, pageable);

        model.addAttribute("listaSolicitudes",   paginadoSolicitudes.getContent());
        model.addAttribute("currentPage",        paginadoSolicitudes.getNumber());
        model.addAttribute("totalPages",         paginadoSolicitudes.getTotalPages());
        model.addAttribute("totalItems",         paginadoSolicitudes.getTotalElements());
        model.addAttribute("terminoBusqueda",    busqueda);
        model.addAttribute("estadoActivo",       estado);
        model.addAttribute("tipoServicioActivo", tipoServicio);
        model.addAttribute("ordenActivo",        orden);
        model.addAttribute("ordenCampoActivo",   ordenCampo);

        model.addAttribute("listaClientes", CustomersService.listar());

        List<servicesModel> listaServicios = ServicesService.listar();
        model.addAttribute("tiposServicio", listaServicios);

        List<Map<String, Object>> tiposParaJS = new ArrayList<>();
        for (servicesModel s : listaServicios) {
            String tipo = s.getTipoServicio();
            if (tipo != null && !tipo.trim().isEmpty()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("tipoServicio",   tipo.trim());
                item.put("precioServicio", s.getPrecioServicio() != null ? s.getPrecioServicio() : 0L);
                tiposParaJS.add(item);
            }
        }

        String tiposServicioJson = "[]";
        try {
            tiposServicioJson = objectMapper.writeValueAsString(tiposParaJS);
        } catch (JsonProcessingException e) {
            // usa "[]" por defecto
        }
        model.addAttribute("tiposServicioJson", tiposServicioJson);

        if (idSolicitud != null) {
            model.addAttribute("solicitudActual", RequestService.buscar(idSolicitud));
        } else {
            model.addAttribute("solicitudActual", new requestModel());
        }

        return "request";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/guardarSolicitud")
    public String guardarSolicitud(
            @ModelAttribute requestModel solicitud,
            RedirectAttributes redirectAttrs) {

        String error = solicitudValidator.validar(solicitud);
        if (error != null) {
            redirectAttrs.addFlashAttribute("errorSolicitud", error);
            return "redirect:/private/admin/request";
        }

        boolean esNueva = solicitud.getId() == null || solicitud.getId().trim().isEmpty();

        if (esNueva) {
            solicitud.setId(null);
            int siguienteId = RequestService.listar()
                .stream()
                .mapToInt(requestModel::getIdSolicitud)
                .max()
                .orElse(0) + 1;
            solicitud.setIdSolicitud(siguienteId);
        } else {
            requestModel existente = RequestService.buscar(solicitud.getId());
            if (existente != null) {
                solicitud.setIdSolicitud(existente.getIdSolicitud());
            }
        }

        // Guardar la solicitud en su coleccion
        requestModel guardada = RequestService.guardar(solicitud);

        if (guardada.getCliente() != null
                && guardada.getCliente().getClienteId() != null
                && !guardada.getCliente().getClienteId().trim().isEmpty()) {

            customersModel cliente = CustomersService.buscar(guardada.getCliente().getClienteId());

            if (cliente != null) {
                // Construir el objeto embebido actualizado
                SolicitudEmbebida embebida = new SolicitudEmbebida();
                embebida.setIdSolicitud(guardada.getIdSolicitud());
                embebida.setTipoServicio(
                    guardada.getServicio() != null ? guardada.getServicio().getTipoServicio() : null
                );
                embebida.setDescripcion(guardada.getDescripcion());
                embebida.setEstado(guardada.getEstado());
                embebida.setFecha(guardada.getFecha());
                embebida.setHora(guardada.getHora());
                embebida.setNoServicios(guardada.getNoServicios());
                embebida.setTotal(guardada.getTotal());

                if (esNueva) {
                    // Agregar nueva entrada al array
                    cliente.getSolicitudes().add(embebida);
                } else {
                    // Reemplazar la entrada existente por idSolicitud
                    cliente.getSolicitudes().removeIf(
                        s -> s.getIdSolicitud() == guardada.getIdSolicitud()
                    );
                    cliente.getSolicitudes().add(embebida);
                }

                CustomersService.guardar(cliente);
            }
        }

        return "redirect:/private/admin/request";
    }

    @PreAuthorize("hasRole('ADMIN')")
@ResponseBody
@DeleteMapping("/eliminarSolicitud/{id}")
public ResponseEntity<String> eliminarSolicitud(@PathVariable String id) {

    requestModel solicitud = RequestService.buscar(id);

    if (solicitud == null) {
        return ResponseEntity.status(404).body("Solicitud no encontrada.");
    }

    //bloquear eliminacion si no esta finalizada
    String estado = solicitud.getEstado();
    if (estado == null || !estado.trim().equalsIgnoreCase("Finalizado")) {
        return ResponseEntity
            .status(409)
            .body("No se puede eliminar la solicitud porque está en estado \"" + estado + "\". " +
                  "Solo se pueden eliminar solicitudes Finalizadas.");
    }

    //sincronizar si no esta con cliente embebido
    if (solicitud.getCliente() != null
            && solicitud.getCliente().getClienteId() != null) {

        customersModel cliente = CustomersService.buscar(solicitud.getCliente().getClienteId());
        if (cliente != null) {
            cliente.getSolicitudes().removeIf(
                s -> s.getIdSolicitud() == solicitud.getIdSolicitud()
            );
            CustomersService.guardar(cliente);
        }
    }

    RequestService.eliminar(id);
    return ResponseEntity.ok("eliminado");
}
}