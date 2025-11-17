package br.ifsp.lms_api.service;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.ifsp.lms_api.model.Usuario;
import br.ifsp.lms_api.repository.AlunoRepository;
import br.ifsp.lms_api.repository.UsuarioRepository;

@Service
public class AutentificacaoService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final AlunoRepository alunoRepository;

    public AutentificacaoService(UsuarioRepository usuarioRepository, AlunoRepository alunoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.alunoRepository = alunoRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        Optional<UserDetails> userDetails = usuarioRepository.findByEmail(username)
                .map(usuario -> (UserDetails) usuario);

        Optional<UserDetails> finalUserDetails = userDetails.or(() -> 
                alunoRepository.findByRa(username).map(aluno -> (UserDetails) aluno)
        );

        return finalUserDetails
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
    }

    // --- MÉTODO ADICIONADO ---
    /**
     * Busca a entidade Usuario (Professor, Aluno ou Admin) que está 
     * atualmente autenticada no contexto de segurança do Spring.
     * * @return A entidade Usuario logada.
     */
    public Usuario getUsuarioLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
             throw new UsernameNotFoundException("Nenhum usuário autenticado na sessão.");
        }
        
        // O "Principal" é o objeto UserDetails que retornamos no loadUserByUsername,
        // que no nosso caso é a própria entidade Usuario.
        return (Usuario) authentication.getPrincipal();
    }
}