import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Pedido concluído: guarda uma "foto" da compra (nome e preço do produto no
 * momento da compra), a quantidade, o total, os dados de entrega e um número único.
 * Card do Trello: "Registrar a compra e gerar o pedido".
 */
public class Pedido {

    // Gera números únicos e sequenciais (mesma ideia do idProduto no Produto)
    private static final AtomicInteger proximoNumero = new AtomicInteger(1);
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final int numero;
    private final LocalDateTime dataHora;
    private final int idProduto;
    private final String nomeProduto;
    private final double valorUnitario;
    private final int quantidade;
    private final double total;
    private final DadosEntrega entrega;

    /**
     * Só crie o Pedido depois de validar estoque e entrega (o número é gasto aqui).
     */
    public Pedido(Produto produto, int quantidade, DadosEntrega entrega) {
        if (produto == null) {
            throw new IllegalArgumentException("O produto não pode ser nulo!");
        }
        if (entrega == null) {
            throw new IllegalArgumentException("Os dados de entrega são obrigatórios!");
        }
        this.idProduto = produto.getIdProduto();
        this.nomeProduto = produto.getNomeProduto();
        this.valorUnitario = produto.getValor();
        this.quantidade = quantidade;
        this.total = produto.calcularSubtotal(quantidade); // lança erro se quantidade <= 0
        this.entrega = entrega;
        this.dataHora = LocalDateTime.now();
        this.numero = proximoNumero.getAndIncrement();
    }

    public int getNumero() { return numero; }
    public String getNumeroFormatado() { return String.format("PED-%04d", numero); }
    public int getIdProduto() { return idProduto; }
    public String getNomeProduto() { return nomeProduto; }
    public double getValorUnitario() { return valorUnitario; }
    public int getQuantidade() { return quantidade; }
    public double getTotal() { return total; }
    public DadosEntrega getEntrega() { return entrega; }
    public LocalDateTime getDataHora() { return dataHora; }

    /**
     * Exibe o resumo do pedido no terminal.
     */
    public void exibirResumo() {
        System.out.println("============= PEDIDO CONFIRMADO =============");
        System.out.println("Pedido:      " + getNumeroFormatado());
        System.out.println("Data:        " + dataHora.format(FORMATO_DATA));
        System.out.println("---------------- Produto ---------------------");
        System.out.println("Produto:     " + nomeProduto + " (ID " + idProduto + ")");
        System.out.println("Quantidade:  " + quantidade);
        System.out.println("Valor unit.: " + Catalogo.formatarPreco(valorUnitario));
        System.out.println("TOTAL:       " + Catalogo.formatarPreco(total));
        System.out.println("---------------- Entrega ---------------------");
        System.out.println("Destinatário: " + entrega.getNomeDestinatario());
        System.out.println("Telefone:     " + entrega.getTelefone());
        System.out.println("Endereço:     " + entrega.getEnderecoCompleto());
        System.out.println("=============================================");
    }
}
