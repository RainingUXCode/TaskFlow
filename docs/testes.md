# Documentação de testes — TaskFlow

## Objetivo

Verificar as regras de gerenciamento de tarefas, a consistência dos eventos publicados pelo padrão **Observer** e a atualização coordenada dos componentes Swing.

## Organização e execução

Os testes foram implementados em Java, com métodos de verificação e exceções em caso de falha, **sem JUnit e sem bibliotecas de teste externas**.

| Classe | Tipo | Quantidade de cenários |
| --- | --- | ---: |
| `com.taskflow.service.GerenciadorTarefasTeste` | Regras de negócio e contrato Observer | 9 |
| `com.taskflow.view.NotificacaoObservadorTeste` | Integração dos observadores e componentes Swing | 1 |
| **Total** | | **10** |

### Pré-requisitos

JDK com suporte ao Java 17 e ambiente gráfico para o teste Swing. A aplicação e os testes Swing utilizam a Event Dispatch Thread (EDT).

### Comandos (PowerShell, na raiz do projeto)

```powershell
$fontes = @(Get-ChildItem src -Recurse -Filter *.java | ForEach-Object FullName)
javac --release 17 -encoding UTF-8 -d out @fontes
java -cp out com.taskflow.service.GerenciadorTarefasTeste
java -cp out com.taskflow.view.NotificacaoObservadorTeste
```

Cada cenário aprovado imprime `OK - ...` no console. Uma verificação malsucedida lança um erro e encerra o processo sem conclusão bem-sucedida.

## Casos de teste — GerenciadorTarefasTeste

| ID | Cenário | Comportamento verificado |
| --- | --- | --- |
| GT-01 | Criação e identificadores | UUIDs distintos, normalização de título e descrição, status padrão **A Fazer** e inclusão na lista. |
| GT-02 | Alteração de status | Transições entre os três status sem perder os demais dados da tarefa. |
| GT-03 | Exclusão | Exclusão somente da tarefa indicada, preservando as demais. |
| GT-04 | Inscrição e remoção | Observador duplicado não recebe eventos duplicados; removê-lo não afeta outros inscritos. |
| GT-05 | Eventos e imutabilidade | Eventos publicados **após** a alteração, tipos e conteúdos corretos, lista de tarefas imutável e snapshot consistente. |
| GT-06 | Operações inválidas | Título vazio/nulo rejeitado; ID inexistente, status nulo, alteração repetida e exclusão inexistente não geram notificações indevidas. |
| GT-07 | Status inicial | Cadastro nos três status, compatibilidade da criação com status padrão e rejeição de status inicial nulo sem modificar o estado. |
| GT-08 | Isolamento de falhas | Exceção proposital em um observador não impede a entrega do evento ao observador seguinte nem elimina a tarefa criada. |
| GT-09 | Alteração durante notificação | Tentativa de modificar tarefas a partir de um callback é bloqueada; a notificação prossegue e operações posteriores continuam permitidas. |

**Nota sobre GT-08:** esse cenário provoca deliberadamente uma exceção. Um aviso correspondente no console é esperado e não indica falha do teste caso o cenário termine com `OK`.

## Caso de teste — NotificacaoObservadorTeste

| ID | Cenário | Comportamento verificado |
| --- | --- | --- |
| UI-01 | Integração dos três observadores | Criação, movimentação em ambos os sentidos e exclusão atualizam cartões, contadores e avisos; controles possuem nomes acessíveis e dicas; limites de status não exibem movimentações inválidas; busca ignora acentos/maiúsculas e examina somente o título; contadores mantêm totais completos; aviso expira automaticamente. |

O teste de integração registra `QuadroObservador`, `ContadorObservador` e `NotificacaoObservador` no mesmo `GerenciadorTarefas`. A duração da notificação é reduzida nesse teste para acelerar a verificação da expiração, sem alterar a configuração utilizada na aplicação.

## Resultados da validação

| Verificação | Resultado |
| --- | --- |
| Compilação Java com `--release 17` | Aprovada |
| 9 cenários do gerenciador | Aprovados |
| 1 cenário de integração Swing | Aprovado |
| **Total: 10 cenários** | **10 aprovados** |

**Ambiente de referência:** a verificação realizada para a preparação da entrega utiliza um JDK mais recente com compilação direcionada ao Java 17 (`--release 17`). Isso confirma a compatibilidade de compilação, não substitui uma execução específica em um runtime JDK 17.

## Escopo da cobertura

Os testes verificam regras de negócio e integração dos observadores. A avaliação visual de tipografia, cores, espaçamento e composição da interface deve ser feita por inspeção da aplicação em execução.
