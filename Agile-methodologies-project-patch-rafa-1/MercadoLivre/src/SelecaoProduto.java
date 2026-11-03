import java.util.Scanner;

/**
 * Seleção de produto e quantidade para a compra.
 *
 * Localiza o produto no catálogo, mostra preço e estoque, pede a quantidade,
 * valida (inteiro positivo e dentro do estoque) e calcula o subtotal.
 * Card do Trello: "Implementar seleção de produtos e quantidade".
 */
public class SelecaoProduto {

    // Reaproveita a busca por ID que já existe no DetalheProduto
    private final DetalheProduto detalhe;

    public SelecaoProduto(Catalogo catalogo) {
        if (catalogo == null) {
            throw new IllegalArgumentException("O catálogo não pode ser nulo!");
        }
        this.detalhe = new DetalheProduto(catalogo);
    }

    /**
     * Converte o texto digitado em quantidade.
     *
     * @return a quantidade, ou -1 se não for um inteiro positivo
     */
    public static int converterQuantidade(String entrada) {
        if (entrada == null) {
            return -1;
        }
        try {
            int quantidade = Integer.parseInt(entrada.trim());
            return quantidade > 0 ? quantidade : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Pergunta o ID do produto e segue para a escolha da quantidade.
     *
     * @return o item selecionado, ou null se a seleção não foi concluída
     */
    public ItemCompra selecionar(Scanner s) {
        System.out.print("Digite o ID do produto que deseja comprar: ");
        if (!s.hasNextLine()) {
            return null;
        }
        String entrada = s.nextLine().trim();
        int id;
        try {
            id = Integer.parseInt(entrada);
        } catch (NumberFormatException e) {
            System.out.println("ID inválido: digite apenas números.");
            return null;
        }
        return selecionar(s, id);
    }

    /**
     * Passos 1 a 9: recebe o ID, localiza o produto, mostra preço e estoque,
     * pede a quantidade até ela ser válida (ou o usuário cancelar com 0) e
     * devolve o item pronto para a etapa de entrega.
     *
     * @return o item selecionado, ou null (produto inexistente, esgotado ou cancelado)
     */
    public ItemCompra selecionar(Scanner s, int idProduto) {
        Produto p = detalhe.buscarPorId(idProduto);
        if (p == null) {
            System.out.println("Produto não encontrado (ID " + idProduto + ").");
            return null;
        }

        System.out.println("Produto: " + p.getNomeProduto());
        System.out.println("Preço:   " + Catalogo.formatarPreco(p.getValor()));
        if (p.estaEsgotado()) {
            System.out.println("Produto esgotado: não é possível comprar.");
            return null;
        }
        System.out.println("Estoque: " + p.getQuantidadeDisponivel() + " unidade(s)");

        // Repete até a quantidade ser válida: assim o usuário pode corrigir
        while (true) {
            System.out.print("Quantidade desejada (0 para cancelar): ");
            if (!s.hasNextLine()) {
                return null;
            }
            String entrada = s.nextLine().trim();

            if (entrada.equals("0")) {
                System.out.println("Seleção cancelada.");
                return null;
            }

            int quantidade = converterQuantidade(entrada);
            if (quantidade < 0) {
                System.out.println("Quantidade inválida: digite um número inteiro maior que zero.");
                continue;
            }
            if (quantidade > p.getQuantidadeDisponivel()) {
                System.out.println("Estoque insuficiente: só há "
                        + p.getQuantidadeDisponivel() + " unidade(s) disponível(is).");
                continue;
            }

            ItemCompra item = new ItemCompra(p, quantidade);
            System.out.println("Subtotal: " + Catalogo.formatarPreco(item.getSubtotal()));
            return item;
        }
    }
}
