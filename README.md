# API de LMS para Cursos de Exatas

Este projeto é a API back-end para um Sistema de Gerenciamento de Aprendizado (LMS) robusto e especializado, construído com Spring Boot. O foco do sistema é atender às necessidades específicas de cursos de ciências exatas, permitindo um gerenciamento detalhado de disciplinas, turmas, tópicos e atividades pedagógicas complexas.

## Contexto Acadêmico

Este projeto foi desenvolvido para as disciplinas de **GRUAPIM (APIs e Microsserviços)** e **GRUENGS (Engenharia de Software)** do Instituto Federal de São Paulo (IFSP) - Campus Guarulhos, sob a orientação do Professor Giovani.

### 👨‍💻 Equipe
* Gabriel de Souza Costa
* Gabriel Feitoza da Silva
* Lucas Bento da Silva Batista
* Maria Eduarda Alves Selvatti

---

## ✨ Funcionalidades Principais

A API suporta o CRUD (Create, Read, Update, Delete) completo e paginado para as principais entidades do sistema:

* **Gerenciamento de Usuários:**
    * Estrutura de herança (`SINGLE_TABLE`) para `Usuario` (Abstrato), `Aluno`, `Professor` e `Administrador`.
    * Endpoints CRUD completos para Alunos e Professores.
* **Segurança e Autenticação (Base):**
    * Adição do Spring Security para gerenciamento de autenticação.
    * Criptografia de senhas de usuários (usando `BCryptPasswordEncoder`) no banco de dados.
* **Disciplinas:** Gerenciamento completo de disciplinas.
* **Turmas:**
    * Criação de turmas avulsas (vinculadas a uma disciplina existente).
    * Criação aninhada (criação de turmas ao mesmo tempo em que se cria uma disciplina).
    * Deleção em cascata (ao deletar uma disciplina, suas turmas são removidas).
* **Gerenciamento de Tags (Análise de Erro):**
    * Endpoints CRUD completos para criar e gerenciar um "dicionário" de tags (ex: "Cálculo 1", "Derivadas").
    * Implementação dos pilares da "Análise de Erro" através de relacionamentos Many-to-Many:
        * `Tags <-> Tópicos`
        * `Tags <-> Atividades`
        * `Tags <-> Questões`
* **Tópicos:** Gerenciamento de tópicos de aula vinculados a uma turma.
    * **Segurança:** Inclui sanitização de HTML (OWASP) para campos de "conteúdo".
    * Permite a associação de Atividades e Tags já existentes.
* **Material de Aula:**
    * Upload de arquivos (`MultipartFile`) associados a um tópico.
    * Integração com um serviço de armazenamento de arquivos.
* **Atividades (Polimorfismo):**
    * Estrutura de herança para `Atividade`.
    * Endpoints separados para `AtividadeTexto`, `AtividadeArquivos` e `AtividadeQuestionario`.
* **Questionários:** Estrutura completa para `AtividadeQuestionario` e seu relacionamento N:M com `Questoes` e `Alternativas`.
* **População de Dados (Data Seeder):**
    * Inclui um `CommandLineRunner` (`DataInitializer`) que popula o banco de dados em memória (H2) em toda inicialização, facilitando os testes da API.
* **Documentação da API (Swagger):**
    * Geração automática de documentação da API e interface de testes via **Springdoc (Swagger UI)**.

---

## 🗺️ Endpoints da API

A API segue os padrões RESTful. Os DTOs de `Update` utilizam `Optional` para permitir atualizações parciais (`PATCH`). Todas as listagens `GET` são paginadas.

*(Acesse `http://localhost:8080/swagger-ui.html` para a documentação interativa completa)*

### Usuários (Alunos / Professores)
* `POST /alunos`, `GET /alunos`, `PATCH /alunos/{id}`, `DELETE /alunos/{id}`
* `POST /professores`, `GET /professores`, `PATCH /professores/{id}`, `DELETE /professores/{id}`

### Tags (Conteúdo)
* `POST /tags`, `GET /tags`, `GET /tags/{id}`, `PATCH /tags/{id}`, `DELETE /tags/{id}`

### Disciplinas
* `POST /disciplinas`: Cria uma nova disciplina (com turmas aninhadas).
* `GET /disciplinas`: Lista todas as disciplinas (paginado).
* `PATCH /disciplinas/{id}`: Atualiza uma disciplina.
* `DELETE /disciplinas/{id}`: Deleta uma disciplina (e suas turmas em cascata).

### Turmas
* `POST /turmas`: Cria uma nova turma avulsa (vinculada a um `idDisciplina` existente).
* `GET /turmas`: Lista todas as turmas (paginado).
* `PATCH /turmas/{id}`: Atualiza uma turma.
* `DELETE /turmas/{id}`: Deleta uma turma.

### Tópicos
* `POST /topicos`: Cria um novo tópico (vinculado a um `idTurma` e opcionalmente a `tagIds` e `idAtividade` existentes).
* `GET /topicos/turma/{idTurma}`: Lista todos os tópicos de uma turma específica (paginado).
* `GET /topicos/{id}`: Busca um tópico por ID (e exibe suas atividades e tags).
* `PATCH /topicos/{id}`: Atualiza um tópico (incluindo suas tags ou atividades).
* `DELETE /topicos/{id}`: Deleta um tópico.

### Atividades
* `POST /atividades-texto`: Cria uma nova atividade de texto (com `tagIds` opcionais).
* `POST /atividades-arquivo`: Cria uma nova atividade de envio de arquivo (com `tagIds` opcionais).
* `POST /atividades-questionario`: Cria um novo questionário (com `tagIds` opcionais).
* `POST /atividades-questionario/{id}/questoes`: Associa questões a um questionário.
* *(Endpoints `GET`, `PATCH`, `DELETE` seguem o mesmo padrão)*

### Questões e Alternativas
* `POST /questoes`: Cria uma nova questão (com alternativas aninhadas e `tagIds` opcionais).
* `GET /questoes`: Lista todas as questões (paginado).
* `PATCH /questoes/{id}`: Atualiza uma questão (enunciado ou tags).
* `DELETE /questoes/{id}`: Deleta uma questão.
* *(Endpoints CRUD para `/alternativas` também existem)*

### Material de Aula
* `POST /materiais/topico/{idTopico}`: Faz upload de um arquivo (`MultipartFile`) para um tópico.
* `GET /materiais/topico/{idTopico}`: Lista os materiais de um tópico.
* `PUT /materiais/{id}`: Atualiza o arquivo de um material.
* `DELETE /materiais/{id}`: Deleta um material (e o arquivo físico).