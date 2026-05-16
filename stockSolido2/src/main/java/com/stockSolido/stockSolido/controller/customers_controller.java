package com.stockSolido.stockSolido.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

import com.stockSolido.stockSolido.config.ClienteValidator;
import com.stockSolido.stockSolido.model.customersModel;
import com.stockSolido.stockSolido.model.requestModel;
import com.stockSolido.stockSolido.service.customersService;
import com.stockSolido.stockSolido.service.requestService;   // MOD-1

import java.util.List;                                        // MOD-1

@Controller
@RequestMapping("/private/admin")
public class customers_controller {

    @Autowired
    private customersService CustomersService;

    @Autowired
    private ClienteValidator clienteValidator;

    // MOD-1: inyectar requestService para eliminar solicitudes huérfanas
    @Autowired
    private requestService RequestService;

    // listar - buscar cliente
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/customers")
    public String mostrarClientes(
            Model model,
            @RequestParam(required = false) String idCliente,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String tipo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<customersModel> paginadoClientes;

        if (tipo != null && !tipo.isEmpty() && busqueda != null && !busqueda.isEmpty()) {
            paginadoClientes = CustomersService.buscarPorTipoYNombre(tipo, busqueda, pageable);
        } else if (tipo != null && !tipo.isEmpty()) {
            paginadoClientes = CustomersService.listarPorTipo(tipo, pageable);
        } else if (busqueda != null && !busqueda.isEmpty()) {
            paginadoClientes = CustomersService.obtenerPaginados(busqueda, pageable);
        } else {
            paginadoClientes = CustomersService.obtenerPaginados(null, pageable);
        }

        model.addAttribute("listaClientes",   paginadoClientes.getContent());
        model.addAttribute("currentPage",     paginadoClientes.getNumber());
        model.addAttribute("totalPages",      paginadoClientes.getTotalPages());
        model.addAttribute("totalItems",      paginadoClientes.getTotalElements());
        model.addAttribute("terminoBusqueda", busqueda);
        model.addAttribute("tipoActivo",      tipo);

        if (idCliente != null) {
            model.addAttribute("clienteActual", CustomersService.buscar(idCliente));
            model.addAttribute("tituloModal",   "Editar Cliente");
        } else {
            model.addAttribute("clienteActual", new customersModel());
            model.addAttribute("tituloModal",   "Agregar Cliente");
        }

        return "customers";
    }

    // guardar o actualizar
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/guardarCliente")
    public String guardarCliente(
            @ModelAttribute customersModel cliente,
            RedirectAttributes redirectAttrs) {

        boolean esNuevo = cliente.getId() == null || cliente.getId().trim().isEmpty();

        // =========================================================
        // MOD-2 — Validar documento también en edición.
        //
        // ANTES: el listener JS hacía "if (id) return" y saltaba la
        //        validación completa al editar. El backend tampoco
        //        revalidaba el documento en edición.
        //
        // AHORA: clienteValidator.validar() se ejecuta siempre.
        //        El validador ya comprueba formato de doc/teléfono/correo.
        // =========================================================
        String errorFormato = clienteValidator.validar(cliente);
        if (errorFormato != null) {
            redirectAttrs.addFlashAttribute("errorCliente", errorFormato);
            return "redirect:/private/admin/customers";
        }

        if (esNuevo) {
            cliente.setId(null);
            if (CustomersService.existePorDocumento(cliente.getDocumento())) {
                redirectAttrs.addFlashAttribute("errorCliente",
                    "Ya existe un cliente registrado con el documento: " + cliente.getDocumento());
                return "redirect:/private/admin/customers";
            }
        } else {
            // En edición: si el documento cambió, verificar que el nuevo no esté en uso
            // por OTRO cliente (no por el mismo que se está editando)
            customersModel existente = CustomersService.buscar(cliente.getId());
            if (existente != null
                    && !existente.getDocumento().equals(cliente.getDocumento())
                    && CustomersService.existePorDocumento(cliente.getDocumento())) {
                redirectAttrs.addFlashAttribute("errorCliente",
                    "Ya existe otro cliente con el documento: " + cliente.getDocumento());
                return "redirect:/private/admin/customers";
            }
            // Preservar el array de solicitudes embebidas del cliente
            if (existente != null) {
                cliente.setSolicitudes(existente.getSolicitudes());
            }
        }

        CustomersService.guardar(cliente);
        return "redirect:/private/admin/customers";
    }

    // eliminar
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/eliminarCliente/{id}")
    @ResponseBody
    public ResponseEntity<Void> eliminarCliente(@PathVariable String id) {

        // =========================================================
        // MOD-1 — Eliminar en cascada: borrar también todas las
        //          solicitudes de este cliente en la colección "solicitud".
        //
        // ANTES: solo se borraba el documento del cliente. Las solicitudes
        //        quedaban huérfanas con un clienteId que ya no existe,
        //        contaminando el historial y los reportes de totales.
        //
        // AHORA: primero se eliminan todas sus solicitudes, luego el cliente.
        // =========================================================
        List<requestModel> todasLasSolicitudes = RequestService.listar();
        for (requestModel solicitud : todasLasSolicitudes) {
            if (solicitud.getCliente() != null
                    && id.equals(solicitud.getCliente().getClienteId())) {
                RequestService.eliminar(solicitud.getId());
            }
        }

        CustomersService.eliminar(id);
        return ResponseEntity.ok().build();
    }
}