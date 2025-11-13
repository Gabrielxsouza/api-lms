package br.ifsp.lms_api.model;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuario")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Usuario implements UserDetails{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 3, max = 100)
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Formato de e-mail inválido")
    @Column(unique = true) 
    private String email;

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 6) 
    private String senha;

    @NotBlank(message = "O CPF é obrigatório")
    @Column(unique = true)
    private String cpf;

    @Column(name = "tipo_usuario", insertable = false, updatable = false)
    private String tipoUsuario;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Usa o valor da coluna "tipo_usuario" (ADMIN, ALUNO, PROFESSOR)
        // e transforma em uma "Role" que o Spring Security entende.
        // Ex: "ADMIN" vira "ROLE_ADMIN"
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.tipoUsuario));
    }

    @Override
    public String getPassword() {
        return this.senha; // Retorna o campo da senha
    }

    @Override
    public String getUsername() {
        return this.email; // isso aqui é pro email servir pra logar no lugar do nome
    }

    //ver depois
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}