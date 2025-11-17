package br.ifsp.lms_api.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;



@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private CustomLoginSuccessHandler customLoginSuccessHandler;


    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    public SecurityConfig(CustomLoginSuccessHandler customLoginSuccessHandler,
                          CustomAuthenticationEntryPoint customAuthenticationEntryPoint) {
        this.customLoginSuccessHandler = customLoginSuccessHandler;
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())

            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.disable())
            )

            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/",
                        "/login",
                        "/publico/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html/**",
                        "/v3/api-docs/**",
                        "/h2-console/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
                .successHandler(customLoginSuccessHandler)
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .permitAll()
            )

            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(customAuthenticationEntryPoint)
            );

        return http.build();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        String hierarchy = "ROLE_ADMIN > ROLE_PROFESSOR \n" +
                        "ROLE_ADMIN > ROLE_ALUNO";
    return RoleHierarchyImpl.fromHierarchy(hierarchy);
}


    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(roleHierarchy);
        return expressionHandler;
    }
}
