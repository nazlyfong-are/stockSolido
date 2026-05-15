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


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    //credenciales inyectadas desde variables de entorno / properties
    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.powerbi.username:powerbi}")
    private String powerbiUsername;

    @Value("${app.powerbi.password}")
    private String powerbiPassword;

    //cadena para la API de PowerBI
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

    //cadena principal para la aplicacion web
    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            //reglas de acceso
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**", "/img/**", "/error").permitAll()
                .requestMatchers("/private/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )

            //formulario de login
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/private/admin/home", true)
                .failureUrl("/login?error")
                .permitAll()
            )

            //cierre de sesion
                .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )

            //gestion de sesiones
            .sessionManagement(session -> session
                .invalidSessionUrl("/login?sessionExpired")
                .maximumSessions(1)
                    .expiredUrl("/login?sessionExpired")
            )

            //cabeceras de seguridad HTTP
            .headers(headers -> headers
                .contentTypeOptions(contentType -> {})
                .frameOptions(frame -> frame.deny())
                .referrerPolicy(referrer -> referrer
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN)
                )
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives(
                        "default-src 'self'; " +
                        "script-src 'self' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; " +
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

    //usuarios en memoria
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

    //encriptar contraseña
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}