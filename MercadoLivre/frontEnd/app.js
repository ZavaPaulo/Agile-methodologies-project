// ------------------------------------------------------------------
// Dados dos produtos: mesmos campos e valores do Catalogo.carregarProdutosExemplo()
// (Produto.java: idProduto, nomeProduto, detalheProduto, valor,
//  quantidadeDisponivel, categoria).
// Na integração Front e Back, é só trocar esta lista pelos dados vindos do Java.
// ------------------------------------------------------------------
const PRODUTOS = [
  { idProduto: 1, nomeProduto: "Mouse Gamer", detalheProduto: "Mouse Gamer RGB 8000 DPI", valor: 300.00, quantidadeDisponivel: 20, categoria: "Periféricos" },
  { idProduto: 2, nomeProduto: "Teclado Mecânico", detalheProduto: "Teclado mecânico RGB switch blue", valor: 400.00, quantidadeDisponivel: 5, categoria: "Periféricos" },
  { idProduto: 3, nomeProduto: "Headset Bluetooth", detalheProduto: "Headset sem fio com microfone", valor: 249.90, quantidadeDisponivel: 12, categoria: "Áudio" },
  { idProduto: 4, nomeProduto: 'Monitor 24"', detalheProduto: "Monitor Full HD 144Hz", valor: 1299.99, quantidadeDisponivel: 3, categoria: "Monitores" },
  { idProduto: 5, nomeProduto: "Cadeira Gamer", detalheProduto: "Cadeira ergonômica reclinável", valor: 899.00, quantidadeDisponivel: 0, categoria: "Móveis" }
];

// Ícones provisórios, já que o Produto.java ainda não tem imagem
const ICONES = { "Periféricos": "🖱️", "Áudio": "🎧", "Monitores": "🖥️", "Móveis": "🪑" };
const icone = (p) => ICONES[p.categoria] || "📦";

// Mesmo resultado do Catalogo.formatarPreco no Java
const formatarPreco = (v) => v.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });

// ------------------------------------------------------------------
// Busca (mesma ideia do PesquisaProduto.java: sem acento, sem maiúscula,
// e o produto precisa conter TODAS as palavras)
// ------------------------------------------------------------------
const normalizar = (t) => (t || "").normalize("NFD").replace(/\p{M}/gu, "").toLowerCase().trim();

function buscarPorId(id) {
  return PRODUTOS.find((p) => p.idProduto === id) || null;
}

function listarCategorias() {
  return [...new Set(PRODUTOS.map((p) => p.categoria))];
}

function buscar(palavraChave, categoria) {
  const termos = normalizar(palavraChave).split(/\s+/).filter(Boolean);
  const cat = normalizar(categoria);
  return PRODUTOS.filter((p) => {
    if (cat && normalizar(p.categoria) !== cat) return false;
    const texto = normalizar(p.nomeProduto + " " + p.detalheProduto);
    return termos.every((t) => texto.includes(t));
  });
}

// ------------------------------------------------------------------
// Carrinho (fica no localStorage do navegador: lista de {id, qtd})
// ------------------------------------------------------------------
function lerCarrinho() {
  try { return JSON.parse(localStorage.getItem("carrinho")) || []; }
  catch (e) { return []; }
}

function salvarCarrinho(carrinho) {
  localStorage.setItem("carrinho", JSON.stringify(carrinho));
  atualizarBadge();
}

function qtdNoCarrinho(id) {
  const item = lerCarrinho().find((i) => i.id === id);
  return item ? item.qtd : 0;
}

// Devolve false se passar do estoque (nada é alterado)
function adicionarAoCarrinho(id, qtd) {
  const p = buscarPorId(id);
  const carrinho = lerCarrinho();
  const item = carrinho.find((i) => i.id === id);
  const nova = (item ? item.qtd : 0) + qtd;
  if (!p || nova > p.quantidadeDisponivel) return false;
  if (item) item.qtd = nova; else carrinho.push({ id, qtd });
  salvarCarrinho(carrinho);
  return true;
}

// Guarda a compra no mesmo formato do objeto "compra" do pagamento.html
function comprarAgora(id, qtd) {
  const p = buscarPorId(id);
  localStorage.setItem("compraAtual", JSON.stringify({
    produto: { idProduto: p.idProduto, nomeProduto: p.nomeProduto, valor: p.valor },
    quantidade: qtd
  }));
  location.href = "pagamento.html";
}

// ------------------------------------------------------------------
// Cabeçalho (igual em todas as páginas)
// ------------------------------------------------------------------
function atualizarBadge() {
  const total = lerCarrinho().reduce((soma, i) => soma + i.qtd, 0);
  const badge = document.getElementById("badge");
  if (badge) badge.textContent = total > 0 ? total : "";
}

function montarCabecalho(consulta) {
  document.getElementById("topo").innerHTML = `
    <a class="marca" href="index.html">Mercado Livre</a>
    <form class="busca" action="index.html">
      <input name="q" type="search" placeholder="Buscar produtos" aria-label="Buscar produtos">
      <button aria-label="Buscar">🔍</button>
    </form>
    <a class="carrinho-link" href="carrinho.html">🛒 Carrinho<span class="badge" id="badge"></span></a>`;
  document.querySelector(".busca input").value = consulta || "";
  atualizarBadge();
}
