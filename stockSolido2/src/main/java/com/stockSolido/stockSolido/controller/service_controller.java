package com.stockSolido.stockSolido.controller;

import java.time.LocalDate;

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

import com.stockSolido.stockSolido.model.PrecioHistorico;
import com.stockSolido.stockSolido.model.servicesModel;
import com.stockSolido.stockSolido.service.servicesService;

@Controller
@RequestMapping("/private/admin")
public class service_controller {

    @Autowired
    private servicesService serviceServi;

    //listar
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/services")
    public String mostrarServicios(
            Model model,
            @RequestParam(required = false) String idServicio,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String orden,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {

        
        Pageable pageable;
        if ("asc".equals(orden)) {
            pageable = PageRequest.of(page, size, Sort.by("precioServicio").ascending());
        } else if ("desc".equals(orden)) {
            pageable = PageRequest.of(page, size, Sort.by("precioServicio").descending());
        } else {
            pageable = PageRequest.of(page, size);
        }

        Page<servicesModel> paginadoServicios = serviceServi.obtenerPaginados(busqueda, pageable);

        //paginacion y filtros
        model.addAttribute("listaServicios",  paginadoServicios.getContent());
        model.addAttribute("currentPage",     paginadoServicios.getNumber());
        model.addAttribute("totalPages",      paginadoServicios.getTotalPages());
        model.addAttribute("totalItems",      paginadoServicios.getTotalElements());
        model.addAttribute("terminoBusqueda", busqueda);
        model.addAttribute("ordenActivo",     orden);

        // precargar modal
        if (idServicio != null) {
            model.addAttribute("servicioActual", serviceServi.buscar(idServicio));
            model.addAttribute("tituloModal",    "Editar Servicio");
        } else {
            model.addAttribute("servicioActual", new servicesModel());
            model.addAttribute("tituloModal",    "Agregar Servicio");
        }

        return "services";
    }

    //guardar/actualizar
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/guardarServicio")
    public String guardarServicio(
            @ModelAttribute servicesModel servicio,
            RedirectAttributes redirectAttrs) {

        boolean esNuevo = servicio.getId() == null || servicio.getId().trim().isEmpty();

        //validar precio antes de cualquier informacion
        if (servicio.getPrecioServicio() == null || servicio.getPrecioServicio() <= 0) {
            redirectAttrs.addFlashAttribute("errorServicio", "El precio debe ser mayor a 0.");
            return "redirect:/private/admin/services";
        }

        if (esNuevo) {
            //crear= verificar nombre unico y registrar precio inicial
            servicio.setId(null);

            if (serviceServi.existePorTipoServicio(servicio.getTipoServicio())) {
                redirectAttrs.addFlashAttribute("errorServicio",
                    "Ya existe un servicio con el nombre: " + servicio.getTipoServicio());
                return "redirect:/private/admin/services";
            }

            PrecioHistorico precioInicial = new PrecioHistorico(
                servicio.getPrecioServicio(),
                LocalDate.now(),
                "Precio inicial"
            );
            servicio.getHistorialPrecios().add(precioInicial);

        } else {
            //editar= añadir al historial solo si el precio cambio
            servicesModel existente = serviceServi.buscar(servicio.getId());
            if (existente != null
                    && !existente.getPrecioServicio().equals(servicio.getPrecioServicio())) {

                //conservar historial previo y añadir el nuevo precio
                servicio.setHistorialPrecios(existente.getHistorialPrecios());

                PrecioHistorico nuevoPrecio = new PrecioHistorico(
                    servicio.getPrecioServicio(),
                    LocalDate.now(),
                    "Actualización de precio"
                );
                servicio.getHistorialPrecios().add(nuevoPrecio);
            }
        }

        serviceServi.guardar(servicio);
        return "redirect:/private/admin/services";
    }

    //eliminar
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    @DeleteMapping("/eliminarServicio/{id}")
    public ResponseEntity<Void> eliminarServicio(@PathVariable String id) {
        serviceServi.eliminar(id);
        return ResponseEntity.ok().build();
    }
}