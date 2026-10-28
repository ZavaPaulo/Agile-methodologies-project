import java.util.Scanner;

public class CadastroProduto {

    /**
     * Cadastra o produto do usuário com as informações solicitadas
     * Ele pede ao user, em ordem:
     *      1- nome do produto
     *      2- descrição do produto
     *      3- categoria do produto
     *      4- preço do produto
     *      5- quantidade do produto
     *
     * @param s O scanner
     * @return Um novo objeto da classe produto, com as informações digitadas
     */
    public static Produto cadastrarProduto(Scanner s){
        System.out.println("-----Digite o nome do produto-----");
        String nomeProduto = cadastrarStringValido(s);

        System.out.println("-----Digite a descrição do produto-----");
        String descricaoProduto = cadastrarStringValido(s);

        System.out.println("-----Digite a categoria do produto-----");
        String categoria = cadastrarStringValido(s);

        System.out.println("-----Digite o preço do produto-----");
        double valor = 0;
        while(!Double.isFinite(valor) || valor <= 0) {
            while (!s.hasNextDouble()) {
                s.next();
                System.out.println("-----Digite um número-----");
            }
            valor = s.nextDouble();
            if(!Double.isFinite(valor) || valor <= 0){
                System.out.println("-----Digite um valor positivo-----");
            }
        }

        System.out.println("-----Digite a quantidade de produtos-----");
        int quantidadeDisponivel = -1;
        while(quantidadeDisponivel < 0) {
            while (!s.hasNextInt()) {
                s.next();
                System.out.println("Digite um número");
            }
            quantidadeDisponivel = s.nextInt();
            if(quantidadeDisponivel < 0){
                System.out.println("Digite um valor positivo");
            }
        }
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
