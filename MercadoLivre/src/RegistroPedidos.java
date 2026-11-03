import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Estrutura que guarda os pedidos concluídos e faz o registro da compra.
 * Card do Trello: "Registrar a compra e gerar o pedido".
 */
public class RegistroPedidos {

    private final List<Pedido> pedidos = new ArrayList<>();

    /**
     * Registra a compra: confirma os dados, revalida o estoque, gera o pedido,
     * guarda e só então baixa o estoque. Se algo falhar, nada é registrado.
     *
     * @param item produto + quantidade + dados de entrega
     * @return o pedido registrado, ou null se a compra não pôde ser concluída
     */
    public Pedido registrar(ItemCompra item) {
        // Passo 1
        if (item == null) {
            System.out.println("Compra não registrada: nenhum produto selecionado.");
            return null;
        }
        // Passo 2
        if (!item.temDadosEntrega()) {
            System.out.println("Compra não registrada: dados de entrega não preenchidos.");
            return null;
        }
        // Passo 3: o estoque pode ter mudado desde a seleção
        Produto produto = item.getProduto();
        if (item.getQuantidade() > produto.getQuantidadeDisponivel()) {
            System.out.println("Compra não registrada: estoque insuficiente de '"
                    + produto.getNomeProduto() + "' (disponível: "
                    + produto.getQuantidadeDisponivel() + ").");
            return null;
        }

        // Passos 4 e 5: o Pedido calcula o total e gera o número único
        Pedido pedido = new Pedido(produto, item.getQuantidade(), item.getDadosEntrega());

        // Passo 6
        pedidos.add(pedido);

        // Passo 7: baixa o estoque depois do registro
        if (!produto.comprar(item.getQuantidade())) {
            pedidos.remove(pedido); // desfaz: não deixa compra incompleta registrada
            System.out.println("Compra não registrada: falha ao atualizar o estoque.");
            return null;
        }
        return pedido;
    }

    public List<Pedido> getPedidos() {
        return Collections.unmodifiableList(pedidos);
    }

    public int quantidadePedidos() {
        return pedidos.size();
    }

    public Pedido buscarPorNumero(int numero) {
        for (Pedido p : pedidos) {
            if (p.getNumero() == numero) {
                return p;
            }
        }
        return null;
    }
}
