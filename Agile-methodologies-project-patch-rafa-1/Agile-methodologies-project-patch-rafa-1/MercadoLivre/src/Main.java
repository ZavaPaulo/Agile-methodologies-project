import java.util.ArrayList;
import java.util.Scanner;

public class  Main {
    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        int op;

        Produto p = CadastroProduto.cadastrarProduto(s);
        Produto g = CadastroProduto.cadastrarProduto(s);
        ArrayList<Produto> produtos = new ArrayList<>();
        produtos.add(p);
        System.out.println("menu de edicao\n1 edicao de produtos\n0 sair");
        op = s.nextByte();
        switch (op) {
            case 1:
                EditarProduto.editarProduto(s, produtos);
                EditarProduto.deletarProduto(produtos, p);
                break;
            case 0:
                System.out.println("pov: parte do cliente");
                EditarProduto.deletarProduto(produtos, p);
        }
        produtos.add(p);
        produtos.add(g);
        p.apresentarProduto();
        g.apresentarProduto();
        System.out.println("quanto deseja comprar de "+p.getNomeProduto());
        op = s.nextByte();
        p.comprar(op);

        System.out.println("quanto deseja comprar de "+g.getNomeProduto());
        op = s.nextByte();
        g.comprar(op);

        // Lista com todos os produtos para a pesquisa
        PesquisaProduto.menuPesquisa(s, produtos);

        s.close();
    }
}
