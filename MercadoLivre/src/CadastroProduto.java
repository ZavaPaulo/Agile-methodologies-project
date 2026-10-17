import java.util.Scanner;

public class CadastroProduto {

    /**
     * Cadastra o produto do usuário com as informações solicitadas
     * Ele pede ao user, em ordem:
     *      1- nome do produto
     *      2- descrição do produto
     *      3- preço do produto
     *      4- quantidade no estoque
     *
     * @param s O scanner
     * @return Um novo objeto da classe produto, com as informações digitadas
     */
    public static Produto cadastrarProduto(Scanner s){
        System.out.println("Digite o nome do produto");
        String nomeProduto = cadastrarStringValido(s);
        System.out.println("Digite a descrição do produto");
        String descricaoProduto = cadastrarStringValido(s);
        System.out.println("Digite o preço do produto");
        double valor = s.nextDouble();
        do{
            if (!Double.isFinite(valor) || valor <= 0){
                System.out.println("O valor deve ser positivo e finito");
                valor = s.nextDouble();
            }
        }while(!Double.isFinite(valor) || valor <= 0);
        System.out.println("Digite a quantidade de produtos");
        int quantidadeDisponivel = s.nextInt();
        do{
            if (!Double.isFinite(quantidadeDisponivel) || quantidadeDisponivel <= 0){
                System.out.println("O valor deve ser positivo e finito");
                quantidadeDisponivel = s.nextInt();
            }
        }while(!Double.isFinite(quantidadeDisponivel) || quantidadeDisponivel <= 0);
        System.out.println("Digite a categoria do produto: ");
        String categoria = s.next();
        do {
            if (categoria == null || categoria.isBlank()) {
                System.out.println("A categoria do produto não pode ser vazio");
                categoria = s.nextLine();
            }
        } while (categoria == null || categoria.isBlank());
        //Essa linha é super importante, não mexa
        s.nextLine();
        return new Produto(nomeProduto, descricaoProduto, valor, quantidadeDisponivel, categoria);
    }


    /**
     * Pede ao usuário um texto e ele verifica se ele é válido
     * Para ser válido ele precisa:
     * 1- não ser nulo
     * 2 - não ser vazio
     *
     * @param s O Scanner
     * @return o texto que o usuário digitou, validado
     */
    public static String cadastrarStringValido(Scanner s){
        String string = s.nextLine();
        do{
            if (string == null || string.isBlank()){
                System.out.println("O texto não pode ser vazio");
                string = s.nextLine();
            }
        }while(string == null || string.isBlank());
        return string;
    }

}
