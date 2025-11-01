package br.ifsp.lms_api.repository;

import br.ifsp.lms_api.model.AtividadeArquivos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AtividadeArquivosRepository extends JpaRepository<AtividadeArquivos, Long> {
    
    // Exemplo de busca customizada (Query Method do Spring Data):
    // "Encontre todas as atividades de arquivo onde a lista de 
    // arquivos permitidos contenha um tipo específico"
    List<AtividadeArquivos> findByArquivosPermitidosContains(String tipoArquivo);
}