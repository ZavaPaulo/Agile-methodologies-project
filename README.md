# Marketplace Acadêmico — Réplica independente

> **AVISO:** Este projeto é somente uma réplica acadêmica independente, sem vínculo, afiliação, patrocínio ou endosso do Mercado Livre. Não é um serviço oficial e não processa pagamentos reais.

## Documentação técnica do sistema

**Projeto:** Réplica simplificada de um marketplace inspirado no Mercado Livre  
**Repositório:** [Agile-methodologies-project](https://github.com/ZavaPaulo/Agile-methodologies-project)  
**Status:** Em desenvolvimento  
**Versão deste documento:** 1.1  
**Última atualização:** 12/11/2026

---

## 1. Visão geral

O **Agile Methodologies Projects** é um projeto acadêmico de desenvolvimento de software cujo objetivo é construir uma réplica simplificada de um marketplace, inspirada na experiência de navegação e compra do Mercado Livre.

A aplicação oferece uma interface web para catálogo, pesquisa, detalhes de produtos, carrinho e compra simulada. O backend Java mantém as regras de negócio; a interface de terminal permanece como implementação legada e apoio aos testes. O projeto também utiliza práticas de organização ágil para distribuir responsabilidades, acompanhar o progresso e facilitar a colaboração entre os integrantes.

> **Nota de escopo:** este documento descreve os requisitos identificados no planejamento do projeto e as decisões técnicas discutidas durante o desenvolvimento. Recursos ainda não implementados devem ser considerados planejados, não entregues.

## 2. Objetivos

### 2.1 Objetivo geral

Desenvolver uma aplicação de marketplace simplificada que permita cadastrar, consultar e excluir anúncios de produtos, gerenciar um carrinho de compras e simular a aquisição de produtos com atualização do estoque.

### 2.2 Objetivos específicos

- Implementar operações de cadastro, consulta, atualização e exclusão de produtos (CRUD).
- Apresentar os produtos em uma interface web conectada à API Java.
- Permitir adicionar e remover produtos do carrinho.
- Calcular subtotais e o valor total da compra.
- Simular a finalização de uma compra.
- Atualizar a quantidade disponível após uma compra.
- Identificar produtos de forma única.
- Organizar o trabalho em cards no Trello e manter o código-fonte em repositório Git.

## 3. Escopo funcional

### 3.1 Funcionalidades previstas

| ID | Funcionalidade | Descrição |
|---|---|---|
| RF-01 | Cadastrar produto | Registrar um produto com nome, descrição, preço e quantidade inicial em estoque. |
| RF-02 | Consultar produtos | Listar os produtos cadastrados e consultar um produto específico. |
| RF-03 | Atualizar produto | Editar os dados de um anúncio existente. |
| RF-04 | Excluir anúncio | Remover um anúncio do catálogo. |
| RF-05 | Adicionar ao carrinho | Incluir um produto e uma quantidade no carrinho. |
| RF-06 | Remover do carrinho | Retirar um item do carrinho. |
| RF-07 | Calcular total | Calcular subtotais e o total dos itens do carrinho. |
| RF-08 | Finalizar compra | Simular a confirmação de uma compra. |
| RF-09 | Atualizar estoque | Deduzir do estoque a quantidade comprada. |
| RF-10 | Controlar disponibilidade | Impedir compras acima do estoque e indicar produtos esgotados. |
| RF-11 | Identificar produtos | Atribuir um identificador único a cada produto para operações de consulta e gerenciamento. |

### 3.2 Fora do escopo confirmado

Não há confirmação, na definição atual do projeto, de integração com pagamentos reais, autenticação, contas de usuários, entrega, cálculo de frete, sistema de avaliações ou comunicação com os serviços reais do Mercado Livre. Esses recursos não devem ser considerados requisitos da versão atual sem aprovação do grupo.

## 4. Regras de negócio

- **RN-01 — Identificação:** cada produto deve possuir um identificador único e estável.
- **RN-02 — Nome:** o nome do produto é obrigatório e não pode ser vazio.
- **RN-03 — Descrição:** na implementação inicial, a descrição é obrigatória; o grupo pode rever essa regra caso decida torná-la opcional.
- **RN-04 — Preço:** o preço deve ser positivo e finito.
- **RN-05 — Estoque:** a quantidade disponível não pode ser negativa.
- **RN-06 — Compra:** a quantidade solicitada deve ser maior que zero e não pode ultrapassar o estoque disponível.
- **RN-07 — Reposição:** a quantidade adicionada ao estoque deve ser maior que zero.
- **RN-08 — Esgotamento:** quando o estoque chegar a zero, o produto deve ser indicado como esgotado.
- **RN-09 — Totalização:** os valores da compra devem ser calculados no backend ou na camada responsável pelas regras de negócio, e não dependerem exclusivamente da entrada informada no terminal.
- **RN-10 — Compra inválida:** uma compra recusada não deve alterar o estoque.
- **RN-11 — Exclusão:** a exclusão deve afetar o anúncio correto, identificado pelo seu ID. O comportamento para produtos que já estejam em um carrinho deve ser definido na integração.

## 5. Arquitetura lógica

A arquitetura abaixo descreve a organização por responsabilidades da aplicação: uma interface web que consome a API Java. A interface de terminal (`Main`) permanece como implementação legada e apoio aos testes.

```text
Frontend (HTML, CSS e JavaScript)
            |
            | HTTP / JSON (mesma origem)
            v
MarketplaceServer (API REST Java / JDK HttpServer)
            |
            +------> Catalogo / Produto / PesquisaProduto
            |
            +------> ItemCompra / DadosEntrega / Checkout
            |
            +------> RegistroPedidos / Pedido
            |
            v
Coleções em memória durante a execução
```

### 5.1 Regras de negócio e serviços

Responsável por validar as operações e garantir a consistência dos dados.

Responsabilidades previstas:

- Gerenciar o ciclo de vida dos produtos.
- Validar os dados de entrada.
- Controlar inclusão e remoção de itens do carrinho.
- Calcular valores da compra.
- Validar novamente o estoque ao finalizar a compra.
- Atualizar o estoque e indicar produtos esgotados.
- Gerenciar os dados em memória durante a execução.

### 5.2 Modelo `Produto`

A classe `Produto` representa um anúncio do catálogo. A implementação Java discutida pelo grupo contém os seguintes dados:

| Atributo | Tipo previsto | Finalidade |
|---|---|---|
| `idProduto` | `int` | Identificador único do produto. |
| `nomeProduto` | `String` | Nome exibido no catálogo. |
| `detalheProduto` | `String` | Descrição do produto. |
| `valor` | `double` | Preço unitário na implementação inicial. |
| `quantidadeDisponivel` | `int` | Quantidade disponível em estoque. |

A classe representa os dados de um produto. Esta documentação não detalha os métodos internos da classe, conforme solicitado.

> **Consideração técnica:** `double` é mantido aqui por compatibilidade com a implementação inicial. Para aplicações financeiras, é preferível usar `BigDecimal` e definir regras explícitas de arredondamento.

## 6. Organização do código

Estrutura sugerida, a ser adaptada às pastas existentes:

```text
src/
├── model/
│   └── Produto.java
├── service/
│   ├── ProdutoService.java
│   └── CarrinhoService.java
├── controller/             # se a arquitetura utilizar controllers
└── Main.java
```

Essa estrutura é uma sugestão conceitual, não uma descrição garantida da estrutura atual do repositório.

### Responsabilidades sugeridas

- `Produto`: representação dos dados de um produto.
- `ProdutoService`: regras de cadastro, atualização e exclusão.
- `CarrinhoService`: regras de carrinho e compra.
- `Main` ou camada de inicialização: iniciar a aplicação e conectar os componentes.

## 7. Fluxos principais

### 7.1 Cadastro de produto

1. O usuário seleciona a opção de cadastro no terminal e informa os dados solicitados.
2. A aplicação valida nome, descrição, preço e estoque.
3. O sistema cria um identificador único.
4. O produto é registrado em uma coleção em memória.
5. O terminal apresenta o resultado.

### 7.2 Adição ao carrinho

1. O usuário seleciona um produto e informa a quantidade.
2. O sistema localiza o produto pelo ID.
3. A aplicação verifica se o produto existe e se há estoque suficiente.
4. O item é incluído ou atualizado no carrinho.
5. O subtotal e o total são recalculados.
6. O terminal exibe o carrinho atualizado.

### 7.3 Finalização de compra

1. O usuário solicita a finalização.
2. O sistema verifica se o carrinho contém itens.
3. O backend valida novamente os produtos e o estoque.
4. O total é calculado com base nos dados válidos.
5. A compra simulada é confirmada.
6. O estoque é atualizado.
7. O carrinho é limpo e o terminal apresenta a confirmação.

Se alguma validação falhar, a compra deve ser recusada sem deduzir parcialmente o estoque. Em implementações mais robustas, a confirmação da compra e a atualização do estoque devem ser tratadas como uma operação atômica.

## 8. Estratégia de testes

Os testes devem cobrir os fluxos normais, entradas inválidas e situações de limite.

| Área | Cenários mínimos |
|---|---|
| Produto | Criar produto válido; rejeitar nome vazio, preço inválido e estoque negativo. |
| Identificador | Confirmar que produtos diferentes recebem IDs diferentes. |
| Estoque | Repor quantidade positiva; rejeitar zero e valores negativos; impedir estouro numérico. |
| Compra | Comprar quantidade válida; rejeitar quantidade zero, negativa ou acima do estoque. |
| Disponibilidade | Confirmar que estoque zero é exibido como esgotado. |
| CRUD | Cadastrar, consultar, editar e excluir o produto correto pelo ID. |
| Carrinho | Adicionar e remover itens; validar quantidades e conferir subtotais e total. |
| Integração | Validar o fluxo entre terminal, regras de negócio e coleções em memória. |

### 8.1 Testes automatizados disponíveis

Todos ficam em `MercadoLivre/src/` e são executados pelo `main` de cada classe (botão de executar do IntelliJ) ou pelo terminal, depois de compilar (ver seção 13.2):

| Classe | O que cobre |
|---|---|
| `TesteCatalogo` | Vitrine vazia, com um e com vários produtos; listas somente leitura. |
| `TesteDetalheProduto` | Detalhe de produto, ID inválido e catálogo vazio. |
| `TesteCheckout` | Registro de pedido, baixa de estoque, compra recusada e cancelamento. |
| `TesteApi` | Integração HTTP: servidor e frontend, catálogo e busca, carrinho, "Comprar agora" sem consumir o carrinho, finalização, CRUD, `PUT` atômico, exclusão e pedidos. |

```bash
java -cp out/production/Agile-methodologies-project TesteApi
```

## 9. Gestão do projeto e colaboração

O planejamento do trabalho é acompanhado no Trello, com listas de tarefas e cards que devem conter:

- Título identificável.
- Descrição do resultado esperado.
- Responsável principal.
- Checklist de execução.
- Critério de conclusão.
- Etiquetas de área, como Backend, Integração ou Testes.

O código-fonte é mantido no GitHub. Recomenda-se que cada integrante trabalhe em alterações delimitadas, escreva mensagens de commit descritivas e revise mudanças antes da integração à versão principal.

### Divisão inicial de responsabilidades

| Integrante | Responsabilidade sugerida |
|---|---|
| Paulo Zava | Modelo e operações de produtos no backend. |
| Bruno Santana | Carrinho, finalização de compra e regras de estoque. |
| Guilherme Mota | Fluxo de catálogo, cadastro e gerenciamento de anúncios pelo terminal. |
| Henrique | Fluxo do carrinho e da compra pelo terminal. |
| Rafael Schnorr | Integração, testes, documentação e preparação da entrega. |

A distribuição acima é uma proposta de planejamento e deve ser confirmada pelo grupo.

## 10. Critérios de aceite do projeto

A versão será considerada pronta para demonstração quando:

- [ ] O usuário puder cadastrar e consultar produtos.
- [ ] Os anúncios puderem ser editados e excluídos.
- [ ] Cada produto tiver um identificador único e estável.
- [ ] O usuário puder adicionar e remover itens do carrinho.
- [ ] Os subtotais e o total forem calculados corretamente.
- [ ] A compra simulada validar o estoque antes de confirmar.
- [ ] O estoque for reduzido somente após uma compra válida.
- [ ] Produtos com estoque zero forem identificados como esgotados.
- [ ] Os principais cenários de sucesso e erro forem testados.
- [ ] O repositório contiver instruções de configuração e execução pelo terminal.
- [ ] Os cards de entrega estiverem revisados e concluídos no Trello.

## 11. Como executar

- **Pré-requisitos:** JDK 21 (o projeto IntelliJ usa `zulu-21`; o código exige no mínimo Java 19, por causa de `Locale.of`). Navegador atualizado.
- **Instalação:** não há dependências externas nem ferramenta de build; o servidor usa o módulo `jdk.httpserver` do próprio JDK.
- **Configuração:** opcional. A porta padrão é 8080 e pode ser trocada pelo primeiro argumento ou pela variável de ambiente `PORT`. A pasta do frontend é localizada automaticamente; para forçar outra, use `-Dmarketplace.frontend=<pasta>`.
- **Execução:** comandos de compilação e inicialização na seção 13.2.
- **Testes:** classes `Teste*` descritas na seção 8.1.

Os produtos, carrinhos e pedidos ficam em memória e são reiniciados quando o servidor é encerrado.

## 12. Referências do projeto

- **Repositório GitHub:** https://github.com/ZavaPaulo/Agile-methodologies-project
- **Site de referência visual:** https://www.mercadolivre.com.br/
- **Quadro Trello:** https://trello.com/invite/b/6ad0d940df775bc707fd4948/ATTI18bbbaebbae001a04f49d719de47a7208EF0F22F/agil-methodologies-projects

---

## Histórico de versões

| Versão | Data | Descrição |
|---|---|---|
| 1.0 | 16/10/2026 | Criação da documentação inicial de visão geral, escopo, regras de negócio, arquitetura proposta, testes e organização do projeto. |
| 1.1 | 12/11/2026 | Integração frontend/backend revisada: tela de gerenciamento de anúncios, correção do "Comprar agora", `PUT` atômico, sessão tolerante a cookies entre aspas, localização automática do frontend, testes de API e correção da formatação desta documentação. |

**Responsáveis pela manutenção da documentação:** equipe do Agile Methodologies Projects.


## 13. Integração do frontend com o backend

A branch `IntegracaoFronBack` conecta as páginas web ao backend Java por uma API HTTP que trafega JSON. O servidor também entrega os arquivos estáticos do frontend na mesma origem, evitando configuração de CORS para a execução local.

### 13.1 Pré-requisitos

- JDK 21, conforme configurado no projeto IntelliJ (mínimo: Java 19).
- Git para obter a branch.
- Navegador atualizado.

### 13.2 Como executar

Na raiz do repositório, compile as classes Java:

```bash
javac --add-modules jdk.httpserver -encoding UTF-8 -d out/production/Agile-methodologies-project MercadoLivre/src/*.java
```

Inicie o servidor:

```bash
java --add-modules jdk.httpserver -Dmarketplace.frontend=MercadoLivre/frontEnd -cp out/production/Agile-methodologies-project MarketplaceServer
```

Abra `http://localhost:8080` no navegador. Para usar outra porta, informe o número como primeiro argumento, por exemplo `MarketplaceServer 9090`.

Ao iniciar, o servidor imprime a pasta do frontend que está usando. Ele a localiza automaticamente tanto na raiz do repositório quanto dentro de `MercadoLivre/` (útil para o IntelliJ); a propriedade `-Dmarketplace.frontend` continua disponível para forçar outro caminho.

Para executar os testes de integração da API (o servidor é iniciado e encerrado pelo próprio teste, em outra porta):

```bash
java --add-modules jdk.httpserver -cp out/production/Agile-methodologies-project TesteApi
```

### 13.3 Endpoints da API

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/api/health` | Verifica se o servidor está ativo |
| GET | `/api/produtos?q=&categoria=` | Lista, pesquisa e filtra produtos |
| GET | `/api/produtos/{id}` | Consulta produto pelo ID |
| POST | `/api/produtos` | Cadastra produto |
| PUT | `/api/produtos/{id}` | Atualiza produto |
| DELETE | `/api/produtos/{id}` | Exclui produto |
| GET | `/api/carrinho` | Consulta carrinho da sessão |
| POST | `/api/carrinho/itens` | Adiciona item ao carrinho |
| PATCH | `/api/carrinho/itens/{id}` | Atualiza quantidade |
| DELETE | `/api/carrinho/itens/{id}` | Remove item |
| DELETE | `/api/carrinho` | Limpa carrinho |
| POST | `/api/compras/finalizar` | Registra compra simulada |
| GET | `/api/pedidos` | Lista pedidos em memória |

**Uso no frontend:** o catálogo, o detalhe, o carrinho e o pagamento usam os endpoints de produtos (consulta), carrinho e finalização. A página `admin.html` ("Gerenciar anúncios") usa o CRUD de produtos (`POST`, `PUT`, `DELETE`) e `GET /api/pedidos`. Apenas `DELETE /api/carrinho` e `/api/health` não são chamados pelas telas.

O `PUT /api/produtos/{id}` é atômico: se qualquer campo enviado for inválido, nenhum campo do produto é alterado.

### 13.4 Sessão e checkout

O carrinho é armazenado no servidor e separado por cookie de sessão do navegador. O frontend não mantém uma lista paralela de produtos nem decide o estoque. O backend valida quantidade, disponibilidade e os dados de entrega antes de registrar a compra.

No botão **"Comprar agora"**, o frontend envia `compraDireta: true` junto com o item. Assim, a compra não consome unidades do mesmo produto que já estejam no carrinho; ao finalizar o carrinho, os itens comprados são retirados dele.

O cookie de sessão (`MARKETPLACE_SESSION`) é aceito com ou sem aspas, conforme a RFC 6265, e somente identificadores no formato UUID emitidos pelo servidor são reconhecidos.

A classe `Pedido` existente representa um produto por pedido. Por compatibilidade com o modelo atual, uma compra com vários produtos gera um pedido por item, com o mesmo endereço e modalidade simulada. A modalidade Pix mantém o desconto demonstrativo de 5%.

**Nenhum pagamento é processado.** A aplicação não coleta número de cartão, validade ou CVV, não emite boleto e não se conecta a serviços financeiros.

### 13.5 Limitações e segurança

- Produtos, carrinhos e pedidos são mantidos em memória; reiniciar o servidor reinicia os dados.
- A API não possui autenticação ou autorização administrativa. Não deve ser exposta diretamente à internet.
- Os dados de entrega são exemplos acadêmicos; não use dados pessoais reais.
- O conversor `Json.java` é uma implementação pequena para os formatos utilizados pela API. Caso o projeto cresça, recomenda-se adotar uma biblioteca JSON mantida, como Jackson.
- Para implantação real, seriam necessários autenticação, autorização, persistência transacional, proteção contra abuso, logs e controles de privacidade.

### 13.6 Arquivos da integração

- `MercadoLivre/src/MarketplaceServer.java`: servidor HTTP, endpoints REST e entrega das páginas.
- `MercadoLivre/src/Json.java`: conversão entre objetos Java e JSON.
- `MercadoLivre/src/Catalogo.java`: busca e exclusão por identificador.
- `MercadoLivre/frontEnd/app.js`: cliente HTTP compartilhado.
- `MercadoLivre/frontEnd/index.html`: catálogo e pesquisa.
- `MercadoLivre/frontEnd/produto.html`: detalhe e ações do produto.
- `MercadoLivre/frontEnd/carrinho.html`: carrinho por sessão do servidor.
- `MercadoLivre/frontEnd/pagamento.html`: entrega e compra simulada.
- `MercadoLivre/frontEnd/admin.html`: cadastro, edição e exclusão de anúncios e lista de pedidos.
- `MercadoLivre/src/TesteApi.java`: testes de integração da API HTTP.
- `MercadoLivre/frontEnd/estilo.css`: estilos e identificação acadêmica.
