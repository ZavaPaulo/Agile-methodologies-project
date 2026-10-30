import java.util.Scanner;

/**
 * Detalhamento de produto do Mercado Livre.
 *
 * Permite que o usuário selecione um produto pelo ID e veja suas
 * informações completas no terminal. Usa os mesmos dados do Catalogo.
 * Card do Trello: "Implementar o detalhamento do produto" (RF-03.3).
 *
 * @author Bruno Santana
 */
public class DetalheProduto {

    // Passo 2: o detalhamento consulta a mesma coleção do catálogo
    private final Catalogo catalogo;

    public DetalheProduto(Catalogo catalogo) {
        if (catalogo == null) {
            throw new IllegalArgumentException("O catálogo não pode ser nulo!");
        }
        this.catalogo = catalogo;
    }

    /**
     * Passos 2 e 3: localiza o produto na coleção pelo ID.
     *
     * @param id identificador do produto
     * @return o produto encontrado, ou null se o ID não existir
     */
    public Produto buscarPorId(int id) {
        for (Produto p : catalogo.getProdutos()) {
            if (p.getIdProduto() == id) {
                return p;
            }
        }
        return null;
    }

    /**
     * Passos 1, 4, 5, 6 e 7: recebe o ID do produto selecionado e exibe
     * todas as informações dele, ou avisa que o produto não foi encontrado.
     *
     * @param id identificador do produto selecionado
     * @return true se o produto foi encontrado e exibido
     */
    public boolean exibirDetalhes(int id) {
        Produto p = buscarPorId(id);

        // Passo 7: produto inexistente
        if (p == null) {
            System.out.println("Produto não encontrado (ID " + id + ").");
            return false;
        }

        System.out.println("============ DETALHES DO PRODUTO ============");
        // Passo 4: nome, descrição e categoria
        System.out.println("ID:          " + p.getIdProduto());
        System.out.println("Nome:        " + p.getNomeProduto());
        System.out.println("Categoria:   " + p.getCategoria());

        // Passo 5: especificações completas
        System.out.println("Descrição:   " + p.getDetalheProduto());

        // Passo 6: preço e quantidade em estoque
        System.out.println("Preço:       " + Catalogo.formatarPreco(p.getValor()));
        if (p.estaEsgotado()) {
            System.out.println("Estoque:     0 unidade(s) (ESGOTADO)");
        } else {
            System.out.println("Estoque:     " + p.getQuantidadeDisponivel() + " unidade(s)");
        }
        System.out.println("=============================================");
        return true;
    }

    /**
     * Pergunta o ID ao usuário no terminal e exibe os detalhes.
     * Trata entradas que não são números sem derrubar o programa.
     *
     * @param s o Scanner do terminal
     * @return true se o produto foi encontrado e exibido
     */
    public boolean selecionarEExibir(Scanner s) {
        System.out.print("Digite o ID do produto: ");
        if (!s.hasNextLine()) {
            return false;
        }
        String entrada = s.nextLine().trim();
        int id;
        try {
            id = Integer.parseInt(entrada);
        } catch (NumberFormatException e) {
            System.out.println("ID inválido: digite apenas números.");
            return false;
        }
        return exibirDetalhes(id);
    }
}
