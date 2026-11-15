# TaskFlow — Diagrama de classes (núcleo Observer)

Diagrama simplificado dos contratos e dependências mais relevantes. Os detalhes de layout Swing foram omitidos para facilitar a apresentação.

```mermaid
classDiagram
    direction TB
    class Observador {
        <<interface>>
        +atualizar(EventoTarefa) void
    }
    class GerenciadorTarefas {
        -tarefas Map~UUID,Tarefa~
        -observadores Set~Observador~
        +registrarObservador(Observador) void
        +removerObservador(Observador) void
        +criarTarefa(titulo,descricao,status) Tarefa
        +alterarStatus(id,status) boolean
        +excluirTarefa(id) boolean
        +listarTarefas() List~Tarefa~
        -notificar(tipo,tarefa) void
    }
    class EventoTarefa {
        <<record>>
        +tipo Tipo
        +tarefa Tarefa
        +tarefas List~Tarefa~
    }
    class Tarefa {
        <<record>>
        +id UUID
        +titulo String
        +descricao String
        +status Status
        +comStatus(Status) Tarefa
    }
    class Status {
        <<enumeration>>
        A_FAZER
        EM_ANDAMENTO
        CONCLUIDO
    }
    class QuadroObservador {
        +atualizar(EventoTarefa) void
    }
    class ContadorObservador {
        +atualizar(EventoTarefa) void
    }
    class NotificacaoObservador {
        +atualizar(EventoTarefa) void
    }
    class Main {
        +main(String[]) void
    }
    class TaskFlowFrame

    GerenciadorTarefas o-- "0..*" Observador : inscrições
    GerenciadorTarefas --> EventoTarefa : publica
    GerenciadorTarefas --> Tarefa : gerencia
    EventoTarefa --> Tarefa : afetada / snapshot
    Tarefa --> Status
    QuadroObservador ..|> Observador
    ContadorObservador ..|> Observador
    NotificacaoObservador ..|> Observador
    Main ..> GerenciadorTarefas : instancia
    Main ..> QuadroObservador : registra
    Main ..> ContadorObservador : registra
    Main ..> NotificacaoObservador : registra
    Main ..> TaskFlowFrame : monta janela
```

## Leitura do diagrama

1. **Subject:** `GerenciadorTarefas` armazena os dados e gerencia uma coleção de `Observador`.
2. **Contrato:** `Observador` declara `atualizar(EventoTarefa)`; o gerenciador não depende dos observadores concretos.
3. **Evento:** `EventoTarefa` carrega tipo, tarefa afetada e snapshot imutável da lista após a alteração.
4. **Observadores concretos:** o quadro, o contador e os avisos reagem separadamente.
5. **Composição:** `Main` constrói e registra os objetos.

O diagrama é didático e focaliza o padrão Observer; não representa cada método, componente ou relação visual do projeto.
