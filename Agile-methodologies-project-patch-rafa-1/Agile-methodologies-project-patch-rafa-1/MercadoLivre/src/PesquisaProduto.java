import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Scanner;

    public class PesquisaProduto {

        /**
         * Padroniza um texto para comparação: tira acentos, passa para minúsculo
         * e remove espaços das pontas. Assim "Mecânico", "mecanico" e "MECANICO"
         * são tratados como iguais.
         *
         * @param texto O texto original
         * @return O texto padronizado (vazio se o texto for nulo)
         */
        private static String normalizar(String texto) {
            if (texto == null) {
                return "";
            }
            String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "");
            return semAcento.toLowerCase().trim();
        }

        /**
         * Busca produtos por palavra-chave no nome e na descrição.
         * Se o usuário digitar várias palavras, o produto precisa conter TODAS elas.
         * Se a palavra-chave for vazia, todos os produtos são retornados.
         *
         * @param produtos     A lista de produtos onde a busca será feita
         * @param palavraChave O texto digitado pelo usuário
         * @return Uma nova lista só com os produtos encontrados
         */
        public static ArrayList<Produto> buscarPorPalavraChave(ArrayList<Produto> produtos, String palavraChave) {
            ArrayList<Produto> resultado = new ArrayList<>();
            String busca = normalizar(palavraChave);

            if (busca.isEmpty()) {
                resultado.addAll(produtos);
                return resultado;
            }

            String[] termos = busca.split("\\s+");
            for (Produto p : produtos) {
                String texto = normalizar(p.getNomeProduto() + " " + p.getDetalheProduto());
                boolean contemTodos = true;
                for (String termo : termos) {
                    if (!texto.contains(termo)) {
                        contemTodos = false;
                        break;
                    }
                }
                if (contemTodos) {
                    resultado.add(p);
                }
            }
            return resultado;
        }

        /**
         * Filtra os produtos de uma categoria (sem diferenciar maiúsculas e acentos).
         * Se a categoria for vazia, todos os produtos são retornados.
         *
         * @param produtos  A lista de produtos onde o filtro será aplicado
         * @param categoria A categoria desejada
         * @return Uma nova lista só com os produtos da categoria
         */
        public static ArrayList<Produto> filtrarPorCategoria(ArrayList<Produto> produtos, String categoria) {
            ArrayList<Produto> resultado = new ArrayList<>();
            String filtro = normalizar(categoria);

            for (Produto p : produtos) {
                if (filtro.isEmpty() || normalizar(p.getCategoria()).equals(filtro)) {
                    resultado.add(p);
                }
            }
            return resultado;
        }

        /**
         * Combina os dois: primeiro filtra pela categoria e depois busca a palavra-chave
         * dentro dos produtos que sobraram.
         *
         * @param produtos     A lista de produtos
         * @param palavraChave O texto a ser procurado (pode ser vazio)
         * @param categoria    A categoria desejada (pode ser vazia)
         * @return Uma nova lista com os produtos que atendem aos dois critérios
         */
        public static ArrayList<Produto> buscarComFiltro(ArrayList<Produto> produtos, String palavraChave, String categoria) {
            ArrayList<Produto> daCategoria = filtrarPorCategoria(produtos, categoria);
            return buscarPorPalavraChave(daCategoria, palavraChave);
        }

        /**
         * Monta a lista de categorias que existem nos produtos, sem repetir.
         *
         * @param produtos A lista de produtos
         * @return As categorias encontradas, na ordem em que aparecem
         */
        public static ArrayList<String> listarCategorias(ArrayList<Produto> produtos) {
            ArrayList<String> categorias = new ArrayList<>();
            ArrayList<String> normalizadas = new ArrayList<>();

            for (Produto p : produtos) {
                String normalizada = normalizar(p.getCategoria());
                if (!normalizadas.contains(normalizada)) {
                    normalizadas.add(normalizada);
                    categorias.add(p.getCategoria());
                }
            }
            return categorias;
        }

        /**
         * Mostra no terminal os produtos de uma lista de resultados.
         *
         * @param resultados Os produtos a serem mostrados
         */
        public static void apresentarResultados(ArrayList<Produto> resultados) {
            if (resultados.isEmpty()) {
                System.out.println("Nenhum produto encontrado.");
                System.out.println();
                return;
            }
            for (Produto p : resultados) {
                p.apresentarProduto();
            }
            System.out.println(resultados.size() + " produto(s) encontrado(s).");
            System.out.println();
        }

        /**
         * Mostra as categorias disponíveis no terminal.
         */
        private static void mostrarCategorias(ArrayList<Produto> produtos) {
            ArrayList<String> categorias = listarCategorias(produtos);
            if (categorias.isEmpty()) {
                System.out.println("Ainda não há categorias cadastradas.");
                return;
            }
            System.out.println("Categorias disponíveis: " + String.join(", ", categorias));
        }

        /**
         * Lê a opção do menu como texto e converte para número,
         * assim uma entrada inválida (ex: letras) não derruba o programa.
         *
         * @return O número digitado, ou -1 se não for um número
         */
        private static int lerOpcao(Scanner s) {
            String entrada = s.nextLine().trim();
            try {
                return Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        /**
         * Menu de pesquisa no terminal. Fica em repetição até o usuário escolher voltar.
         *
         * @param s        O Scanner
         * @param produtos A lista de produtos cadastrados
         */
        public static void menuPesquisa(Scanner s, ArrayList<Produto> produtos) {
            int opcao;
            do {
                System.out.println("===== PESQUISA DE PRODUTOS =====");
                System.out.println("1 - Pesquisar por palavra-chave");
                System.out.println("2 - Filtrar por categoria");
                System.out.println("3 - Pesquisar por palavra-chave dentro de uma categoria");
                System.out.println("4 - Listar todos os produtos");
                System.out.println("0 - Voltar");
                System.out.println("Escolha uma opção: ");
                opcao = lerOpcao(s);

                switch (opcao) {
                    case 1:
                        System.out.println("Digite a palavra-chave: ");
                        String palavra = s.nextLine();
                        apresentarResultados(buscarPorPalavraChave(produtos, palavra));
                        break;
                    case 2:
                        mostrarCategorias(produtos);
                        System.out.println("Digite a categoria: ");
                        String categoria = s.nextLine();
                        apresentarResultados(filtrarPorCategoria(produtos, categoria));
                        break;
                    case 3:
                        System.out.println("Digite a palavra-chave: ");
                        String chave = s.nextLine();
                        mostrarCategorias(produtos);
                        System.out.println("Digite a categoria: ");
                        String cat = s.nextLine();
                        apresentarResultados(buscarComFiltro(produtos, chave, cat));
                        break;
                    case 4:
                        apresentarResultados(produtos);
                        break;
                    case 0:
                        System.out.println("Saindo da pesquisa...");
                        break;
                    default:
                        System.out.println("Opção inválida!");
                        System.out.println();
                }
            } while (opcao != 0);
        }
    }

