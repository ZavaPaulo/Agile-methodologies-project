import java.util.ArrayList;
import java.util.Scanner;

public class EditarProduto {

    /**
     * Pede o usuário digitar o ID do produto que quer alterar
     * Por que o ID? Porque é mais fácil desse jeito
     * @param s O Scanner
     * @param produtos A lista de produtos registrados
     * @return Nulo se não foi encontrado o ID digitado ou o produto com o ID solicitado
     */
    public static Produto pesquisarProdutoPorId(Scanner s, ArrayList<Produto> produtos){
        System.out.println("Digite o ID do produto que quer editar");
        int idPesquisar = verificarIntValido(s);
        for (Produto p : produtos) {
            if(p.getIdProduto() == idPesquisar){
                return p;
            }
        }
        System.out.println("Produto não encontrado");
        return null;
    }

    /**
     * Menu de edição no terminal. Fica em repetição até o usuário escolher voltar.
     * @param s O Scanner
     * @param produtos A lista de produtos registrados
     */
    public static void editarProduto(Scanner s, ArrayList<Produto> produtos){
        Produto p = pesquisarProdutoPorId(s,produtos);
        if(p != null){
            int escolha = -1;
            while (escolha != 0) {
                menuEdicao();
                escolha = verificarIntValido(s);
                switch (escolha){
                    case 0:
                        System.out.println("Saindo...");
                        break;
                    case 1:
                        editarNome(s,p);
                        break;
                    case 2:
                        editarDetalhes(s,p);
                        break;
                    case 3:
                        editarCategoria(s,p);
                        break;
                    case 4:
                        editarPreco(s,p);
                        break;
                    case 5:
                        editarQtd(s,p);
                        break;
                    default:
                        System.out.println("Escolha inválida, digite novamente");
                }
            }
        }
        s.nextLine();
    }

    public static void editarNome(Scanner s, Produto p){
        s.nextLine();
        System.out.println("Digite o novo nome:");
        p.setNomeProduto(s.nextLine());
    }
    public static void editarDetalhes(Scanner s, Produto p){
        s.nextLine();
        System.out.println("Digite a nova descrição:");
        p.setDetalheProduto(s.nextLine());
    }
    public static void editarCategoria(Scanner s,Produto p){
        System.out.println("Digite a nova categoria:");
        s.nextLine();
        p.setCategoria(s.nextLine());
    }
    public static void editarPreco(Scanner s,Produto p){
        System.out.println("Digite o novo preço");
        double numero = verificarDoubleValido(s);
        p.setValor(numero);
    }
    public static void editarQtd(Scanner s,Produto p){
        System.out.println("Digite a nova quantidade");
        int numero = verificarIntValido(s);
        p.setQuantidadeDisponivel(numero);
    }

    /**
     * Exibe um menu com informações do que fazer
     */
    public static void menuEdicao(){
        System.out.println("=====O que deseja editar?=====");
        System.out.println("1 - Nome do produto");
        System.out.println("2 - Detalhes");
        System.out.println("3 - Categoria");
        System.out.println("4 - Preço do produto");
        System.out.println("5 - Quantidade disponível");
        System.out.println("0 - Sair");
    }

    /**
     * Verifica se o usuário está realmente digitando um número inteiro
     *
     * @param s O Scanner
     * @return Um número inteiro
     */
    public static int verificarIntValido(Scanner s){
        while(!s.hasNextInt()){
            System.out.println("Favor,digite um número");
            s.next();
        }
        return s.nextInt();
    }


    /**
     * Verifica se o usuário está realmente digitando um número Real
     *
     * @param s O Scanner
     * @return Um número real
     */
    public static double verificarDoubleValido(Scanner s){
        while(!s.hasNextDouble()){
            System.out.println("Favor,digite um número");
            s.next();
        }
        return s.nextDouble();
    }
}
