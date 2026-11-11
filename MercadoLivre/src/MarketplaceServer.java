import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * Servidor HTTP do marketplace acadêmico.
 *
 * A aplicação usa o próprio JDK para expor uma API REST e servir os arquivos
 * estáticos do frontend. Nenhuma dependência externa é necessária.
 *
 * Importante: é uma réplica acadêmica independente; não possui vínculo com
 * o Mercado Livre e não processa pagamentos reais.
 */
public class MarketplaceServer {

    private static final Catalogo CATALOGO = new Catalogo();
    private static final RegistroPedidos REGISTRO = new RegistroPedidos();

    // Cada navegador recebe um identificador de sessão e um carrinho próprio.
    private static final Map<String, ConcurrentHashMap<Integer, Integer>> CARRINHOS =
            new ConcurrentHashMap<>();

    private static final int LIMITE_CORPO = 64 * 1024;

    public static void main(String[] args) throws IOException {
        int porta = obterPorta(args);

        // Carrega os mesmos produtos de demonstração já usados pelo projeto.
        CATALOGO.carregarProdutosExemplo();

        HttpServer servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", porta), 0);
        servidor.createContext("/api", MarketplaceServer::tratarApi);
        servidor.createContext("/", MarketplaceServer::servirFrontend);
        servidor.setExecutor(Executors.newCachedThreadPool());
        servidor.start();

        System.out.println("Marketplace acadêmico disponível em http://localhost:" + porta);
        System.out.println("API: http://localhost:" + porta + "/api/health");
        System.out.println("Projeto independente — réplica acadêmica, sem vínculo com o Mercado Livre.");
    }

    private static int obterPorta(String[] args) {
        if (args.length > 0) {
            return Integer.parseInt(args[0]);
        }
        String variavel = System.getenv("PORT");
        return variavel == null || variavel.isBlank() ? 8080 : Integer.parseInt(variavel);
    }

    /**
     * Ponto central de entrada da API. Encaminha cada endpoint para seu método
     * específico e converte erros de validação em respostas HTTP compreensíveis.
     */
    private static void tratarApi(HttpExchange exchange) throws IOException {
        try {
            String metodo = exchange.getRequestMethod();
            String caminho = exchange.getRequestURI().getPath();

            if ("OPTIONS".equalsIgnoreCase(metodo)) {
                exchange.getResponseHeaders().set("Allow", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("/api/health".equals(caminho) && "GET".equalsIgnoreCase(metodo)) {
                responderJson(exchange, 200, Map.of(
                        "status", "online",
                        "projeto", "Réplica acadêmica independente",
                        "pagamentosReais", false
                ));
                return;
            }

            if (caminho.equals("/api/produtos") || caminho.startsWith("/api/produtos/")) {
                tratarProdutos(exchange, metodo, caminho);
                return;
            }

            if (caminho.equals("/api/carrinho") || caminho.startsWith("/api/carrinho/")) {
                tratarCarrinho(exchange, metodo, caminho);
                return;
            }

            if ("/api/compras/finalizar".equals(caminho) && "POST".equalsIgnoreCase(metodo)) {
                finalizarCompra(exchange);
                return;
            }

            if ("/api/pedidos".equals(caminho) && "GET".equalsIgnoreCase(metodo)) {
                synchronized (CATALOGO) {
                    List<Map<String, Object>> pedidos = new ArrayList<>();
                    for (Pedido pedido : REGISTRO.getPedidos()) {
                        pedidos.add(pedidoComoJson(pedido));
                    }
                    responderJson(exchange, 200, pedidos);
                }
                return;
            }

            responderErro(exchange, 404, "Endpoint não encontrado.");
        } catch (IllegalArgumentException erro) {
            responderErro(exchange, 400, erro.getMessage());
        } catch (Exception erro) {
            erro.printStackTrace();
            responderErro(exchange, 500, "Ocorreu um erro interno ao processar a solicitação.");
        } finally {
            exchange.close();
        }
    }

    /**
     * CRUD de produtos. As validações continuam sendo feitas pela classe Produto,
     * preservando as regras de negócio que já existiam no backend.
     */
    private static void tratarProdutos(HttpExchange exchange, String metodo, String caminho)
            throws IOException {
        String[] partes = caminho.split("/");
        synchronized (CATALOGO) {
            if (partes.length == 3) {
                if ("GET".equalsIgnoreCase(metodo)) {
                    Map<String, String> filtros = parametros(exchange.getRequestURI().getRawQuery());
                    ArrayList<Produto> lista = new ArrayList<>(CATALOGO.getProdutos());
                    lista = PesquisaProduto.buscarComFiltro(
                            lista,
                            filtros.getOrDefault("q", ""),
                            filtros.getOrDefault("categoria", "")
                    );

                    List<Map<String, Object>> resposta = new ArrayList<>();
                    for (Produto produto : lista) {
                        resposta.add(produtoComoJson(produto));
                    }
                    responderJson(exchange, 200, resposta);
                    return;
                }

                if ("POST".equalsIgnoreCase(metodo)) {
                    Map<String, Object> dados = lerObjetoJson(exchange);
                    Produto produto = new Produto(
                            textoObrigatorio(dados, "nomeProduto"),
                            textoObrigatorio(dados, "detalheProduto"),
                            decimalObrigatorio(dados, "valor"),
                            inteiroObrigatorio(dados, "quantidadeDisponivel"),
                            textoObrigatorio(dados, "categoria")
                    );
                    CATALOGO.adicionarProduto(produto);
                    responderJson(exchange, 201, produtoComoJson(produto));
                    return;
                }
            }

            if (partes.length == 4) {
                int id = inteiroPath(partes[3]);
                Produto produto = buscarProduto(id);
                if (produto == null) {
                    responderErro(exchange, 404, "Produto não encontrado.");
                    return;
                }

                if ("GET".equalsIgnoreCase(metodo)) {
                    responderJson(exchange, 200, produtoComoJson(produto));
                    return;
                }

                if ("PUT".equalsIgnoreCase(metodo)) {
                    Map<String, Object> dados = lerObjetoJson(exchange);
                    if (dados.containsKey("nomeProduto")) {
                        produto.setNomeProduto(textoObrigatorio(dados, "nomeProduto"));
                    }
                    if (dados.containsKey("detalheProduto")) {
                        produto.setDetalheProduto(textoObrigatorio(dados, "detalheProduto"));
                    }
                    if (dados.containsKey("valor")) {
                        produto.setValor(decimalObrigatorio(dados, "valor"));
                    }
                    if (dados.containsKey("quantidadeDisponivel")) {
                        produto.setQuantidadeDisponivel(inteiroObrigatorio(dados, "quantidadeDisponivel"));
                    }
                    if (dados.containsKey("categoria")) {
                        produto.setCategoria(textoObrigatorio(dados, "categoria"));
                    }
                    responderJson(exchange, 200, produtoComoJson(produto));
                    return;
                }

                if ("DELETE".equalsIgnoreCase(metodo)) {
                    CATALOGO.removerProduto(id);
                    // Itens removidos do catálogo também deixam de aparecer nos carrinhos.
                    for (Map<Integer, Integer> carrinho : CARRINHOS.values()) {
                        carrinho.remove(id);
                    }
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }
            }
        }

        responderErro(exchange, 405, "Método não permitido para produtos.");
    }

    /**
     * Mantém o carrinho no backend, separado por sessão de navegador.
     * O estoque é conferido novamente no momento da finalização da compra.
     */
    private static void tratarCarrinho(HttpExchange exchange, String metodo, String caminho)
            throws IOException {
        String sessao = obterSessao(exchange);
        ConcurrentHashMap<Integer, Integer> carrinho =
                CARRINHOS.computeIfAbsent(sessao, chave -> new ConcurrentHashMap<>());

        if ("/api/carrinho".equals(caminho)) {
            if ("GET".equalsIgnoreCase(metodo)) {
                responderJson(exchange, 200, carrinhoComoJson(carrinho));
                return;
            }
            if ("DELETE".equalsIgnoreCase(metodo)) {
                carrinho.clear();
                responderJson(exchange, 200, carrinhoComoJson(carrinho));
                return;
            }
        }

        if ("/api/carrinho/itens".equals(caminho) && "POST".equalsIgnoreCase(metodo)) {
            Map<String, Object> dados = lerObjetoJson(exchange);
            int id = inteiroObrigatorio(dados, "idProduto");
            int quantidade = inteiroObrigatorio(dados, "quantidade");
            if (quantidade <= 0) {
                throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
            }

            synchronized (CATALOGO) {
                Produto produto = buscarProduto(id);
                if (produto == null) {
                    responderErro(exchange, 404, "Produto não encontrado.");
                    return;
                }
                int atual = carrinho.getOrDefault(id, 0);
                if ((long) atual + quantidade > produto.getQuantidadeDisponivel()) {
                    responderErro(exchange, 409, "Estoque insuficiente para a quantidade solicitada.");
                    return;
                }
                carrinho.put(id, atual + quantidade);
                responderJson(exchange, 201, carrinhoComoJson(carrinho));
                return;
            }
        }

        if (caminho.startsWith("/api/carrinho/itens/")) {
            int id = inteiroPath(caminho.substring("/api/carrinho/itens/".length()));

            if ("DELETE".equalsIgnoreCase(metodo)) {
                carrinho.remove(id);
                responderJson(exchange, 200, carrinhoComoJson(carrinho));
                return;
            }

            if ("PATCH".equalsIgnoreCase(metodo)) {
                int quantidade = inteiroObrigatorio(lerObjetoJson(exchange), "quantidade");
                if (quantidade <= 0) {
                    throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
                }

                synchronized (CATALOGO) {
                    Produto produto = buscarProduto(id);
                    if (produto == null) {
                        carrinho.remove(id);
                        responderErro(exchange, 404, "Produto não encontrado.");
                        return;
                    }
                    if (quantidade > produto.getQuantidadeDisponivel()) {
                        responderErro(exchange, 409, "Estoque insuficiente para a quantidade solicitada.");
                        return;
                    }
                    carrinho.put(id, quantidade);
                    responderJson(exchange, 200, carrinhoComoJson(carrinho));
                    return;
                }
            }
        }

        responderErro(exchange, 405, "Método não permitido para o carrinho.");
    }

    /**
     * Conclui a compra usando os validadores de DadosEntrega e o RegistroPedidos
     * existente. O modelo Pedido representa um produto por vez; por isso, um
     * carrinho com vários produtos gera um pedido para cada item.
     */
    private static void finalizarCompra(HttpExchange exchange) throws IOException {
        String sessao = obterSessao(exchange);
        ConcurrentHashMap<Integer, Integer> carrinho =
                CARRINHOS.computeIfAbsent(sessao, chave -> new ConcurrentHashMap<>());

        Map<String, Object> corpo = lerObjetoJson(exchange);
        Object entregaObjeto = corpo.get("entrega");
        if (!(entregaObjeto instanceof Map)) {
            throw new IllegalArgumentException("Informe os dados de entrega.");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> entregaDados = (Map<String, Object>) entregaObjeto;

        DadosEntrega entrega = new DadosEntrega(
                textoObrigatorio(entregaDados, "nomeDestinatario"),
                textoObrigatorio(entregaDados, "telefone"),
                textoObrigatorio(entregaDados, "cep"),
                textoObrigatorio(entregaDados, "logradouro"),
                textoObrigatorio(entregaDados, "numero"),
                textoOpcional(entregaDados, "complemento"),
                textoObrigatorio(entregaDados, "bairro"),
                textoObrigatorio(entregaDados, "cidade"),
                textoObrigatorio(entregaDados, "estado")
        );

        String formaPagamento = String.valueOf(corpo.getOrDefault("formaPagamento", "pix"));
        if (!List.of("pix", "boleto", "cartao").contains(formaPagamento)) {
            throw new IllegalArgumentException("Forma de pagamento não reconhecida.");
        }

        synchronized (CATALOGO) {
            // Permite finalizar o carrinho inteiro ou somente um produto ("Comprar agora").
            Map<Integer, Integer> itensSolicitados = new LinkedHashMap<>();
            Object itensObjeto = corpo.get("itens");

            if (itensObjeto instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> itens = (List<Object>) itensObjeto;
                for (Object objeto : itens) {
                    if (!(objeto instanceof Map)) {
                        throw new IllegalArgumentException("Cada item da compra deve ser um objeto.");
                    }
                    @SuppressWarnings("unchecked")
                    Map<String, Object> item = (Map<String, Object>) objeto;
                    int id = inteiroObrigatorio(item, "idProduto");
                    int quantidade = inteiroObrigatorio(item, "quantidade");
                    if (quantidade <= 0) {
                        throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
                    }
                    itensSolicitados.merge(id, quantidade, Integer::sum);
                }
            } else {
                itensSolicitados.putAll(carrinho);
            }

            if (itensSolicitados.isEmpty()) {
                responderErro(exchange, 400, "Não há produtos para finalizar.");
                return;
            }

            // Valida todos os itens antes de alterar qualquer estoque.
            for (Map.Entry<Integer, Integer> item : itensSolicitados.entrySet()) {
                Produto produto = buscarProduto(item.getKey());
                if (produto == null) {
                    responderErro(exchange, 409, "Um produto da compra não está mais disponível.");
                    return;
                }
                if (item.getValue() <= 0 || item.getValue() > produto.getQuantidadeDisponivel()) {
                    responderErro(exchange, 409,
                            "Estoque insuficiente para o produto " + produto.getNomeProduto() + ".");
                    return;
                }
            }

            List<Map<String, Object>> pedidos = new ArrayList<>();
            double subtotal = 0.0;
            for (Map.Entry<Integer, Integer> item : itensSolicitados.entrySet()) {
                Produto produto = buscarProduto(item.getKey());
                ItemCompra compra = new ItemCompra(produto, item.getValue());
                compra.setDadosEntrega(entrega);

                Pedido pedido = REGISTRO.registrar(compra);
                if (pedido == null) {
                    responderErro(exchange, 409, "Não foi possível registrar todos os itens da compra.");
                    return;
                }

                Map<String, Object> dadosPedido = pedidoComoJson(pedido);
                dadosPedido.put("formaPagamento", formaPagamento);
                pedidos.add(dadosPedido);
                subtotal += pedido.getTotal();
            }

            // Desconto Pix demonstrativo, sem transação financeira.
            double desconto = "pix".equals(formaPagamento) ? subtotal * 0.05 : 0.0;
            double total = subtotal - desconto;

            // "Comprar agora" não deve remover unidades que já estavam no carrinho.
            boolean compraDireta = Boolean.TRUE.equals(corpo.get("compraDireta"));
            if (!compraDireta) {
                for (Map.Entry<Integer, Integer> item : itensSolicitados.entrySet()) {
                    int restante = carrinho.getOrDefault(item.getKey(), 0) - item.getValue();
                    if (restante > 0) carrinho.put(item.getKey(), restante);
                    else carrinho.remove(item.getKey());
                }
            }

            Map<String, Object> resposta = new LinkedHashMap<>();
            resposta.put("mensagem", "Compra simulada concluída.");
            resposta.put("formaPagamento", formaPagamento);
            resposta.put("pagamentoProcessado", false);
            resposta.put("subtotal", subtotal);
            resposta.put("desconto", desconto);
            resposta.put("total", total);
            resposta.put("pedidos", pedidos);
            responderJson(exchange, 201, resposta);
        }
    }

    private static Map<String, Object> carrinhoComoJson(Map<Integer, Integer> carrinho) {
        synchronized (CATALOGO) {
            List<Map<String, Object>> itens = new ArrayList<>();
            double subtotal = 0.0;

            for (Map.Entry<Integer, Integer> item : carrinho.entrySet()) {
                Produto produto = buscarProduto(item.getKey());
                if (produto == null) {
                    continue;
                }
                double valorItem = produto.calcularSubtotal(item.getValue());
                subtotal += valorItem;

                Map<String, Object> linha = new LinkedHashMap<>();
                linha.put("produto", produtoComoJson(produto));
                linha.put("quantidade", item.getValue());
                linha.put("subtotal", valorItem);
                itens.add(linha);
            }

            return Map.of("itens", itens, "subtotal", subtotal, "total", subtotal);
        }
    }

    private static Map<String, Object> produtoComoJson(Produto produto) {
        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("idProduto", produto.getIdProduto());
        dados.put("nomeProduto", produto.getNomeProduto());
        dados.put("detalheProduto", produto.getDetalheProduto());
        dados.put("valor", produto.getValor());
        dados.put("quantidadeDisponivel", produto.getQuantidadeDisponivel());
        dados.put("categoria", produto.getCategoria());
        dados.put("esgotado", produto.estaEsgotado());
        return dados;
    }

    private static Map<String, Object> pedidoComoJson(Pedido pedido) {
        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("numero", pedido.getNumeroFormatado());
        dados.put("dataHora", pedido.getDataHora().toString());
        dados.put("idProduto", pedido.getIdProduto());
        dados.put("nomeProduto", pedido.getNomeProduto());
        dados.put("valorUnitario", pedido.getValorUnitario());
        dados.put("quantidade", pedido.getQuantidade());
        dados.put("total", pedido.getTotal());
        dados.put("destinatario", pedido.getEntrega().getNomeDestinatario());
        dados.put("endereco", pedido.getEntrega().getEnderecoCompleto());
        return dados;
    }

    private static Produto buscarProduto(int id) {
        for (Produto produto : CATALOGO.getProdutos()) {
            if (produto.getIdProduto() == id) {
                return produto;
            }
        }
        return null;
    }

    private static String obterSessao(HttpExchange exchange) {
        String cookie = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookie != null) {
            for (String parte : cookie.split(";")) {
                String[] par = parte.trim().split("=", 2);
                if (par.length == 2 && "MARKETPLACE_SESSION".equals(par[0])) {
                    return par[1];
                }
            }
        }

        String novaSessao = UUID.randomUUID().toString();
        exchange.getResponseHeaders().add(
                "Set-Cookie",
                "MARKETPLACE_SESSION=" + novaSessao + "; Path=/; HttpOnly; SameSite=Lax; Max-Age=86400"
        );
        return novaSessao;
    }

    private static Map<String, String> parametros(String query) {
        Map<String, String> resultado = new LinkedHashMap<>();
        if (query == null || query.isBlank()) {
            return resultado;
        }
        for (String parte : query.split("&")) {
            String[] par = parte.split("=", 2);
            String chave = URLDecoder.decode(par[0], StandardCharsets.UTF_8);
            String valor = par.length > 1
                    ? URLDecoder.decode(par[1], StandardCharsets.UTF_8)
                    : "";
            resultado.put(chave, valor);
        }
        return resultado;
    }

    private static Map<String, Object> lerObjetoJson(HttpExchange exchange) throws IOException {
        try (InputStream entrada = exchange.getRequestBody()) {
            byte[] bytes = entrada.readNBytes(LIMITE_CORPO + 1);
            if (bytes.length > LIMITE_CORPO) {
                throw new IllegalArgumentException("O corpo da requisição é muito grande.");
            }
            return Json.parseObject(new String(bytes, StandardCharsets.UTF_8));
        }
    }

    private static String textoObrigatorio(Map<String, Object> dados, String campo) {
        Object valor = dados.get(campo);
        if (valor == null || valor.toString().isBlank()) {
            throw new IllegalArgumentException("O campo '" + campo + "' é obrigatório.");
        }
        return valor.toString().trim();
    }

    private static String textoOpcional(Map<String, Object> dados, String campo) {
        Object valor = dados.get(campo);
        return valor == null ? "" : valor.toString().trim();
    }

    private static int inteiroObrigatorio(Map<String, Object> dados, String campo) {
        Object valor = dados.get(campo);
        if (!(valor instanceof Number)) {
            throw new IllegalArgumentException("O campo '" + campo + "' deve ser numérico.");
        }
        try {
            return new java.math.BigDecimal(valor.toString()).intValueExact();
        } catch (ArithmeticException erro) {
            throw new IllegalArgumentException("O campo '" + campo + "' deve ser um número inteiro.");
        }
    }

    private static int inteiroPath(String valor) {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException erro) {
            throw new IllegalArgumentException("O identificador informado é inválido.");
        }
    }

    private static double decimalObrigatorio(Map<String, Object> dados, String campo) {
        Object valor = dados.get(campo);
        if (!(valor instanceof Number)) {
            throw new IllegalArgumentException("O campo '" + campo + "' deve ser numérico.");
        }
        return ((Number) valor).doubleValue();
    }

    private static void responderErro(HttpExchange exchange, int status, String mensagem)
            throws IOException {
        responderJson(exchange, status, Map.of("erro", mensagem == null ? "Solicitação inválida." : mensagem));
    }

    private static void responderJson(HttpExchange exchange, int status, Object corpo)
            throws IOException {
        byte[] bytes = Json.stringify(corpo).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
    }

    /**
     * Entrega o frontend apenas a partir da pasta autorizada. A normalização
     * do caminho impede que uma URL tente acessar arquivos fora dessa pasta.
     */
    private static void servirFrontend(HttpExchange exchange) throws IOException {
        try {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().set("Allow", "GET");
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            Path raiz = Paths.get(
                    System.getProperty("marketplace.frontend", "MercadoLivre/frontEnd")
            ).toAbsolutePath().normalize();

            String solicitado = exchange.getRequestURI().getPath();
            if (solicitado == null || "/".equals(solicitado)) {
                solicitado = "/index.html";
            }

            Path arquivo = raiz.resolve(solicitado.substring(1)).normalize();
            if (!arquivo.startsWith(raiz)) {
                exchange.sendResponseHeaders(403, -1);
                return;
            }
            if (!Files.isRegularFile(arquivo)) {
                responderTexto(exchange, 404, "Arquivo não encontrado.");
                return;
            }

            String tipo = tipoConteudo(arquivo);
            byte[] bytes = Files.readAllBytes(arquivo);
            exchange.getResponseHeaders().set("Content-Type", tipo);
            exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
            exchange.getResponseHeaders().set("Cache-Control", "no-cache");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
        } finally {
            exchange.close();
        }
    }

    private static String tipoConteudo(Path arquivo) {
        String nome = arquivo.getFileName().toString().toLowerCase();
        if (nome.endsWith(".html")) return "text/html; charset=utf-8";
        if (nome.endsWith(".css")) return "text/css; charset=utf-8";
        if (nome.endsWith(".js")) return "text/javascript; charset=utf-8";
        if (nome.endsWith(".svg")) return "image/svg+xml";
        if (nome.endsWith(".png")) return "image/png";
        if (nome.endsWith(".jpg") || nome.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
    }

    private static void responderTexto(HttpExchange exchange, int status, String mensagem)
            throws IOException {
        byte[] bytes = mensagem.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
    }
}
