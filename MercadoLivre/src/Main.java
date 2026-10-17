import java.util.ArrayList;
import java.util.Scanner;

public class Main {
   public static void main(String[] args) {
       Scanner s = new Scanner(System.in);

        Produto mouse = new Produto(
                "Mouse Gamer",
                "Mouse Gamer RGB 8000 DPI",
                300.00,
                20,
                "Gamer"
        );

        Produto teclado = new Produto(
                "Teclado Gamer",
                "Teclado mecânico RGB",
                400.00,
                5,
                "Gamer"
        );
        Produto p = CadastroProduto.cadastrarProduto(s);
        Produto g = CadastroProduto.cadastrarProduto(s);

        p.apresentarProduto();
        g.apresentarProduto();

        mouse.apresentarProduto();
        teclado.apresentarProduto();

        mouse.comprar(20);
        mouse.apresentarProduto();

        mouse.adicionarEstoque(5);
        mouse.apresentarProduto();

        mouse.comprar(3);
        mouse.apresentarProduto();

        mouse.comprar(-5);
        mouse.apresentarProduto();
         // Lista com todos os produtos para a pesquisa
        ArrayList<Produto> produtos = new ArrayList<>();
        produtos.add(mouse);
        produtos.add(teclado);
        produtos.add(p);
        produtos.add(g);

        PesquisaProduto.menuPesquisa(s, produtos);

        s.close();
    }
}
