package com.stockSolido.stockSolido.controller;

import com.stockSolido.stockSolido.model.customersModel;
import com.stockSolido.stockSolido.model.requestModel;
import com.stockSolido.stockSolido.model.SolicitudEmbebida;
import com.stockSolido.stockSolido.model.servicesModel;
import com.stockSolido.stockSolido.service.customersService;
import com.stockSolido.stockSolido.service.recordService;
import com.stockSolido.stockSolido.service.servicesService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/private/admin")
public class record_controller {

    @Autowired
    private recordService historialService;

    @Autowired
    private servicesService ServicioService;

    @Autowired
    private customersService CustomersService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/record")
    public String mostrarHistorial(
            Model model,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String orden,
            @RequestParam(required = false) String tipoServicio,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable;
        if ("asc".equals(orden)) {
            pageable = PageRequest.of(page, size, Sort.by("total").ascending());
        } else if ("desc".equals(orden)) {
            pageable = PageRequest.of(page, size, Sort.by("total").descending());
        } else {
            pageable = PageRequest.of(page, size, Sort.by("fecha").descending());
        }

        Page<requestModel> paginaSolicitudes =
            historialService.buscarConFiltros(tipoServicio, busqueda, pageable);

        model.addAttribute("listaSolicitudes",   paginaSolicitudes.getContent());
        model.addAttribute("currentPage",        paginaSolicitudes.getNumber());
        model.addAttribute("totalPages",         paginaSolicitudes.getTotalPages());
        model.addAttribute("totalItems",         paginaSolicitudes.getTotalElements());
        model.addAttribute("terminoBusqueda",    busqueda);
        model.addAttribute("tipoServicioActivo", tipoServicio);
        model.addAttribute("ordenActivo",        orden);

        model.addAttribute("totalGeneralReporte", historialService.obtenerTotalFinalizados());

        // FIX: LinkedHashMap null-safe, sin LocalDate ni listas anidadas
        List<servicesModel> listaServicios = ServicioService.listar();
        List<Map<String, Object>> tiposParaJS = new ArrayList<>();
        for (servicesModel s : listaServicios) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("tipoServicio", s.getTipoServicio() != null ? s.getTipoServicio() : "");
            tiposParaJS.add(item);
        }
        model.addAttribute("tiposServicioJS", tiposParaJS);

        return "record";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/record/cliente/{clienteId}")
    public String historialPorCliente(
            @PathVariable String clienteId,
            Model model) {

        customersModel cliente = CustomersService.buscar(clienteId);

        if (cliente == null) {
            model.addAttribute("solicitudesCliente", List.of());
            model.addAttribute("totalCliente",       0L);
        } else {
            List<SolicitudEmbebida> finalizadas = cliente.getSolicitudes()
                .stream()
                .filter(s -> "Finalizado".equals(s.getEstado()))
                .toList();

            long total = finalizadas.stream()
                .mapToLong(s -> s.getTotal() != null ? s.getTotal().longValue() : 0L)
                .sum();

            model.addAttribute("solicitudesCliente", finalizadas);
            model.addAttribute("totalCliente",       total);
            model.addAttribute("cliente",            cliente);
        }

        List<servicesModel> listaServicios = ServicioService.listar();
        List<Map<String, Object>> tiposParaJS = new ArrayList<>();
        for (servicesModel s : listaServicios) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("tipoServicio", s.getTipoServicio() != null ? s.getTipoServicio() : "");
            tiposParaJS.add(item);
        }
        model.addAttribute("tiposServicioJS", tiposParaJS);

        return "record";
    }
}