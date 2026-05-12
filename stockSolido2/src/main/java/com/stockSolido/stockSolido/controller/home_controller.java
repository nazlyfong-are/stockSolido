package com.stockSolido.stockSolido.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/private/admin")
public class home_controller {

    /**
     * Muestra la pantalla de inicio (home)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/home")
    public String showHome() {
        return "home";
    }

    /**
     * Muestra el dashboard
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dashboard")
    public String showDash() {
        return "dashboard";
    }
}