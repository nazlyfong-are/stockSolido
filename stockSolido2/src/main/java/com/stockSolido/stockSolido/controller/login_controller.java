package com.stockSolido.stockSolido.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class login_controller {

    /**
     * Muestra el formulario de login.
     *
     * @param error          presente si el login falla
     * @param logout         presente si el usuario cierra sesion
     * @param sessionExpired presente si la sesion expira
     * @param model          modelo Thymeleaf donde se inyectan mensajes
     * @return nombre de la plantilla "login"
     */
    @GetMapping("/login")
    public String showLogin(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            @RequestParam(required = false) String sessionExpired,
            Model model) {

        //mensajes de error
        if (error != null) {
            model.addAttribute("mensajeError", "Usuario o contraseña incorrectos.");
        }
        if (logout != null) {
            model.addAttribute("mensajeLogout", "Has cerrado sesión correctamente.");
        }
        if (sessionExpired != null) {
            model.addAttribute("mensajeError", "Tu sesión ha expirado. Por favor inicia sesión nuevamente.");
        }

        return "login";
    }
}