package br.ifsp.lms_api.initialData;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.ifsp.lms_api.model.*;
import br.ifsp.lms_api.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime; // <-- IMPORTAR
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    // (Repositórios existentes)
    private final TagRepository tagRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final TurmaRepository turmaRepository;
    private final TopicosRepository topicosRepository;
    private final QuestoesRepository questoesRepository;
    private final AlunoRepository alunoRepository;
    private final ProfessorRepository professorRepository;
    private final AdministradorRepository administradorRepository;
    private final AtividadeTextoRepository atividadeTextoRepository;
    private final AtividadeArquivosRepository atividadeArquivosRepository;
    private final AtividadeQuestionarioRepository atividadeQuestionarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final MatriculaRepository matriculaRepository;

    // --- ADICIONAR ESTES REPOSITÓRIOS ---
    private final MaterialDeAulaRepository materialDeAulaRepository;
    private final TentativaTextoRepository tentativaTextoRepository;
    private final TentativaQuestionarioRepository tentativaQuestionarioRepository;

    public DataInitializer(TagRepository tagRepository,
                           DisciplinaRepository disciplinaRepository,
                           TurmaRepository turmaRepository,
                           TopicosRepository topicosRepository,
                           QuestoesRepository questoesRepository,
                           AlunoRepository alunoRepository,
                           ProfessorRepository professorRepository,
                           AdministradorRepository administradorRepository,
                           AtividadeTextoRepository atividadeTextoRepository,
                           AtividadeArquivosRepository atividadeArquivosRepository,
                           AtividadeQuestionarioRepository atividadeQuestionarioRepository,
                           PasswordEncoder passwordEncoder,
                           MaterialDeAulaRepository materialDeAulaRepository,
                           TentativaTextoRepository tentativaTextoRepository,
                           TentativaQuestionarioRepository tentativaQuestionarioRepository,
                           MatriculaRepository matriculaRepository) {

        // (Atribuições existentes)
        this.tagRepository = tagRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.turmaRepository = turmaRepository;
        this.topicosRepository = topicosRepository;
        this.questoesRepository = questoesRepository;
        this.alunoRepository = alunoRepository;
        this.professorRepository = professorRepository;
        this.administradorRepository = administradorRepository;
        this.atividadeTextoRepository = atividadeTextoRepository;
        this.atividadeArquivosRepository = atividadeArquivosRepository;
        this.atividadeQuestionarioRepository = atividadeQuestionarioRepository;
        this.passwordEncoder = passwordEncoder;
        
        // --- ADICIONAR ESTAS ATRIBUIÇÕES ---
        this.materialDeAulaRepository = materialDeAulaRepository;
        this.tentativaTextoRepository = tentativaTextoRepository;
        this.tentativaQuestionarioRepository = tentativaQuestionarioRepository;
        this.matriculaRepository = matriculaRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println(">>> INICIANDO O DATA SEEDER (POPULANDO BANCO DE DADOS)...");

        // --- TAGS ---
        Tag tagCalculo = new Tag();
        tagCalculo.setNome("Cálculo 1");
        Tag tagDerivadas = new Tag();
        tagDerivadas.setNome("Derivadas");
        Tag tagP1 = new Tag();
        tagP1.setNome("Prova P1");
        Tag tagPOO = new Tag(); // <-- NOVA TAG
        tagPOO.setNome("POO");
        
        tagRepository.saveAll(List.of(tagCalculo, tagDerivadas, tagP1, tagPOO));

        // --- USUÁRIOS ---
        Aluno aluno = new Aluno();
        aluno.setNome("Maria Eduarda Alves Selvatti");
        aluno.setEmail("maria.selvatti@aluno.ifsp.edu.br");
        aluno.setSenha(passwordEncoder.encode("123456"));
        aluno.setCpf("123.456.789-00");
        aluno.setRa("GU3000001");
        alunoRepository.save(aluno);
        
        // (Aluno 2, Prof, Admin... - sem mudanças)
        Aluno aluno2 = new Aluno();
        aluno2.setNome("Gabriel Feitoza");
        aluno2.setEmail("gabrielfeitoza@aluno.ifsp.edu.br");
        aluno2.setSenha(passwordEncoder.encode("123456"));
        aluno2.setCpf("111.113.111-11");
        aluno2.setRa("GU3000002");
        alunoRepository.save(aluno2);

        Professor prof = new Professor();
        prof.setNome("Prof. Giovani");
        prof.setEmail("giovani@ifsp.edu.br");
        prof.setSenha(passwordEncoder.encode("123456"));
        prof.setCpf("987.654.321-00");
        prof.setDepartamento("Engenharia da Computação");
        professorRepository.save(prof);
        
        Administrador admin = new Administrador();
        admin.setNome("Admin");
        admin.setEmail("admin@lms.com");
        admin.setSenha(passwordEncoder.encode("admin123"));
        admin.setCpf("111.111.111-11");
        administradorRepository.save(admin);

        // --- ESTRUTURA DE CURSO ---
        Disciplina disciplina = new Disciplina();
        disciplina.setNomeDisciplina("Engenharia de Software");
        disciplina.setCodigoDisciplina("ESL708");
        disciplina.setDescricaoDisciplina("Disciplina de teste");

        Turma turma = new Turma();
        turma.setNomeTurma("Turma A - 2025");
        turma.setSemestre("2025/2");
        turma.setProfessor(prof);
        prof.setTurmas(List.of(turma));
        turma.setDisciplina(disciplina);
        disciplina.setTurmas(List.of(turma));
        disciplinaRepository.save(disciplina); 

        // --- TÓPICO 1 (Para Atividades) ---
        Topicos topico1 = new Topicos();
        topico1.setTituloTopico("Tópico 1 - Prova P1");
        topico1.setConteudoHtml("<p>Este tópico contém a P1.</p>");
        topico1.setTurma(turma); 
        topico1.setTags(Set.of(tagP1)); // Tópico geral da P1
        topicosRepository.save(topico1); 

        // --- ATIVIDADES (Associadas ao Tópico 1) ---
        AtividadeTexto atividadeTexto = new AtividadeTexto();
        atividadeTexto.setTituloAtividade("Redação P1 (Texto)");
        // ... (datas, status, etc.)
        atividadeTexto.setDataInicioAtividade(LocalDate.now());
        atividadeTexto.setDataFechamentoAtividade(LocalDate.now().plusDays(7));
        atividadeTexto.setStatusAtividade(true);
        atividadeTexto.setNumeroMaximoCaracteres(1000L); 
        atividadeTexto.setTags(Set.of(tagPOO)); // <-- MUDANÇA: Tag "POO" para testar nota boa
        atividadeTexto.setTopico(topico1); 
        atividadeTextoRepository.save(atividadeTexto);

        // (AtividadeArquivo - não vamos criar tentativa para ela por enquanto)
        AtividadeArquivos atividadeArquivo = new AtividadeArquivos();
        atividadeArquivo.setTituloAtividade("Upload P1 (Arquivo)");
        // ... (datas, status, etc.)
        atividadeArquivo.setDataInicioAtividade(LocalDate.now());
        atividadeArquivo.setDataFechamentoAtividade(LocalDate.now().plusDays(7));
        atividadeArquivo.setStatusAtividade(true);
        atividadeArquivo.setArquivosPermitidos(List.of(".pdf", ".zip"));
        atividadeArquivo.setTags(Set.of(tagP1));
        atividadeArquivo.setTopico(topico1); 
        atividadeArquivosRepository.save(atividadeArquivo);

        // --- QUESTÃO 1 (Errada pela aluna) ---
        Alternativas alt1 = new Alternativas(null, "2x", true, null); // Correta
        Alternativas alt2 = new Alternativas(null, "x^2", false, null); // Errada
        Questoes questao1 = new Questoes();
        questao1.setEnunciado("Qual a derivada de f(x) = x^2 ?");
        questao1.setTags(Set.of(tagCalculo, tagDerivadas)); // <-- Tags de Ponto Fraco
        questao1.setAlternativas(List.of(alt1, alt2));
        alt1.setQuestoes(questao1);
        alt2.setQuestoes(questao1);
        questoesRepository.save(questao1);

        // --- QUESTÃO 2 (Certa pela aluna) ---
        Alternativas alt3 = new Alternativas(null, "2", true, null); // Correta
        Alternativas alt4 = new Alternativas(null, "0", false, null); // Errada
        Questoes questao2 = new Questoes();
        questao2.setEnunciado("Qual o limite de (x^2-1)/(x-1) com x->1?");
        questao2.setTags(Set.of(tagCalculo)); // <-- Tag de Ponto Fraco/Misto
        questao2.setAlternativas(List.of(alt3, alt4));
        alt3.setQuestoes(questao2);
        alt4.setQuestoes(questao2);
        questoesRepository.save(questao2);

        // --- QUESTIONÁRIO (com as duas questões) ---
        AtividadeQuestionario questionario = new AtividadeQuestionario();
        questionario.setTituloAtividade("Simulado P1 de Cálculo");
        // ... (datas, status, etc.)
        questionario.setDataInicioAtividade(LocalDate.now());
        questionario.setDataFechamentoAtividade(LocalDate.now().plusDays(10));
        questionario.setStatusAtividade(true);
        questionario.setDuracaoQuestionario(60L);
        questionario.setNumeroTentativas(3);
        questionario.setTopico(topico1); 
        questionario.setTags(Set.of(tagP1, tagCalculo)); 
        questionario.setQuestoes(List.of(questao1, questao2)); // <-- Contém as 2 questões
        atividadeQuestionarioRepository.save(questionario);

        Topicos topicoDerivadas = new Topicos();
        topicoDerivadas.setTituloTopico("Tópico 2 - Revisão de Derivadas");
        topicoDerivadas.setConteudoHtml("<p>Material de apoio.</p>");
        topicoDerivadas.setTurma(turma); 
        topicoDerivadas.setTags(new HashSet<>(Set.of(tagDerivadas)));
         // <-- Linkado à tag "Derivadas"
        MaterialDeAula materialDerivadas = new MaterialDeAula();
        materialDerivadas.setNomeArquivo("Aula 05 - Regra da Cadeia.pdf");
        materialDerivadas.setUrlArquivo("/uploads/aula05.pdf");
        materialDerivadas.setTipoArquivo("application/pdf");

        materialDerivadas.setTopico(topicoDerivadas);
        topicoDerivadas.setMateriaisDeAula(new ArrayList<>(List.of(materialDerivadas)));

        topicosRepository.save(topicoDerivadas);
        materialDeAulaRepository.save(materialDerivadas);

        // 2. ADICIONAR TENTATIVA DE TEXTO (Nota Boa)
        TentativaTexto tentTexto = new TentativaTexto();
        tentTexto.setAluno(aluno); // Maria
        tentTexto.setAtividadeTexto(atividadeTexto); // Atividade de "POO"
        tentTexto.setTextoResposta("Polimorfismo é a capacidade...");
        tentTexto.setNota(9.0); // <-- NOTA BOA (9.0)
        tentTexto.setFeedBack("Excelente!");
        tentativaTextoRepository.save(tentTexto);

        // 3. ADICIONAR TENTATIVA DE QUESTIONÁRIO (Nota Ruim/Mista)
        // IDs das respostas que a Maria vai escolher:
        // Questão 1 (Derivada): Ela escolhe 'alt2' (ERRADA)
        // Questão 2 (Limite): Ela escolhe 'alt3' (CORRETA)
        List<Long> respostasMaria = List.of(alt2.getIdAlternativa(), alt3.getIdAlternativa());

        TentativaQuestionario tentQuest = new TentativaQuestionario();
        tentQuest.setAluno(aluno); // Maria
        tentQuest.setAtividadeQuestionario(questionario);
        tentQuest.setRespostas(respostasMaria); // <-- Respostas (1 errada, 1 certa)
        tentQuest.setNumeroDaTentativa(1);
        tentQuest.setDataEnvio(LocalDateTime.now());
        // A nota total será calculada pelo service, mas a nossa análise
        // vai recalcular por questão de qualquer forma.
        tentativaQuestionarioRepository.save(tentQuest);

        System.out.println(">>> Matriculando alunos na turma...");
        Matricula matriculaMaria = new Matricula();
        matriculaMaria.setAluno(aluno); // A 'Maria'
        matriculaMaria.setTurma(turma); // A 'Turma A'
        matriculaMaria.setStatusMatricula(Status.ATIVA); // <-- CORRIGIDO
        matriculaRepository.save(matriculaMaria);

        Matricula matriculaGabriel = new Matricula();
        matriculaGabriel.setAluno(aluno2); // O 'Gabriel'
        matriculaGabriel.setTurma(turma); // A 'Turma A'
        matriculaGabriel.setStatusMatricula(Status.ATIVA); // <-- CORRIGIDO
        matriculaRepository.save(matriculaGabriel);
                // --- FIM DAS NOVAS ADIÇÕES ---

        System.out.println(">>> DATA SEEDER CONCLUÍDO. APLICAÇÃO PRONTA!");
    }
}