const API_ORIGENS = {
  local: "http://localhost:8080",
  render: "https://api-biblioteca-6xow.onrender.com"
};
let origem = localStorage.getItem("biblioteca-api-origem") || "local";
let moduloAtual = "livros";
let paginaAtual = 0;
let tamanhoPagina = 10;
let registroEditando = null;
let itensAtuais = [];
let apiKey = localStorage.getItem("biblioteca-api-key") || "";

const modulos = {
  livros: {
    titulo:"Livros",endpoint:"/api/v2/livros",singular:"livro",
    exemplo:{titulo:"Dom Casmurro",isbn:"9788535911665",anoPublicacao:1899,quantidadePaginas:256,autor:{id:1},editora:{id:1},categorias:[{id:1}]},
    campos:[{nome:"titulo",rotulo:"Título",tipo:"text",obrigatorio:true,minlength:2,maxlength:200},{nome:"isbn",rotulo:"ISBN",tipo:"text",obrigatorio:true,minlength:10,maxlength:20},{nome:"anoPublicacao",rotulo:"Ano de publicação",tipo:"number",obrigatorio:true,min:1},{nome:"quantidadePaginas",rotulo:"Quantidade de páginas",tipo:"number",obrigatorio:true,min:1},{nome:"autorId",rotulo:"ID do autor",tipo:"number",obrigatorio:true,min:1},{nome:"editoraId",rotulo:"ID da editora",tipo:"number",obrigatorio:true,min:1},{nome:"categoriasIds",rotulo:"IDs das categorias (separados por vírgula)",tipo:"text",obrigatorio:false} ],
    montar(v){return{titulo:v.titulo,isbn:v.isbn,anoPublicacao:Number(v.anoPublicacao),quantidadePaginas:Number(v.quantidadePaginas),autor:{id:Number(v.autorId)},editora:{id:Number(v.editoraId)},categorias:(v.categoriasIds||"").split(",").map(x=>x.trim()).filter(Boolean).map(id=>({id:Number(id)}))};},
    preencher(i){return{titulo:i.titulo,isbn:i.isbn,anoPublicacao:i.anoPublicacao,quantidadePaginas:i.quantidadePaginas,autorId:i.autor?.id??"",editoraId:i.editora?.id??"",categoriasIds:(i.categorias||[]).map(c=>c.id).join(",")};}
  },
  autores:{titulo:"Autores",endpoint:"/api/v2/autores",singular:"autor",exemplo:{nome:"Rodrigo Arruda",nacionalidade:"Brasileira",dataNascimento:"2006-08-24"},campos:[{nome:"nome",rotulo:"Nome",tipo:"text",obrigatorio:true,minlength:2,maxlength:100},{nome:"nacionalidade",rotulo:"Nacionalidade",tipo:"text",obrigatorio:true,maxlength:50},{nome:"dataNascimento",rotulo:"Data de nascimento",tipo:"date",obrigatorio:false,maxDateHoje:true}]},
  editoras:{titulo:"Editoras",endpoint:"/api/v2/editoras",singular:"editora",exemplo:{nome:"Arruda Editora",pais:"Brasil"},campos:[{nome:"nome",rotulo:"Nome",tipo:"text",obrigatorio:true,minlength:2,maxlength:100},{nome:"pais",rotulo:"País",tipo:"text",obrigatorio:true,minlength:2,maxlength:50}]},
  categorias:{titulo:"Categorias",endpoint:"/api/v2/categorias",singular:"categoria",exemplo:{nome:"Romance",descricao:"Obras literárias de romance."},campos:[{nome:"nome",rotulo:"Nome",tipo:"text",obrigatorio:true,minlength:2,maxlength:100},{nome:"descricao",rotulo:"Descrição",tipo:"text",obrigatorio:false,maxlength:255}]},
  leitores:{titulo:"Leitores",endpoint:"/api/v2/leitores",singular:"leitor",exemplo:{nome:"Rodrigo Arruda",email:"rodrigo@teste.com",telefone:"11999999999"},campos:[{nome:"nome",rotulo:"Nome",tipo:"text",obrigatorio:true,minlength:2,maxlength:100},{nome:"email",rotulo:"E-mail",tipo:"email",obrigatorio:true,maxlength:150},{nome:"telefone",rotulo:"Telefone",tipo:"tel",obrigatorio:true,minlength:10,maxlength:20}]},
  emprestimos:{titulo:"Empréstimos",endpoint:"/api/v2/emprestimos",singular:"empréstimo",exemplo:{dataEmprestimo:"2026-09-29",dataPrevistaDevolucao:"2026-10-06",dataDevolucao:null,status:"ATIVO",leitor:{id:1},livro:{id:1}},campos:[{nome:"leitorId",rotulo:"ID do leitor",tipo:"number",obrigatorio:true,min:1},{nome:"livroId",rotulo:"ID do livro",tipo:"number",obrigatorio:true,min:1},{nome:"dataEmprestimo",rotulo:"Data do empréstimo",tipo:"date",obrigatorio:true},{nome:"dataPrevistaDevolucao",rotulo:"Data prevista de devolução",tipo:"date",obrigatorio:true},{nome:"dataDevolucao",rotulo:"Data da devolução (se devolvido)",tipo:"date",obrigatorio:false},{nome:"status",rotulo:"Status",tipo:"select",opcoes:["ATIVO","DEVOLVIDO","ATRASADO"],obrigatorio:true}],montar(v){const body={dataEmprestimo:v.dataEmprestimo,dataPrevistaDevolucao:v.dataPrevistaDevolucao,status:v.status,leitor:{id:Number(v.leitorId)},livro:{id:Number(v.livroId)}};if(v.dataDevolucao)body.dataDevolucao=v.dataDevolucao;return body;},preencher(i){return{leitorId:i.leitor?.id??"",livroId:i.livro?.id??"",dataEmprestimo:i.dataEmprestimo??"",dataPrevistaDevolucao:i.dataPrevistaDevolucao??"",dataDevolucao:i.dataDevolucao??"",status:i.status??"ATIVO"};}}
};

const $ = s => document.querySelector(s);
const formulario = $("#formulario"), lista = $("#lista"), mensagem = $("#mensagem");
function apiUrl(path) { return API_ORIGENS[origem] + path; }
function atualizarLinkSwagger() {
  const url=`${API_ORIGENS[origem]}/swagger-ui/index.html`;
  ["#swaggerLinkTopo","#swaggerLinkRodape"].forEach(id=>{const link=$(id);if(link)link.href=url;});
}
function mostrarMensagem(texto, erro=false) { mensagem.textContent=texto; mensagem.className=erro?"erro":"sucesso"; }
function formatarJSON(valor) { try { return JSON.stringify(valor, null, 2); } catch { return String(valor); } }
function exibirTransacao({metodo,url,status,statusText,request,response,headers}) {
  $("#requestMetodo").textContent = metodo || "—";
  $("#requestUrl").textContent = url || "—";
  $("#requestBody").textContent = request == null ? "(sem corpo)" : formatarJSON(request);
  $("#responseStatus").textContent = status == null ? "Sem resposta" : `${status} ${statusText || ""}`.trim();
  $("#responseHeaders").textContent = headers || "—";
  $("#responseBody").textContent = response == null ? "(sem corpo)" : typeof response === "string" ? response : formatarJSON(response);
}
async function requisicao(path, opcoes={}, registrarNoInspector=true) {
  const url=apiUrl(path), metodo=(opcoes.method||"GET").toUpperCase();
  const headers = new Headers(opcoes.headers || {});
  headers.set("X-API-Version", headers.get("X-API-Version") || "2");
  if (apiKey && !headers.has("X-API-Key")) headers.set("X-API-Key", apiKey);
  if (metodo === "POST" && path !== "/api/chaves-api" && !headers.has("X-Idempotency-Key")) {
    const idempotencyKey = window.crypto?.randomUUID
      ? window.crypto.randomUUID()
      : `idem-${Date.now()}-${Math.random().toString(16).slice(2)}`;
    headers.set("X-Idempotency-Key", idempotencyKey);
  }
  opcoes = { ...opcoes, method: metodo, headers };
  let request=null;
  if(opcoes.body) { try { request=JSON.parse(opcoes.body); } catch { request=opcoes.body; } }
  try {
    const resposta=await fetch(url,opcoes);
    const texto=await resposta.text();
    let corpo=null; if(texto) { try { corpo=JSON.parse(texto); } catch { corpo=texto; } }
    const headers=[...resposta.headers.entries()].map(([k,v])=>`${k}: ${v}`).join("\n");
    if(registrarNoInspector) exibirTransacao({metodo,url,status:resposta.status,statusText:resposta.statusText,request,response:corpo,headers});
    if(!resposta.ok) {
      const detalhe=typeof corpo==="string"?corpo:formatarJSON(corpo);
      throw new Error(`HTTP ${resposta.status} ${resposta.statusText}${detalhe && detalhe!=="null"?" — "+detalhe:""}`);
    }
    return {corpo,status:resposta.status};
  } catch(e) {
    if(e instanceof TypeError) { if(registrarNoInspector) exibirTransacao({metodo,url,request,response:{erro:e.message},status:null}); throw new Error(`Não foi possível conectar à API em ${url}. Confira se o servidor está disponível e se o CORS permite a origem do frontend.`); }
    throw e;
  }
}
async function gerarChaveApi() {
  const nome = prompt("Nome para identificar esta chave:", "Frontend Biblioteca");
  if (!nome || !nome.trim()) return;
  const plano = $("#planoChave").value;
  try {
    const { corpo } = await requisicao("/api/chaves-api", {
      method: "POST",
      headers: { "Content-Type": "application/json", Accept: "application/json" },
      body: JSON.stringify({ nome: nome.trim(), plano })
    });
    apiKey = corpo.apiKey;
    localStorage.setItem("biblioteca-api-key", apiKey);
    $("#apiKey").value = apiKey;
    mostrarMensagem(`Chave criada para o plano ${corpo.plano}. Guarde-a: ela será exibida integralmente apenas agora.`);
    await carregarLista(false);
  } catch (e) {
    mostrarMensagem(e.message, true);
  }
}
function salvarChaveApi() {
  apiKey = $("#apiKey").value.trim();
  if (apiKey) localStorage.setItem("biblioteca-api-key", apiKey);
  else localStorage.removeItem("biblioteca-api-key");
  mostrarMensagem(apiKey ? "Chave de API salva neste navegador." : "Chave de API removida.");
  carregarLista();
}
async function executarConsole() {
  const metodo = $("#consoleMetodo").value;
  const caminho = $("#consoleCaminho").value.trim();
  if (!caminho.startsWith("/")) {
    mostrarMensagem("O endpoint deve começar com /.", true);
    return;
  }
  const opcoes = { method: metodo, headers: { Accept: "application/hal+json, application/json" } };
  if (metodo === "POST" || metodo === "PUT") {
    const texto = $("#consoleBody").value.trim();
    if (texto) {
      try {
        JSON.parse(texto);
      } catch {
        mostrarMensagem("O corpo informado não é um JSON válido.", true);
        return;
      }
      opcoes.headers["Content-Type"] = "application/json";
      opcoes.body = texto;
    }
  }
  try {
    const { status } = await requisicao(caminho, opcoes);
    mostrarMensagem(`Requisição concluída. HTTP ${status}.`);
  } catch (e) {
    mostrarMensagem(e.message, true);
  }
}

function carregarExemploPost() {
  const config=modulos[moduloAtual];
  if(!config.exemplo){mostrarMensagem("Este endpoint não recebe um corpo POST.",true);return;}
  $("#consoleMetodo").value="POST";
  $("#consoleCaminho").value=config.endpoint;
  $("#consoleBody").value=formatarJSON(config.exemplo);
  mostrarMensagem(`Exemplo POST de ${config.titulo} carregado. O exemplo de livro e empréstimo depende dos registros relacionados indicados.`);
}

function extrairItens(dados) {
  if(Array.isArray(dados)) return dados;
  if(Array.isArray(dados?.content)) return dados.content;
  if(Array.isArray(dados?._embedded?.[moduloAtual])) return dados._embedded[moduloAtual];
  if(dados?._embedded) { const a=Object.values(dados._embedded).find(Array.isArray); if(a) return a; }
  if(dados?._integrado) { const a=Object.values(dados._integrado).find(Array.isArray); if(a) return a; }
  return [];
}
function criarCampos(valores={}) {
  const config=modulos[moduloAtual]; formulario.innerHTML="";
  config.campos.forEach(c=>{
    const wrap=document.createElement("label"); wrap.className="campo"; wrap.textContent=c.rotulo;
    let input;
    if(c.tipo==="select") { input=document.createElement("select"); c.opcoes.forEach(op=>{const o=document.createElement("option");o.value=op;o.textContent=op;input.append(o);}); }
    else { input=document.createElement("input"); input.type=c.tipo; }
    input.name=c.nome; input.id=`campo-${c.nome}`; if(c.obrigatorio) input.required=true;
    if(c.min!==undefined) input.min=String(c.min);
    if(c.max!==undefined) input.max=String(c.max);
    if(c.minlength!==undefined) input.minLength=c.minlength;
    if(c.maxlength!==undefined) input.maxLength=c.maxlength;
    if(c.maxDateHoje) input.max=new Date().toISOString().slice(0,10);
    const v=valores[c.nome]; if(v!==undefined&&v!==null) input.value=v;
    wrap.appendChild(input); formulario.appendChild(wrap);
  });
}
function criarTela() {
  const config=modulos[moduloAtual];
  $("#tituloModulo").textContent=config.titulo;
  $("#tituloLista").textContent=`Lista de ${config.titulo.toLowerCase()}`;
  $("#btnCadastrar").textContent=registroEditando===null?`Cadastrar ${config.singular}`:`Salvar alterações`;
  $("#btnCancelar").hidden=registroEditando===null;
  $("#origemApi").value=origem;
  $("#consoleCaminho").value=config.endpoint;
  atualizarLinkSwagger();
  criarCampos(); carregarLista();
}
function renderizarLista(dados) {
  itensAtuais=extrairItens(dados); lista.innerHTML="";
  if(!itensAtuais.length) { lista.textContent="Nenhum registro nesta página."; return; }
  itensAtuais.forEach(item=>{
    const card=document.createElement("article"); card.className="item";
    const titulo=document.createElement("strong"); titulo.textContent=item.titulo||item.nome||`${modulos[moduloAtual].singular} #${item.id}`;
    const resumo=document.createElement("p"); resumo.textContent=Object.entries(item).filter(([k,v])=>!['_links','categorias'].includes(k)&&typeof v!=="object"&&k!=="id").map(([k,v])=>`${k}: ${v??""}`).join(" · ");
    const acoes=document.createElement("div"); acoes.className="acoes";
    const detalhe=document.createElement("button"); detalhe.type="button"; detalhe.textContent="Ver response"; detalhe.onclick=async()=>{try{await requisicao(`${modulos[moduloAtual].endpoint}/${item.id}`,{headers:{Accept:"application/hal+json, application/json"}});}catch(e){mostrarMensagem(e.message,true);}};
    const editar=document.createElement("button"); editar.type="button"; editar.textContent="Editar"; editar.onclick=()=>editarItem(item);
    const excluir=document.createElement("button"); excluir.type="button"; excluir.className="danger"; excluir.textContent="Excluir"; excluir.onclick=()=>excluirItem(item);
    acoes.append(detalhe,editar,excluir); card.append(titulo,resumo,acoes); lista.append(card);
  });
}
async function carregarLista(registrarNoInspector=true) {
  lista.textContent="Carregando...";
  const path=`${modulos[moduloAtual].endpoint}?page=${paginaAtual}&size=${tamanhoPagina}&sort=id,asc`;
  try { const {corpo}=await requisicao(path,{headers:{Accept:"application/hal+json, application/json"}},registrarNoInspector); renderizarLista(corpo||{}); const p=corpo?.page||corpo?.página||{}; $("#paginacaoInfo").textContent=`Página ${Number(p.number??p.número??paginaAtual)+1}${p.totalPages!=null?` de ${p.totalPages}`:""} · ${p.totalElements??p.totalElementos??itensAtuais.length} registro(s)`; }
  catch(e) { lista.textContent="Falha ao carregar os dados."; mostrarMensagem(e.message,true); }
}
function coletarValores() { return Object.fromEntries([...new FormData(formulario).entries()].filter(([,valor])=>String(valor).trim()!=="")); }
async function salvar() {
  if(!formulario.reportValidity()) return;
  const config=modulos[moduloAtual], valores=coletarValores();
  const corpo=config.montar?config.montar(valores):valores;
  const metodo=registroEditando===null?"POST":"PUT";
  const path=registroEditando===null?config.endpoint:`${config.endpoint}/${registroEditando}`;
  try {
    const {status}=await requisicao(path,{method:metodo,headers:{"Content-Type":"application/json",Accept:"application/hal+json, application/json"},body:JSON.stringify(corpo)});
    mostrarMensagem(`${config.singular} ${registroEditando===null?"cadastrado":"atualizado"} com sucesso. HTTP ${status}.`);
    registroEditando=null; $("#tituloFormulario").textContent="Cadastrar registro"; $("#btnCadastrar").textContent=`Cadastrar ${config.singular}`; $("#btnCancelar").hidden=true; criarCampos(); await carregarLista(false);
  } catch(e) { mostrarMensagem(e.message,true); }
}
function editarItem(item) {
  registroEditando=item.id; $("#tituloFormulario").textContent=`Editar ${modulos[moduloAtual].singular} #${item.id}`;
  $("#btnCadastrar").textContent="Salvar alterações"; $("#btnCancelar").hidden=false;
  const valores=modulos[moduloAtual].preencher?modulos[moduloAtual].preencher(item):item;
  criarCampos(valores); mostrarMensagem(`Editando registro #${item.id}. O PUT enviará o JSON exibido no painel Request.`);
  window.scrollTo({top:0,behavior:"smooth"});
}
async function excluirItem(item) {
  if(!confirm(`Deseja excluir ${modulos[moduloAtual].singular} #${item.id}?`)) return;
  try { const {status}=await requisicao(`${modulos[moduloAtual].endpoint}/${item.id}`,{method:"DELETE",headers:{Accept:"application/json"}}); mostrarMensagem(`Exclusão concluída. HTTP ${status}.`); await carregarLista(false); }
  catch(e) { mostrarMensagem(e.message,true); }
}
function cancelarEdicao() { registroEditando=null; $("#tituloFormulario").textContent="Cadastrar registro"; $("#btnCadastrar").textContent=`Cadastrar ${modulos[moduloAtual].singular}`; $("#btnCancelar").hidden=true; criarCampos(); mostrarMensagem(""); }
$("#menu").addEventListener("click",e=>{const b=e.target.closest("button[data-modulo]");if(!b)return;moduloAtual=b.dataset.modulo;paginaAtual=0;registroEditando=null;document.querySelectorAll("#menu button").forEach(x=>x.classList.toggle("active",x===b));criarTela();});
$("#formulario").addEventListener("submit",e=>e.preventDefault());
$("#btnCadastrar").addEventListener("click",salvar);
$("#btnCancelar").addEventListener("click",cancelarEdicao);
$("#btnAtualizar").addEventListener("click",carregarLista);
$("#origemApi").addEventListener("change",e=>{origem=e.target.value;localStorage.setItem("biblioteca-api-origem",origem);atualizarLinkSwagger();mostrarMensagem(`API selecionada: ${API_ORIGENS[origem]}`);carregarLista();});
$("#paginaAnterior").addEventListener("click",()=>{if(paginaAtual>0){paginaAtual--;carregarLista();}});
$("#proximaPagina").addEventListener("click",()=>{paginaAtual++;carregarLista();});
$("#tamanhoPagina").addEventListener("change",e=>{tamanhoPagina=Number(e.target.value);paginaAtual=0;carregarLista();});
$("#limparInspector").addEventListener("click",()=>exibirTransacao({}));
$("#btnSalvarChave").addEventListener("click", salvarChaveApi);
$("#btnGerarChave").addEventListener("click", gerarChaveApi);
$("#btnExecutarConsole").addEventListener("click", executarConsole);
$("#btnExemploPost").addEventListener("click", carregarExemploPost);
$("#apiKey").value = apiKey;
criarTela();
