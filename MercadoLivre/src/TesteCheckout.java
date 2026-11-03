import java.util.Scanner;

/**
 * Teste do checkout (mesmo estilo do TesteCatalogo): simula o que o usuário
 * digitaria e confere os resultados. Rode o main e veja [OK] / [FALHOU].
 */
public class TesteCheckout {

    private static int falhas = 0;

    private static void verificar(String descricao, boolean condicao) {
        if (condicao) {
            System.out.println("  [OK]     " + descricao);
        } else {
            falhas++;
            System.out.println("  [FALHOU] " + descricao);
        }
    }

    private static boolean igual(double a, double b) {
        return Math.abs(a - b) < 0.001;
    }

    private static Scanner entrada(String... linhas) {
        return new Scanner(String.join("\n", linhas) + "\n");
    }

    // Entrega válida, na ordem em que o terminal pergunta
    private static final String ENTREGA_VALIDA =
            "Maria Silva\n43999998888\n86010-000\nRua das Flores\n100\nApto 12\nCentro\nLondrina\nPR";

    private static DadosEntrega dadosValidos() {
        return new DadosEntrega("Maria Silva", "43999998888", "86010-000",
                "Rua das Flores", "100", "Apto 12", "Centro", "Londrina", "PR");
    }

    private static Catalogo novoCatalogo() {
        Catalogo c = new Catalogo();
        c.carregarProdutosExemplo();
        return c;
    }

    // ---------------------------------------------------------------
    private static void testarSelecao() {
        System.out.println("\n--- Seleção de produto e quantidade ---");
        Catalogo catalogo = novoCatalogo();
        SelecaoProduto selecao = new SelecaoProduto(catalogo);
        Produto mouse = catalogo.getProdutos().get(0);     // R$ 300,00 / estoque 20
        Produto cadeira = catalogo.getProdutos().get(4);   // esgotada

        ItemCompra item = selecao.selecionar(entrada("abc", "-2", "50", "2"), mouse.getIdProduto());
        verificar("corrige entradas inválidas (letras, negativo, acima do estoque) e aceita 2",
                item != null && item.getQuantidade() == 2);
        verificar("subtotal = 2 x 300,00 = 600,00", item != null && igual(item.getSubtotal(), 600.00));
        verificar("seleção não mexe no estoque", mouse.getQuantidadeDisponivel() == 20);

        ItemCompra tudo = selecao.selecionar(entrada("20"), mouse.getIdProduto());
        verificar("aceita comprar exatamente o estoque (20)", tudo != null && tudo.getQuantidade() == 20);

        verificar("0 cancela a seleção", selecao.selecionar(entrada("0"), mouse.getIdProduto()) == null);
        verificar("produto esgotado não pode ser selecionado",
                selecao.selecionar(entrada("1"), cadeira.getIdProduto()) == null);
        verificar("ID inexistente devolve null", selecao.selecionar(entrada("1"), 999) == null);
        verificar("ID digitado com letras devolve null", selecao.selecionar(entrada("abc")) == null);
    }

    // ---------------------------------------------------------------
    private static void testarEntrega() {
        System.out.println("\n--- Dados de entrega ---");
        DadosEntrega d = ColetaEntrega.coletar(entrada(
                "", "Maria Silva",          // nome vazio -> corrige
                "123", "(43) 99999-8888",   // telefone curto -> corrige
                "abc", "86010000",          // CEP inválido -> corrige
                "Rua das Flores",
                "", "100",                  // número vazio -> corrige
                "Apto 12", "Centro", "Londrina",
                "PRR", "pr"));              // estado inválido -> corrige
        verificar("coleta os dados após corrigir entradas inválidas", d != null);
        verificar("telefone guardado só com dígitos", d != null && d.getTelefone().equals("43999998888"));
        verificar("CEP formatado 86010-000", d != null && d.getCep().equals("86010-000"));
        verificar("estado em maiúsculas (PR)", d != null && d.getEstado().equals("PR"));

        DadosEntrega semComplemento = ColetaEntrega.coletar(entrada(
                "Joao", "4333334444", "86010000", "Rua A", "S/N", "", "Centro", "Londrina", "PR"));
        verificar("complemento é opcional", semComplemento != null && semComplemento.getComplemento().isEmpty());

        verificar("'cancelar' interrompe a coleta",
                ColetaEntrega.coletar(entrada("Maria", "cancelar")) == null);
        verificar("entrada que acaba no meio não finaliza com dados faltando",
                ColetaEntrega.coletar(entrada("Maria", "43999998888")) == null);
    }

    // ---------------------------------------------------------------
    private static void testarPedido() {
        System.out.println("\n--- Registro da compra e pedido ---");
        Catalogo catalogo = novoCatalogo();
        RegistroPedidos registro = new RegistroPedidos();
        Checkout checkout = new Checkout(catalogo, registro);
        Produto mouse = catalogo.getProdutos().get(0);
        Produto teclado = catalogo.getProdutos().get(1);

        Pedido p1 = checkout.iniciar(new Scanner(mouse.getIdProduto() + "\n2\n" + ENTREGA_VALIDA + "\n"));
        verificar("compra válida é registrada", p1 != null && registro.quantidadePedidos() == 1);
        verificar("total do pedido = 600,00", p1 != null && igual(p1.getTotal(), 600.00));
        verificar("estoque baixou de 20 para 18", mouse.getQuantidadeDisponivel() == 18);
        verificar("pedido guarda os dados de entrega",
                p1 != null && p1.getEntrega().getCidade().equals("Londrina"));

        Pedido p2 = checkout.iniciar(new Scanner(teclado.getIdProduto() + "\n1\n" + ENTREGA_VALIDA + "\n"));
        verificar("segundo pedido tem número diferente do primeiro",
                p1 != null && p2 != null && p1.getNumero() != p2.getNumero());
        verificar("estoque do teclado baixou de 5 para 4", teclado.getQuantidadeDisponivel() == 4);
    }

    // ---------------------------------------------------------------
    private static void testarFalhas() {
        System.out.println("\n--- Compras que NÃO devem ser registradas ---");
        Catalogo catalogo = novoCatalogo();
        RegistroPedidos registro = new RegistroPedidos();
        Checkout checkout = new Checkout(catalogo, registro);
        Produto mouse = catalogo.getProdutos().get(0);
        Produto monitor = catalogo.getProdutos().get(3);   // estoque 3

        verificar("sem dados de entrega não registra",
                registro.registrar(new ItemCompra(mouse, 1)) == null
                        && mouse.getQuantidadeDisponivel() == 20);

        Pedido cancelado = checkout.iniciar(new Scanner(mouse.getIdProduto() + "\n1\nMaria\ncancelar\n"));
        verificar("cancelar a entrega cancela a compra",
                cancelado == null && registro.quantidadePedidos() == 0 && mouse.getQuantidadeDisponivel() == 20);

        // Revalidação do estoque: dois itens de 3 e 2 unidades, mas só há 3 no estoque
        ItemCompra itemA = new ItemCompra(monitor, 3);
        itemA.setDadosEntrega(dadosValidos());
        ItemCompra itemB = new ItemCompra(monitor, 2);
        itemB.setDadosEntrega(dadosValidos());

        verificar("primeira compra do monitor (2 un.) é registrada", registro.registrar(itemB) != null);
        verificar("segunda (3 un.) é barrada por falta de estoque", registro.registrar(itemA) == null);
        verificar("estoque do monitor ficou em 1", monitor.getQuantidadeDisponivel() == 1);
        verificar("só 1 pedido registrado", registro.quantidadePedidos() == 1);
        verificar("registrar item nulo devolve null", registro.registrar(null) == null);
    }

    public static void main(String[] args) {
        testarSelecao();
        testarEntrega();
        testarPedido();
        testarFalhas();

        System.out.println("\n=====================================");
        System.out.println(falhas == 0 ? "TODOS OS TESTES PASSARAM!" : falhas + " TESTE(S) FALHARAM.");
    }
}
