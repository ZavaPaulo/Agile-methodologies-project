import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

/**
 * Testes de integração da API HTTP (MarketplaceServer).
 * Sobe o servidor em uma porta de teste, faz requisições reais e confere as respostas.
 *
 * Cobre, entre outros, as duas correções da integração:
 *  - "Comprar agora" (compraDireta) não pode mexer nas unidades que estão no carrinho;
 *  - PUT de produto é atômico: se um campo for inválido, nenhum campo é alterado.
 *
 * Para rodar: execute o main (IntelliJ) ou, na raiz do repositório:
 *   java -cp out/production/Agile-methodologies-project TesteApi
 */
public class TesteApi {

    private static final int PORTA = 18080;
    private static final String BASE = "http://127.0.0.1:" + PORTA;

    private static int ok = 0;
    private static int falhas = 0;

    private static final HttpClient CLIENTE = HttpClient.newBuilder()
            .cookieHandler(new CookieManager())
            .build();

    private static void verificar(String descricao, boolean condicao) {
        if (condicao) {
            ok++;
            System.out.println("[OK]    " + descricao);
        } else {
            falhas++;
            System.out.println("[FALHA] " + descricao);
        }
    }

    /** Resposta simplificada: status + corpo. */
    private record Resposta(int status, String corpo) {
        Map<String, Object> json() {
            return Json.parseObject(corpo);
        }
    }

    private static Resposta chamar(String metodo, String caminho, String corpoJson)
            throws IOException, InterruptedException {
        HttpRequest.Builder req = HttpRequest.newBuilder(URI.create(BASE + caminho))
                .header("Content-Type", "application/json");
        if (corpoJson == null) {
            req.method(metodo, HttpRequest.BodyPublishers.noBody());
        } else {
            req.method(metodo, HttpRequest.BodyPublishers.ofString(corpoJson));
        }
        HttpResponse<String> r = CLIENTE.send(req.build(), HttpResponse.BodyHandlers.ofString());
        return new Resposta(r.statusCode(), r.body());
    }

    private static final String ENTREGA = "\"entrega\":{\"nomeDestinatario\":\"Maria\","
            + "\"telefone\":\"43999998888\",\"cep\":\"86010-000\",\"logradouro\":\"Rua A\","
            + "\"numero\":\"10\",\"complemento\":\"\",\"bairro\":\"Centro\","
            + "\"cidade\":\"Londrina\",\"estado\":\"PR\"}";

    @SuppressWarnings("unchecked")
    private static int unidadesNoCarrinho(int idProduto) throws Exception {
        Map<String, Object> carrinho = chamar("GET", "/api/carrinho", null).json();
        for (Object linha : (List<Object>) carrinho.get("itens")) {
            Map<String, Object> item = (Map<String, Object>) linha;
            Map<String, Object> produto = (Map<String, Object>) item.get("produto");
            if (((Number) produto.get("idProduto")).intValue() == idProduto) {
                return ((Number) item.get("quantidade")).intValue();
            }
        }
        return 0;
    }

    public static void main(String[] args) throws Exception {
        MarketplaceServer.main(new String[]{String.valueOf(PORTA)});

        try {
            System.out.println("\n### TESTE 1: servidor e arquivos do frontend");
            verificar("GET /api/health responde 200", chamar("GET", "/api/health", null).status() == 200);
            for (String pagina : List.of("/", "/index.html", "/produto.html", "/carrinho.html",
                    "/pagamento.html", "/admin.html", "/app.js", "/estilo.css")) {
                verificar("frontend entrega " + pagina, chamar("GET", pagina, null).status() == 200);
            }

            System.out.println("\n### TESTE 2: catálogo e busca");
            Resposta lista = chamar("GET", "/api/produtos", null);
            verificar("lista de produtos responde 200", lista.status() == 200);
            verificar("lista contém o Mouse Gamer", lista.corpo().contains("Mouse Gamer"));
            verificar("busca sem acento encontra 'Teclado Mecânico'",
                    chamar("GET", "/api/produtos?q=mecanico", null).corpo().contains("Teclado Mec"));
            verificar("produto inexistente -> 404", chamar("GET", "/api/produtos/9999", null).status() == 404);

            System.out.println("\n### TESTE 3: carrinho e estoque");
            Resposta add = chamar("POST", "/api/carrinho/itens", "{\"idProduto\":1,\"quantidade\":5}");
            verificar("adicionar 5 Mouses ao carrinho -> 201", add.status() == 201);
            verificar("adicionar acima do estoque -> 409",
                    chamar("POST", "/api/carrinho/itens", "{\"idProduto\":4,\"quantidade\":99}").status() == 409);
            verificar("quantidade zero -> 400",
                    chamar("PATCH", "/api/carrinho/itens/1", "{\"quantidade\":0}").status() == 400);
            verificar("produto esgotado não entra no carrinho -> 409",
                    chamar("POST", "/api/carrinho/itens", "{\"idProduto\":5,\"quantidade\":1}").status() == 409);

            System.out.println("\n### TESTE 4: 'Comprar agora' não mexe no carrinho (correção)");
            verificar("carrinho começa com 5 Mouses", unidadesNoCarrinho(1) == 5);
            Resposta direta = chamar("POST", "/api/compras/finalizar",
                    "{" + ENTREGA + ",\"formaPagamento\":\"pix\",\"compraDireta\":true,"
                            + "\"itens\":[{\"idProduto\":1,\"quantidade\":2}]}");
            verificar("compra direta de 2 Mouses -> 201", direta.status() == 201);
            verificar("carrinho continua com 5 Mouses", unidadesNoCarrinho(1) == 5);
            Resposta doCarrinho = chamar("POST", "/api/compras/finalizar",
                    "{" + ENTREGA + ",\"formaPagamento\":\"boleto\","
                            + "\"itens\":[{\"idProduto\":1,\"quantidade\":5}]}");
            verificar("finalizar o carrinho -> 201", doCarrinho.status() == 201);
            verificar("carrinho fica sem Mouses após finalizar o carrinho", unidadesNoCarrinho(1) == 0);

            System.out.println("\n### TESTE 5: estoque baixou e compra recusada não altera nada");
            Map<String, Object> mouse = chamar("GET", "/api/produtos/1", null).json();
            verificar("estoque do Mouse: 20 - 2 - 5 = 13",
                    ((Number) mouse.get("quantidadeDisponivel")).intValue() == 13);
            Resposta recusada = chamar("POST", "/api/compras/finalizar",
                    "{" + ENTREGA + ",\"formaPagamento\":\"pix\","
                            + "\"itens\":[{\"idProduto\":3,\"quantidade\":1},{\"idProduto\":4,\"quantidade\":99}]}");
            verificar("compra com item sem estoque -> 409", recusada.status() == 409);
            Map<String, Object> headset = chamar("GET", "/api/produtos/3", null).json();
            verificar("estoque do Headset segue 12 (nada foi baixado)",
                    ((Number) headset.get("quantidadeDisponivel")).intValue() == 12);
            verificar("telefone inválido -> 400",
                    chamar("POST", "/api/compras/finalizar",
                            "{\"entrega\":{\"nomeDestinatario\":\"M\",\"telefone\":\"1\",\"cep\":\"86010-000\","
                                    + "\"logradouro\":\"R\",\"numero\":\"1\",\"bairro\":\"B\",\"cidade\":\"C\","
                                    + "\"estado\":\"PR\"},\"itens\":[{\"idProduto\":3,\"quantidade\":1}]}")
                            .status() == 400);

            System.out.println("\n### TESTE 6: CRUD de produtos");
            Resposta criado = chamar("POST", "/api/produtos",
                    "{\"nomeProduto\":\"Webcam\",\"detalheProduto\":\"Webcam Full HD\","
                            + "\"valor\":199.9,\"quantidadeDisponivel\":7,\"categoria\":\"Periféricos\"}");
            verificar("cadastrar produto -> 201", criado.status() == 201);
            int id = ((Number) criado.json().get("idProduto")).intValue();
            verificar("cadastro com preço negativo -> 400",
                    chamar("POST", "/api/produtos",
                            "{\"nomeProduto\":\"X\",\"detalheProduto\":\"Y\",\"valor\":-1,"
                                    + "\"quantidadeDisponivel\":1,\"categoria\":\"Z\"}").status() == 400);

            Resposta parcial = chamar("PUT", "/api/produtos/" + id, "{\"valor\":149.9}");
            verificar("PUT parcial -> 200", parcial.status() == 200);

            System.out.println("\n### TESTE 7: PUT é atômico (correção)");
            Resposta invalido = chamar("PUT", "/api/produtos/" + id,
                    "{\"nomeProduto\":\"NOVO NOME\",\"detalheProduto\":\"Nova descrição\","
                            + "\"quantidadeDisponivel\":99,\"valor\":-1}");
            verificar("PUT com um campo inválido -> 400", invalido.status() == 400);
            Map<String, Object> depois = chamar("GET", "/api/produtos/" + id, null).json();
            verificar("nome não foi alterado", "Webcam".equals(depois.get("nomeProduto")));
            verificar("descrição não foi alterada", "Webcam Full HD".equals(depois.get("detalheProduto")));
            verificar("estoque não foi alterado",
                    ((Number) depois.get("quantidadeDisponivel")).intValue() == 7);
            verificar("preço segue o último válido (149.9)",
                    ((Number) depois.get("valor")).doubleValue() == 149.9);

            System.out.println("\n### TESTE 8: exclusão");
            chamar("POST", "/api/carrinho/itens", "{\"idProduto\":" + id + ",\"quantidade\":1}");
            verificar("webcam está no carrinho", unidadesNoCarrinho(id) == 1);
            verificar("DELETE -> 204", chamar("DELETE", "/api/produtos/" + id, null).status() == 204);
            verificar("produto excluído -> 404", chamar("GET", "/api/produtos/" + id, null).status() == 404);
            verificar("produto excluído sai do carrinho", unidadesNoCarrinho(id) == 0);

            System.out.println("\n### TESTE 9: pedidos");
            Resposta pedidos = chamar("GET", "/api/pedidos", null);
            verificar("GET /api/pedidos -> 200", pedidos.status() == 200);
            verificar("há pedidos PED-0001 e PED-0002",
                    pedidos.corpo().contains("PED-0001") && pedidos.corpo().contains("PED-0002"));
        } finally {
            System.out.println("\nRESULTADO: " + ok + " ok, " + falhas + " falha(s)");
            System.out.println(falhas == 0 ? "TODOS OS TESTES PASSARAM!" : "HÁ TESTES FALHANDO!");
            System.exit(falhas == 0 ? 0 : 1);
        }
    }
}
