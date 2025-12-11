# Documento de Design Técnico - Projeto Final APIs e Microsserviços

## 1. DDD Estratégico e Mapeamento de Contextos

Com base na análise do código fonte atual (`lms-api`), identificamos os seguintes Contextos Delimitados (Bounded Contexts):

### 1.1. Identificação de Bounded Contexts

| Contexto | Responsabilidade | Entidades Principais (Linguagem Ubíqua) |
| :--- | :--- | :--- |
| **Gestão de Identidade (Identity)** | Gerenciar autenticação, autorização e perfis de usuários. | `Usuario`, `Administrador`, `Aluno`, `Professor` |
| **Gestão Acadêmica (Academic)** | Gerenciar a estrutura curricular, turmas e matrículas. | `Curso`, `Disciplina`, `Turma`, `Matricula` |
| **Aprendizagem e Avaliação (Learning Core)** | Gerenciar conteúdo, atividades, questionários, tentativas e entregas. É o **Core Domain**. | `Topicos`, `MaterialDeAula`, `Atividade`, `Questoes`, `Alternativas`, `Tentativa`, `AnaliseDesempenho` |

### 1.2. Context Map (Relacionamentos)

*   **Gestão de Identidade (U) -> (D) Gestão Acadêmica**:
    *   *Tipo*: **Open Host Service / Published Language**. O Identity fornece serviços de autenticação padrão para todos.
    *   *Acoplamento*: Acadêmica depende de Identidade para saber quem são os Alunos/Professores.

*   **Gestão de Identidade (U) -> (D) Aprendizagem**:
    *   *Tipo*: **Conformist**. Aprendizagem usa o ID do usuário diretamente.

*   **Gestão Acadêmica (U) -> (D) Aprendizagem**:
    *   *Tipo*: **Customer/Supplier**. Aprendizagem (Customer) precisa que Acadêmica (Supplier) forneça as Turmas e Matrículas para vincular conteúdos.
    *   *Justificativa*: Acoplamento forte. Uma atividade não existe sem uma Turma.

---

## 2. Design Interno (Fase 2)

Para a fase de Design Interno, focaremos no **Core Domain: Aprendizagem e Avaliação**.

### 2.1. Escolha Arquitetural: Clean Architecture (Opção B)

*   **Contexto Escolhido**: Aprendizagem e Avaliação (`Atividade`, `Questionario`, `Tentativa`).
*   **Justificativa**: Este domínio possui regras de negócio complexas (cálculo de notas, validação de tentativas, prazos, tipos de atividades). A Clean Architecture nos permite isolar essas regras (Use Cases) de detalhes como o Banco de Dados ou a API Web, facilitando testes unitários das regras de avaliação sem precisar subir o Spring Boot ou Banco de Dados.

### 2.2. Contrato da API (Exemplo para Atividades)

*   **GET /api/v1/turmas/{turmaId}/atividades**: Listar atividades da turma.
*   **POST /api/v1/atividades/questionario**: Criar nova atividade do tipo questionário.
    *   *Input (DTO)*: `{ "titulo": "...", "prazo": "...", "questoes": [...] }`
*   **POST /api/v1/atividades/{id}/tentativas**: Aluno submete uma resposta.

---

## 3. Plano de Migração (Fase 3)

### 3.1. Roteiro de Refatoração

1.  **Isolamento de Domínio**: Remover anotações JPA (`@Entity`, `@ManyToOne`) das classes de domínio do pacote `model` e movê-las para um sub-pacote `infrastructure.persistence`. As classes de domínio devem ser puras (POJOs).
2.  **Criação de DTOs e Mappers**: Garantir que nenhum Controller retorne a Entidade diretamente.
3.  **Inversão de Dependência**: Criar interfaces `Repository` no pacote de domínio (Use Cases) e implementá-las na camada de infraestrutura (Spring Data JPA).

### 3.2. Plano de Decomposição (Extração)

Transformar o contexto **Aprendizagem** em um Microsserviço independente (`lms-learning-service`).

*   **Integração**: O Monólito (`lms-academic-monolith`) se comunicará via **REST Síncrono** com o `lms-learning-service` para consultas em tempo real (ex: ver notas no boletim). Para eventos como "Aluno Matriculado", usaremos (futuramente) mensageria, mas inicialmente REST.
*   **ACL (Anticorruption Layer)**: Criar um serviço no Monólito que traduz chamadas locais de `AtividadeService` para chamadas HTTP ao novo microsserviço, mantendo o contrato antigo para não quebrar o código legado.

---

## 4. Análise Crítica (Postmortem)

### 4.1. Problema dos Dados Compartilhados
Atualmente, o novo Microsserviço `lms-learning-service` foi configurado para rodar com H2 (em memória) para desenvolvimento, enquanto o Monólito usa MySQL. Em produção, se ambos usassem o mesmo banco de dados "físico" mas tabelas separadas, teríamos um "Shared Database Integration".
**Risco**: Alterações de schema no banco podem quebrar ambos os serviços.
**Solução Ideal**: Database-per-service. O `lms-learning-service` deve ter seu próprio Schema/Database, acessível apenas por ele.

### 4.2. Consistência de Dados
No cenário anterior, uma anotação `@Transactional` garantia atomicidade. Agora, ao criar um Questionário:
1. O Monólito recebe a requisição.
2. O Monólito chama o Microsserviço via HTTP (Síncrono).
3. O Microsserviço salva no banco dele e retorna 200 OK.
4. O Monólito retorna ao usuário.
**Problema**: Se o Monólito precisasse salvar algo localmente *após* a chamada e falhasse, o Microsserviço já teria salvado (Inconsistência).
**Solução**: Padrão SAGA (Orquestração ou Coreografia) ou uso de transações distribuídas (não recomendado). Para este projeto, a Consistência Eventual via Mensageria (RabbitMQ/Kafka) seria mais robusta que HTTP.

### 4.3. Acoplamento Síncrono
O uso de `RestTemplate` (HTTP) cria acoplamento temporal. Se o `lms-learning-service` cair, a funcionalidade de criar questionário no Monólito para imediatamente com erro 500.
**Solução**: Implementar Circuit Breaker (Resilience4j) para falhar graciosamente ou usar comunicação assíncrona (Fire-and-Forget) para criações que não exigem resposta imediata.

### 4.4. Reflexão sobre Complexidade
A extração aumentou significativamente a complexidade acidental.
*   **Antes**: Apenas uma chamada de método Java (`service.save()`).
*   **Agora**: Serialização JSON, Latência de Rede, Tratamento de Erros HTTP, Configuração de Portas, Duplicação de DTOs.
*   **Veredito**: Para o tamanho atual do domínio (apenas CRUD de atividades), a arquitetura de Microsserviços é *Overengineering*. Contudo, para fins didáticos e preparação para escala massiva (onde o módulo de Aprendizagem recebe 100x mais tráfego que o Acadêmico), a separação se justifica.
