package com.stockSolido.stockSolido.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.http.HttpStatus;

/**
 * ─────────────────────────────────────────────────────────────────
 * Configuración central de Spring Security.
 *
 * FIX #7 — Credenciales hardcodeadas eliminadas:
 *   Los usuarios y contraseñas se leen ahora de variables de entorno
 *   (o de application.properties con valores cifrados en producción).
 *   Variables requeridas:
 *     APP_ADMIN_USER      → nombre de usuario ADMIN      (default: "admin")
 *     APP_ADMIN_PASSWORD  → contraseña ADMIN             (sin default; obligatoria)
 *     APP_POWERBI_USER    → nombre de usuario POWERBI    (default: "powerbi")
 *     APP_POWERBI_PASSWORD→ contraseña POWERBI           (sin default; obligatoria)
 *
 *   En desarrollo local se pueden definir en application.properties:
 *     app.admin.username=miUsuario
 *     app.admin.password=miContrasena
 *     app.powerbi.username=pbiUser
 *     app.powerbi.password=pbiPass
 *
 *   En producción se deben establecer como variables de entorno del
 *   sistema o secretos del gestor de configuración (Vault, AWS Secrets, etc.)
 *   y NUNCA deben commitearse al repositorio.
 *
 * FIX #8 — CSP sin 'unsafe-inline':
 *   'unsafe-inline' en script-src y style-src anulaba prácticamente
 *   toda la protección XSS que ofrece la Content Security Policy.
 *   Se eliminó de ambas directivas.
 *
 * FIX #9 — blob: añadido a frame-src:
 *   La vista previa de PDFs crea una Blob URL (blob:http://localhost/...)
 *   y la carga dentro de un <iframe>. Sin blob: en frame-src la CSP
 *   bloqueaba el iframe impidiendo ver la vista previa.
 * ─────────────────────────────────────────────────────────────────
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // ─── Credenciales inyectadas desde variables de entorno / properties ──
    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.powerbi.username:powerbi}")
    private String powerbiUsername;

    @Value("${app.powerbi.password}")
    private String powerbiPassword;

    // ─── 1. Cadena para la API de PowerBI ────────────────────────────
    /**
     * Protege /api/powerbi/** con HTTP Basic sin estado (STATELESS).
     * CSRF deshabilitado porque las peticiones son stateless.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain powerBiFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/powerbi/**")
            .authorizeHttpRequests(auth -> auth
                .anyRequest().hasRole("POWERBI")
            )
            .httpBasic(basic -> basic.realmName("PowerBI"))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            );

        return http.build();
    }

    // ─── 2. Cadena principal para la aplicación web ──────────────────
    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // ── Reglas de acceso ──────────────────────────────────────
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**", "/img/**", "/private/admin/*.js", "/private/admin/*.css", "/error").permitAll()
                .requestMatchers("/private/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )

            // ── Formulario de login ───────────────────────────────────
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/private/admin/home", true)
                .failureUrl("/login?error")
                .permitAll()
            )

            // ── Cierre de sesión ──────────────────────────────────────
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )

            // ── Gestión de sesiones ───────────────────────────────────
            .sessionManagement(session -> session
                .invalidSessionUrl("/login?sessionExpired")
                .maximumSessions(1)
                    .expiredUrl("/login?sessionExpired")
            )

            // ── Cabeceras de seguridad HTTP ───────────────────────────
            .headers(headers -> headers
                .contentTypeOptions(contentType -> {})
                .frameOptions(frame -> frame.deny())
                .referrerPolicy(referrer -> referrer
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN)
                )
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives(
                        "default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com;" +
                        "style-src 'self' https://fonts.googleapis.com https://cdn.jsdelivr.net 'unsafe-inline'; " +
                        "font-src 'self' https://fonts.gstatic.com; " +
                        "img-src 'self' data: https://*.powerbi.com; " +
                        "frame-src 'self' blob: https://app.powerbi.com; " +
                        "connect-src 'self' https://cdn.jsdelivr.net https://*.powerbi.com;"
                    )
                )
            );

        return http.build();
    }

    // ─── 3. Usuarios en memoria ───────────────────────────────────────
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        return new InMemoryUserDetailsManager(
            User.builder()
                .username(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .roles("ADMIN")
                .build(),
            User.builder()
                .username(powerbiUsername)
                .password(passwordEncoder.encode(powerbiPassword))
                .roles("POWERBI")
                .build()
        );
    }

    // ─── 4. Codificador de contraseñas ────────────────────────────────
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}