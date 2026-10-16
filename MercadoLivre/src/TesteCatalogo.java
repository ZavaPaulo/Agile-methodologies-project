/**
 * Testes do card "Implementar o catálogo de produtos" (passo 8).
 * Testa a exibição com nenhum produto, um produto e vários produtos.
 *
 * Para rodar no IntelliJ: clique na seta verde ao lado do main.
 *
 * @author Bruno Santana
 */
public class TesteCatalogo {

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
        // Teste 1: catálogo vazio
        System.out.println("\n### TESTE 1: catálogo vazio\n");
        Catalogo vazio = new Catalogo();
        vazio.exibirCatalogo();
        verificar("Catálogo novo começa vazio", vazio.estaVazio());
        verificar("Quantidade é 0", vazio.quantidadeProdutos() == 0);

        // Teste 2: um produto
        System.out.println("\n### TESTE 2: um produto\n");
        Catalogo umProduto = new Catalogo();
        Produto mouse = new Produto("Mouse Gamer", "Mouse RGB", 300.00, 20);
        umProduto.adicionarProduto(mouse);
        umProduto.exibirCatalogo();
        verificar("Catálogo não está vazio", !umProduto.estaVazio());
        verificar("Quantidade é 1", umProduto.quantidadeProdutos() == 1);
        verificar("Produto exibido é o cadastrado",
                umProduto.getProdutos().get(0) == mouse);

        // Teste 3: vários produtos (exemplos)
        System.out.println("\n### TESTE 3: vários produtos\n");
        Catalogo varios = new Catalogo();
        varios.carregarProdutosExemplo();
        varios.exibirCatalogo();
        verificar("Quantidade é 5", varios.quantidadeProdutos() == 5);
        verificar("Dados conferem com o cadastrado",
                varios.getProdutos().get(1).getNomeProduto().equals("Teclado Mecânico")
                        && varios.getProdutos().get(1).getValor() == 400.00);

        // Teste 4: entradas inválidas
        System.out.println("\n### TESTE 4: entradas inválidas\n");
        boolean recusouNulo;
        try {
            varios.adicionarProduto(null);
            recusouNulo = false;
        } catch (IllegalArgumentException e) {
            recusouNulo = true;
        }
        verificar("Recusa produto nulo", recusouNulo);

        boolean listaProtegida;
        try {
            varios.getProdutos().clear();
            listaProtegida = false;
        } catch (UnsupportedOperationException e) {
            listaProtegida = true;
        }
        verificar("Lista não pode ser apagada por fora", listaProtegida
                && varios.quantidadeProdutos() == 5);

        // Teste 5: formatação de preço
        verificar("Preço no formato brasileiro",
                Catalogo.formatarPreco(1299.99).equals("R$ 1.299,99"));

        System.out.println("\nRESULTADO: " + ok + " ok, " + falhas + " falha(s)");
    }
}
