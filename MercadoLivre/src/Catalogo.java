import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Catálogo de produtos do Mercado Livre (vitrine).
 *
 * Responsável por guardar os produtos cadastrados e exibi-los no terminal.
 * Card do Trello: "Implementar o catálogo de produtos" (RF-03.1).
 *
 * @author Bruno Santana
 */
public class Catalogo {

    // Formato brasileiro de moeda: R$ 1.234,56
    private static final Locale BRASIL = Locale.of("pt", "BR");

    // Passo 2: coleção que armazena os produtos cadastrados
    private final List<Produto> produtos = new ArrayList<>();

    /**
     * Adiciona um produto ao catálogo.
     *
     * @param produto o produto a ser adicionado (não pode ser nulo)
     */
    public void adicionarProduto(Produto produto) {
        if (produto == null) {
            throw new IllegalArgumentException("O produto não pode ser nulo!");
        }
        produtos.add(produto);
    }

    /**
     * Passo 3: carrega alguns produtos de exemplo para os testes iniciais.
     */
    public void carregarProdutosExemplo() {
        adicionarProduto(new Produto("Mouse Gamer", "Mouse Gamer RGB 8000 DPI", 300.00, 20));
        adicionarProduto(new Produto("Teclado Mecânico", "Teclado mecânico RGB switch blue", 400.00, 5));
        adicionarProduto(new Produto("Headset Bluetooth", "Headset sem fio com microfone", 249.90, 12));
        adicionarProduto(new Produto("Monitor 24\"", "Monitor Full HD 144Hz", 1299.99, 3));
        adicionarProduto(new Produto("Cadeira Gamer", "Cadeira ergonômica reclinável", 899.00, 0));
    }

    /**
     * Passo 4: recupera os produtos cadastrados.
     * A lista retornada é somente leitura, para que outros módulos
     * não alterem o catálogo por fora (use adicionarProduto).
     *
     * @return lista de produtos do catálogo
     */
    public List<Produto> getProdutos() {
        return Collections.unmodifiableList(produtos);
    }

    /**
     * @return true se não houver nenhum produto cadastrado
     */
    public boolean estaVazio() {
        return produtos.isEmpty();
    }

    /**
     * @return quantidade de produtos no catálogo
     */
    public int quantidadeProdutos() {
        return produtos.size();
    }

    /**
     * Passos 5, 6 e 7: exibe a vitrine no terminal com o ID, o título
     * e o preço de cada produto, ou avisa quando o catálogo está vazio.
     */
    public void exibirCatalogo() {
        System.out.println("========== VITRINE MERCADO LIVRE ==========");

        if (estaVazio()) {
            System.out.println("Nenhum produto cadastrado no momento.");
            System.out.println("===========================================");
            return;
        }

        System.out.printf("%-4s | %-28s | %s%n", "ID", "Título", "Preço");
        System.out.println("-------------------------------------------");
        for (Produto p : produtos) {
            String linha = String.format("%-4d | %-28s | %s",
                    p.getIdProduto(),
                    p.getNomeProduto(),
                    formatarPreco(p.getValor()));
            if (p.estaEsgotado()) {
                linha += "  (ESGOTADO)";
            }
            System.out.println(linha);
        }
        System.out.println("-------------------------------------------");
        System.out.println(quantidadeProdutos() + " produto(s) na vitrine.");
        System.out.println("===========================================");
    }

    /**
     * Formata um valor no padrão brasileiro. Ex.: 1299.99 -> "R$ 1.299,99"
     */
    public static String formatarPreco(double valor) {
        return String.format(BRASIL, "R$ %,.2f", valor);
    }
}
