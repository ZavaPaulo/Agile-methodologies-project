/**
 * Item escolhido pelo usuário para comprar: produto + quantidade.
 * Também carrega os dados de entrega, que são associados depois
 * (ver ColetaEntrega), e é o que a seleção "encaminha" para a entrega.
 * Card do Trello: "Implementar seleção de produtos e quantidade".
 */
public class ItemCompra {

    private final Produto produto;
    private final int quantidade;
    private DadosEntrega dadosEntrega;

    public ItemCompra(Produto produto, int quantidade) {
        if (produto == null) {
            throw new IllegalArgumentException("O produto não pode ser nulo!");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero!");
        }
        this.produto = produto;
        this.quantidade = quantidade;
    }

    public Produto getProduto() {
        return produto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    /**
     * Subtotal = preço x quantidade (usa o cálculo que já existe no Produto).
     */
    public double getSubtotal() {
        return produto.calcularSubtotal(quantidade);
    }

    public DadosEntrega getDadosEntrega() {
        return dadosEntrega;
    }

    /**
     * Associa os dados de entrega a este item (card "dados de entrega").
     */
    public void setDadosEntrega(DadosEntrega dadosEntrega) {
        if (dadosEntrega == null) {
            throw new IllegalArgumentException("Os dados de entrega não podem ser nulos!");
        }
        this.dadosEntrega = dadosEntrega;
    }

    public boolean temDadosEntrega() {
        return dadosEntrega != null;
    }
}
