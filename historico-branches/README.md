# Histórico de versões de branches

Esta pasta preserva versões alternativas que existiam em branches secundárias e que não devem substituir os arquivos ativos da integração.

## Origem: `patch-rafa-1` e `mota471-beepboop-patch-1`

- `MercadoLivre/src/Main.java`: protótipo de fluxo de cadastro, edição, exclusão e compra via terminal.
- `MercadoLivre/src/CadastroProduto.java`: cópia histórica do cadastro de produto. A diferença em relação ao arquivo ativo é essencialmente de formatação/linha em branco.

As duas branches continham cópias aninhadas do projeto inteiro. Essas cópias não foram reproduzidas integralmente para evitar duplicação de dezenas de arquivos e múltiplos projetos dentro do projeto. Os arquivos com conteúdo realmente distinto foram preservados aqui.

## Critério de integração

- Os arquivos ativos em `MercadoLivre/` permanecem como fonte principal.
- Quando havia versões diferentes, foi mantida a versão já integrada e mais recente, especialmente nos arquivos que conectam o frontend à API Java.
- Protótipos antigos ficam nesta pasta apenas para consulta; não fazem parte da compilação da aplicação.
- Nenhum arquivo ativo da branch de integração foi removido.
