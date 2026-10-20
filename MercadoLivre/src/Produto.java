import java.util.concurrent.atomic.AtomicInteger;
import java.io.Serializable;
import java.util.UUID;

public class Produto implements Serializable {
    //Atributos do produto
    private String nomeProduto;
    private String detalheProduto;
    private double valor;
    private int quantidadeDisponivel;
    private final int idProduto;
    private String categoria;
    private static final long serialVersionUID = 1L;

    //Construtor do produto
    public Produto(String nomeProduto, String detalheProduto, double valor, int quantidadeDisponivel, String categoria) {
        setNomeProduto(nomeProduto);
        setDetalheProduto(detalheProduto);
        setValor(valor);
        setQuantidadeDisponivel(quantidadeDisponivel);
        setCategoria(categoria);
        this.idProduto = proximoId.getAndIncrement();
    }

    private static final AtomicInteger proximoId = new AtomicInteger(1);

    //GETTERS & SETTERS
    public String getNomeProduto() {
        return nomeProduto;
    }
    public void setNomeProduto(String nomeProduto) {
        if (nomeProduto == null || nomeProduto.isBlank()) {
            throw new IllegalArgumentException(
                    "O nome do produto não pode ser vazio!"
            );
        }
        this.nomeProduto = nomeProduto.trim();
    }

    public String getDetalheProduto() {
        return detalheProduto;
    }
    public void setDetalheProduto(String detalheProduto) {
        if (detalheProduto == null || detalheProduto.isBlank()) {
            throw new IllegalArgumentException(
                    "A descrição do produto não pode ser vazia!"
            );
        }
        this.detalheProduto = detalheProduto.trim();
    }

    public double getValor() {
        return valor;
    }
    public void setValor(double valor) {
        if (!Double.isFinite(valor) || valor <= 0) {
            throw new IllegalArgumentException(
                    "O valor do produto deve ser positivo e finito!"
            );
        }
        this.valor = valor;
    }

    public int getQuantidadeDisponivel() {
        return quantidadeDisponivel;
    }
    public void setQuantidadeDisponivel(int quantidadeDisponivel) {
        if (quantidadeDisponivel < 0) {
            throw new IllegalArgumentException(
                    "O estoque não pode ser negativo!"
            );
        }
        this.quantidadeDisponivel = quantidadeDisponivel;
    }

    public String getCategoria() {
        return categoria;
    }
    public void setCategoria(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            throw new IllegalArgumentException(
                    "A categoria do produto não pode estar vazia!"
            );
        }

        this.categoria = categoria;
    }

    public int getIdProduto() {
        return idProduto;
    }

    @Override
    public String toString() {
        return "Produto: " + nomeProduto +
                "\nID: " + idProduto +
                "\nDescrição: '" + detalheProduto + '\'' +
                "\nValor: R$ " + valor +
                "\nEstoque: " + quantidadeDisponivel +
                "\nCategoria: " + categoria +
                "\nEsgotado: " + estaEsgotado();
    }

    //Metodos do produto
    // Realiza a compra e informa se ela foi bem-sucedida
    public boolean comprar(int quantidadeComprada) {
        if (quantidadeComprada <= 0) {
            return false;
        }
        if (quantidadeComprada > quantidadeDisponivel) {
            return false;
        }
        quantidadeDisponivel -= quantidadeComprada;
        return true;
    }

    //Verifica se o produto está esgotado
    public boolean estaEsgotado() {
        return quantidadeDisponivel == 0;
    }

    // Calcula o valor total de uma quantidade de produtos
    public double calcularSubtotal(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException(
                    "A quantidade deve ser maior que zero!"
            );
        }
        return valor * quantidade;
    }

    //Adicionar produtos no estoque
    public void adicionarEstoque(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException(
                    "A quantidade adicionada deve ser maior que zero!"
            );
        }
        if (quantidade > Integer.MAX_VALUE - quantidadeDisponivel) {
            throw new IllegalArgumentException(
                    "A quantidade total ultrapassa o limite permitido!"
            );
        }
        quantidadeDisponivel += quantidade;
    }

    //Apresentar o produto
    public void apresentarProduto() {
        System.out.println("= = = = = = = = = = = = =");
        System.out.println(toString());
        System.out.println();
    }
}