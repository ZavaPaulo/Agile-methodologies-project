import java.util.Scanner;

/**
 * Junta as três etapas do checkout:
 * seleção de produto/quantidade -> dados de entrega -> registro do pedido.
 * É esta classe que o Main chama.
 */
public class Checkout {

    private final SelecaoProduto selecao;
    private final RegistroPedidos registro;

    public Checkout(Catalogo catalogo, RegistroPedidos registro) {
        if (catalogo == null || registro == null) {
            throw new IllegalArgumentException("Catálogo e registro de pedidos não podem ser nulos!");
        }
        this.selecao = new SelecaoProduto(catalogo);
        this.registro = registro;
    }

    /**
     * Executa o fluxo completo de compra no terminal.
     *
     * @return o pedido registrado, ou null se a compra não foi concluída
     */
    public Pedido iniciar(Scanner s) {
        // Etapa 1: produto e quantidade
        ItemCompra item = selecao.selecionar(s);
        if (item == null) {
            return null;
        }

        // Etapa 2: dados de entrega (associados ao item)
        DadosEntrega entrega = ColetaEntrega.coletar(s);
        if (entrega == null) {
            System.out.println("Compra cancelada.");
            return null;
        }
        item.setDadosEntrega(entrega);

        // Etapa 3: registrar e mostrar o resumo
        Pedido pedido = registro.registrar(item);
        if (pedido != null) {
            pedido.exibirResumo();
        }
        return pedido;
    }
}
