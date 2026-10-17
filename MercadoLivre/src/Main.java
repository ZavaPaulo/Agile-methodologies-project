public class Main {
    static void main(String[] args) {
        Produto mouse = new Produto(
                "Mouse Gamer",
                "Mouse Gamer RGB 8000 DPI",
                300.00,
                20
        );

        Produto teclado = new Produto(
                "Teclado Gamer",
                "Teclado mecânico RGB",
                400.00,
                10
        );

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
    }
}
