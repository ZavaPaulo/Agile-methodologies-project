// Integração HTTP do Marketplace Acadêmico.
// Os dados oficiais e as regras de estoque são mantidos no backend Java.
const API_BASE = "/api";

function escaparHtml(valor) {
  return String(valor == null ? "" : valor).replace(/[&<>"']/g, function (c) {
    return {"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[c];
  });
}

function formatarPreco(valor) {
  return Number(valor || 0).toLocaleString("pt-BR", {style:"currency",currency:"BRL"});
}

async function requisicaoApi(caminho, opcoes) {
  opcoes = opcoes || {};
  const configuracao = Object.assign({
    method: "GET",
    credentials: "same-origin",
    headers: {}
  }, opcoes);
  if (configuracao.body) {
    configuracao.headers = Object.assign({"Content-Type":"application/json"}, configuracao.headers);
  }
  const resposta = await fetch(API_BASE + caminho, configuracao);
  if (resposta.status === 204) return null;
  const dados = await resposta.json().catch(function () { return {}; });
  if (!resposta.ok) {
    throw new Error(dados.erro || ("Falha na solicitação HTTP " + resposta.status + "."));
  }
  return dados;
}

function listarProdutos(consulta, categoria) {
  const parametros = new URLSearchParams();
  if (consulta) parametros.set("q", consulta);
  if (categoria) parametros.set("categoria", categoria);
  const query = parametros.toString();
  return requisicaoApi("/produtos" + (query ? "?" + query : ""));
}

function buscarPorId(id) {
  return requisicaoApi("/produtos/" + encodeURIComponent(id));
}

function carregarCarrinho() {
  return requisicaoApi("/carrinho");
}

function adicionarAoCarrinho(idProduto, quantidade) {
  return requisicaoApi("/carrinho/itens", {
    method:"POST",
    body:JSON.stringify({idProduto:idProduto, quantidade:quantidade})
  });
}

function atualizarQuantidadeCarrinho(idProduto, quantidade) {
  return requisicaoApi("/carrinho/itens/" + encodeURIComponent(idProduto), {
    method:"PATCH",
    body:JSON.stringify({quantidade:quantidade})
  });
}

function removerDoCarrinho(idProduto) {
  return requisicaoApi("/carrinho/itens/" + encodeURIComponent(idProduto), {method:"DELETE"});
}

function limparCarrinho() {
  return requisicaoApi("/carrinho", {method:"DELETE"});
}

function finalizarCompra(dados) {
  return requisicaoApi("/compras/finalizar", {
    method:"POST",
    body:JSON.stringify(dados)
  });
}

function icone(produto) {
  const icones = {"Periféricos":"🖱️","Áudio":"🎧","Monitores":"🖥️","Móveis":"🪑"};
  return icones[produto.categoria] || "📦";
}

function montarCabecalho(consulta) {
  const topo = document.getElementById("topo");
  if (!topo) return;
  topo.innerHTML =
    '<a class="marca" href="index.html">Marketplace acadêmico' +
      '<small>Somente uma réplica independente</small></a>' +
    '<form class="busca" action="index.html">' +
      '<input name="q" type="search" placeholder="Buscar produtos" aria-label="Buscar produtos">' +
      '<button type="submit" aria-label="Buscar">⌕</button>' +
    '</form>' +
    '<a class="carrinho-link" href="carrinho.html">🛒 Carrinho <span class="badge" id="badge"></span></a>' +
    '<div class="aviso-replica">Projeto acadêmico · sem vínculo com o Mercado Livre</div>';
  const campo = topo.querySelector(".busca input");
  if (campo) campo.value = consulta || "";
  atualizarBadge();
}

async function atualizarBadge() {
  const badge = document.getElementById("badge");
  if (!badge) return;
  try {
    const carrinho = await carregarCarrinho();
    const quantidade = (carrinho.itens || []).reduce(function (total, item) {
      return total + item.quantidade;
    }, 0);
    badge.textContent = quantidade > 0 ? String(quantidade) : "";
  } catch (erro) {
    badge.textContent = "";
  }
}

function exibirErro(elemento, erro) {
  if (!elemento) return;
  elemento.textContent = erro && erro.message ? erro.message : String(erro || "Ocorreu um erro.");
  elemento.hidden = false;
}

function comprarAgora(idProduto, quantidade) {
  const destino = new URL("pagamento.html", location.href);
  destino.searchParams.set("produto", idProduto);
  destino.searchParams.set("qtd", quantidade);
  location.href = destino.toString();
}
