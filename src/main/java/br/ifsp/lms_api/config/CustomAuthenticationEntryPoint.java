package br.ifsp.lms_api.config; // Garanta que é o mesmo pacote

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component 
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // Cria uma mensagem de erro JSON
        Map<String, String> erro = new HashMap<>();
        erro.put("status", "401");
        erro.put("erro", "Não autenticado");
        erro.put("mensagem", "Você precisa estar logado para acessar este recurso.");
        erro.put("path", request.getRequestURI());

        // Escreve o JSON na resposta
        response.getWriter().write(objectMapper.writeValueAsString(erro));
    }
}