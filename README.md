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

* **Disciplinas:** Gerenciamento completo de disciplinas.
* **Turmas:**
    * Criação de turmas avulsas (vinculadas a uma disciplina existente).
    * Criação aninhada (criação de turmas ao mesmo tempo em que se cria uma disciplina).
    * Deleção em cascata (ao deletar uma disciplina, suas turmas são removidas).
* **Tópicos:** Gerenciamento de tópicos de aula vinculados a uma turma.
    * **Segurança:** Inclui sanitização de HTML para campos de "conteúdo" para prevenir ataques de XSS.
* **Material de Aula:**
    * Upload de arquivos (`MultipartFile`) associados a um tópico.
    * Integração com um serviço de armazenamento de arquivos.
* **Atividades (Polimorfismo):**
    * Estrutura de herança para `Atividade`.
    * Endpoints separados para `AtividadeTexto` (envio de texto) e `AtividadeArquivos` (envio de arquivos).
* **Questionários:** Estrutura base para `AtividadeQuestionario` e seu relacionamento com `Questoes` e `Alternativas`.

---

## 🗺️ Endpoints da API

A API segue os padrões RESTful. Os DTOs de `Update` utilizam `Optional` para permitir atualizações parciais (`PATCH`). Todas as listagens `GET` são paginadas.

### Disciplinas (`/disciplinas`)
* `POST /disciplinas`: Cria uma nova disciplina (com turmas aninhadas).
* `GET /disciplinas`: Lista todas as disciplinas (paginado).
* `PATCH /disciplinas/{id}`: Atualiza uma disciplina.
* `DELETE /disciplinas/{id}`: Deleta uma disciplina (e suas turmas em cascata).

### Turmas (`/turmas`)
* `POST /turmas`: Cria uma nova turma avulsa (vinculada a um `idDisciplina` existente).
* `GET /turmas`: Lista todas as turmas (paginado).
* `PATCH /turmas/{id}`: Atualiza uma turma.
* `DELETE /turmas/{id}`: Deleta uma turma.

### Tópicos (`/topicos`)
* `POST /topicos`: Cria um novo tópico (vinculado a um `idTurma`).
* `GET /topicos/turma/{idTurma}`: Lista todos os tópicos de uma turma específica (paginado).
* `GET /topicos/{id}`: Busca um tópico por ID.
* `PATCH /topicos/{id}`: Atualiza um tópico.
* `DELETE /topicos/{id}`: Deleta um tópico.

### Atividades
* `POST /atividades-texto`: Cria uma atividade de texto (vinculada a um `idTopico`).
* `POST /atividades-arquivo`: Cria uma atividade de envio de arquivo (vinculada a um `idTopico`).
* *(Endpoints `GET`, `PATCH`, `DELETE` seguem o mesmo padrão)*

### Material de Aula (`/materiais`)
* `POST /materiais/topico/{idTopico}`: Faz upload de um arquivo (`MultipartFile`) para um tópico.
* `GET /materiais/topico/{idTopico}`: Lista os materiais de um tópico.
* `PUT /materiais/{id}`: Atualiza o arquivo de um material.
* `DELETE /materiais/{id}`: Deleta um material (e o arquivo físico).