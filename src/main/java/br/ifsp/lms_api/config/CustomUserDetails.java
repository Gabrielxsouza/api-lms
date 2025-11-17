package br.ifsp.lms_api.config;

import br.ifsp.lms_api.model.Aluno;
import br.ifsp.lms_api.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class CustomUserDetails implements UserDetails {

    private Usuario usuario;

    public CustomUserDetails(Usuario usuario) {
        this.usuario = usuario;
    }



    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return usuario.getAuthorities();
    }

    @Override
    public String getPassword() {
        return usuario.getPassword();
    }

    @Override
    public String getUsername() { //esse aqui é o e-mail
        return usuario.getUsername();
    }

    public String getNome() {
        return usuario.getNome();
    }

    @Override
    public boolean isAccountNonExpired() {
        return usuario.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return usuario.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return usuario.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return usuario.isEnabled();
    }

    // --- MÉTODOS CUSTOMIZADOS (O MOTIVO DE CRIARMOS A CLASSE) ---

    /**
     * Retorna o ID do usuário logado.
     */
    public Long getId() {
        return usuario.getIdUsuario();
    }

    /**
     * Verifica se o usuário logado é um Aluno.
     */
    public boolean isAluno() {
        return usuario instanceof Aluno;
    }

    /**
     * Retorna a entidade Usuario (para uso INTERNO no Service, se necessário).
     * Evite usar isso no Controller.
     */
    public Usuario getUsuario() {
        return usuario;
    }
}
