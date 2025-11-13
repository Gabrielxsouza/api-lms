package br.ifsp.lms_api.service;

import java.util.Optional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.ifsp.lms_api.model.Aluno;
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
}