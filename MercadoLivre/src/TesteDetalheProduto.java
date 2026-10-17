import java.util.Scanner;

/**
 * Testes do card "Implementar o detalhamento do produto" (passo 8).
 * Testa a consulta com identificadores válidos e inválidos.
 *
 * Para rodar no IntelliJ: clique na seta verde ao lado do main.
 *
 * @author Bruno Santana
 */
public class TesteDetalheProduto {

    private static int ok = 0;
    private static int falhas = 0;

    private static void verificar(String descricao, boolean condicao) {
        if (condicao) {
            ok++;
            System.out.println("[OK]    " + descricao);
        } else {
            falhas++;
            System.out.println("[FALHA] " + descricao);
        }
    }

    public static void main(String[] args) {
        Catalogo catalogo = new Catalogo();
        catalogo.carregarProdutosExemplo();
        DetalheProduto detalhe = new DetalheProduto(catalogo);

        // IDs reais dos produtos de exemplo (são gerados automaticamente)
        Produto primeiro = catalogo.getProdutos().get(0);   // Mouse Gamer
        Produto esgotado = catalogo.getProdutos().get(4);   // Cadeira Gamer (estoque 0)
        int idValido = primeiro.getIdProduto();

        // Teste 1: ID válido
        System.out.println("\n### TESTE 1: ID válido\n");
        verificar("Exibe produto existente", detalhe.exibirDetalhes(idValido));
        verificar("Busca retorna o produto certo",
                detalhe.buscarPorId(idValido) == primeiro);

        // Teste 2: produto esgotado
        System.out.println("\n### TESTE 2: produto esgotado\n");
        verificar("Exibe produto esgotado",
                detalhe.exibirDetalhes(esgotado.getIdProduto()));

        // Teste 3: IDs inválidos
        System.out.println("\n### TESTE 3: IDs inválidos\n");
        verificar("ID inexistente (999) é tratado", !detalhe.exibirDetalhes(999));
        verificar("ID zero é tratado", !detalhe.exibirDetalhes(0));
        verificar("ID negativo é tratado", !detalhe.exibirDetalhes(-1));
        verificar("Busca de ID inexistente retorna null", detalhe.buscarPorId(999) == null);

        // Teste 4: usa os mesmos dados do catálogo
        System.out.println("\n### TESTE 4: mesmos dados do catálogo\n");
        primeiro.comprar(5);
        verificar("Estoque alterado no catálogo aparece no detalhe",
                detalhe.buscarPorId(idValido).getQuantidadeDisponivel() == 15);

        // Teste 5: catálogo vazio
        System.out.println("\n### TESTE 5: catálogo vazio\n");
        DetalheProduto detalheVazio = new DetalheProduto(new Catalogo());
        verificar("Catálogo vazio: produto não encontrado",
                !detalheVazio.exibirDetalhes(idValido));

        // Teste 6: entrada digitada no terminal
        System.out.println("\n### TESTE 6: entrada do terminal\n");
        verificar("Digitar um ID válido exibe o produto",
                detalhe.selecionarEExibir(new Scanner(idValido + "\n")));
        verificar("Digitar letras não derruba o programa",
                !detalhe.selecionarEExibir(new Scanner("abc\n")));
        verificar("Digitar vazio não derruba o programa",
                !detalhe.selecionarEExibir(new Scanner("\n")));

        // Teste 7: catálogo nulo
        boolean recusouNulo;
        try {
            new DetalheProduto(null);
            recusouNulo = false;
        } catch (IllegalArgumentException e) {
            recusouNulo = true;
        }
        verificar("Recusa catálogo nulo", recusouNulo);

        System.out.println("\nRESULTADO: " + ok + " ok, " + falhas + " falha(s)");
    }
}
