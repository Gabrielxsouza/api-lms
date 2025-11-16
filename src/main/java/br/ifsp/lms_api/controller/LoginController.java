package br.ifsp.lms_api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.ifsp.lms_api.config.CustomUserDetails;


    @Validated
    @RestController
    @RequestMapping("/login")
    public class LoginController {

    @GetMapping
    @ResponseStatus(HttpStatus.UNAUTHORIZED) 
    public String paginaDeLogin() {
        return "Você precisa estar logado para acessar este recurso.";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin")
    public String paginaDeAdmin(@AuthenticationPrincipal CustomUserDetails usuarioLogado) {
        return "Bem vindo administrador " + usuarioLogado.getNome();
    }

    @PreAuthorize("hasRole('ROLE_PROFESSOR')")
    @GetMapping("/professor")
    public String paginaDeProfessor(@AuthenticationPrincipal CustomUserDetails usuarioLogado) {
        return "Bem vindo professor " + usuarioLogado.getNome();
    }

    @PreAuthorize("hasRole('ROLE_ALUNO')")
    @GetMapping("/alunos")
    public String paginaDeAluno(@AuthenticationPrincipal CustomUserDetails usuarioLogado) {
        return "Bem vindo aluno " + usuarioLogado.getNome();
    }
}
