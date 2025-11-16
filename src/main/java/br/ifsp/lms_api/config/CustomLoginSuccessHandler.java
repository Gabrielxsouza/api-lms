package br.ifsp.lms_api.config;

import br.ifsp.lms_api.model.Administrador;
import br.ifsp.lms_api.model.Aluno;
import br.ifsp.lms_api.model.Professor;
import br.ifsp.lms_api.model.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {


        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        
        Usuario usuarioLogado = userDetails.getUsuario(); 


        Long usuarioId = usuarioLogado.getIdUsuario(); 
        String nomeUsuario = usuarioLogado.getNome();

        request.getSession().setAttribute("usuarioId", usuarioId);
        request.getSession().setAttribute("nomeUsuario", nomeUsuario);
        
        String targetUrl = "/";

       
        if (usuarioLogado instanceof Administrador) {
            targetUrl = "login/admin"; 
        
        } else if (usuarioLogado instanceof Professor) {
            targetUrl = "login/professor";
        
        } else if (usuarioLogado instanceof Aluno) {
            targetUrl = "login/alunos";
        
        } else {
            targetUrl = "/"; 
        }

        response.sendRedirect(targetUrl);
    }
}