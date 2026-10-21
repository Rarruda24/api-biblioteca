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

const modulos = {
  livros: { titulo: "Livros", endpoint: "/api/livros", singular: "livro", campos: [
    { nome: "titulo", rotulo: "Título", tipo: "text", obrigatorio: true },
    { nome: "isbn", rotulo: "ISBN", tipo: "text", obrigatorio: true },
    { nome: "anoPublicacao", rotulo: "Ano de publicação", tipo: "number", obrigatorio: true },
    { nome: "quantidadePaginas", rotulo: "Quantidade de páginas", tipo: "number", obrigatorio: true },
    { nome: "autorId", rotulo: "ID do autor", tipo: "number", obrigatorio: true },
    { nome: "editoraId", rotulo: "ID da editora", tipo: "number", obrigatorio: true },
    { nome: "categoriasIds", rotulo: "IDs das categorias (separados por vírgula)", tipo: "text", obrigatorio: false }
  ], montar(v) { return { titulo:v.titulo, isbn:v.isbn, anoPublicacao:Number(v.anoPublicacao), quantidadePaginas:Number(v.quantidadePaginas), autor:{id:Number(v.autorId)}, editora:{id:Number(v.editoraId)}, categorias:(v.categoriasIds||"").split(",").map(x=>x.trim()).filter(Boolean).map(id=>({id:Number(id)})) }; }, preencher(i) { return {titulo:i.titulo,isbn:i.isbn,anoPublicacao:i.anoPublicacao,quantidadePaginas:i.quantidadePaginas,autorId:i.autor?.id ?? "",editoraId:i.editora?.id ?? "",categoriasIds:(i.categorias||[]).map(c=>c.id).join(",")}; } },
  autores: { titulo: "Autores", endpoint: "/api/autores", singular: "autor", campos: [
    {nome:"nome",rotulo:"Nome",tipo:"text",obrigatorio:true},{nome:"nacionalidade",rotulo:"Nacionalidade",tipo:"text",obrigatorio:true},{nome:"dataNascimento",rotulo:"Data de nascimento",tipo:"date",obrigatorio:false}
  ] },
  editoras: { titulo: "Editoras", endpoint: "/api/editoras", singular: "editora", campos: [
    {nome:"nome",rotulo:"Nome",tipo:"text",obrigatorio:true},{nome:"pais",rotulo:"País",tipo:"text",obrigatorio:false}
  ] },
  categorias: { titulo: "Categorias", endpoint: "/api/categorias", singular: "categoria", campos: [
    {nome:"nome",rotulo:"Nome",tipo:"text",obrigatorio:true},{nome:"descricao",rotulo:"Descrição",tipo:"text",obrigatorio:false}
  ] },
  leitores: { titulo: "Leitores", endpoint: "/api/leitores", singular: "leitor", campos: [
    {nome:"nome",rotulo:"Nome",tipo:"text",obrigatorio:true},{nome:"email",rotulo:"E-mail",tipo:"email",obrigatorio:true},{nome:"telefone",rotulo:"Telefone",tipo:"tel",obrigatorio:false}
  ] },
  emprestimos: { titulo: "Empréstimos", endpoint: "/api/emprestimos", singular: "empréstimo", campos: [
    {nome:"leitorId",rotulo:"ID do leitor",tipo:"number",obrigatorio:true},{nome:"livroId",rotulo:"ID do livro",tipo:"number",obrigatorio:true},{nome:"dataEmprestimo",rotulo:"Data do empréstimo",tipo:"date",obrigatorio:true},{nome:"dataPrevistaDevolucao",rotulo:"Data prevista de devolução",tipo:"date",obrigatorio:true},{nome:"dataDevolucao",rotulo:"Data da devolução (se devolvido)",tipo:"date",obrigatorio:false},{nome:"status",rotulo:"Status",tipo:"select",opcoes:["ATIVO","DEVOLVIDO","ATRASADO"],obrigatorio:true}
  ], montar(v) { const p={dataEmprestimo:v.dataEmprestimo,dataPrevistaDevolucao:v.dataPrevistaDevolucao,status:v.status,leitor:{id:Number(v.leitorId)},livro:{id:Number(v.livroId)}}; if(v.dataDevolucao) p.dataDevolucao=v.dataDevolucao; return p; }, preencher(i) { return {leitorId:i.leitor?.id??"",livroId:i.livro?.id??"",dataEmprestimo:i.dataEmprestimo??"",dataPrevistaDevolucao:i.dataPrevistaDevolucao??"",dataDevolucao:i.dataDevolucao??"",status:i.status??"ATIVO"}; } }
};

const $ = s => document.querySelector(s);
const formulario = $("#formulario"), lista = $("#lista"), mensagem = $("#mensagem");
function apiUrl(path) { return API_ORIGENS[origem] + path; }
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
async function requisicao(path, opcoes={}) {
  const url=apiUrl(path), metodo=opcoes.method||"GET";
  let request=null;
  if(opcoes.body) { try { request=JSON.parse(opcoes.body); } catch { request=opcoes.body; } }
  try {
    const resposta=await fetch(url,opcoes);
    const texto=await resposta.text();
    let corpo=null; if(texto) { try { corpo=JSON.parse(texto); } catch { corpo=texto; } }
    const headers=[...resposta.headers.entries()].map(([k,v])=>`${k}: ${v}`).join("\n");
    exibirTransacao({metodo,url,status:resposta.status,statusText:resposta.statusText,request,response:corpo,headers});
    if(!resposta.ok) {
      const detalhe=typeof corpo==="string"?corpo:formatarJSON(corpo);
      throw new Error(`HTTP ${resposta.status} ${resposta.statusText}${detalhe && detalhe!=="null"?" — "+detalhe:""}`);
    }
    return {corpo,status:resposta.status};
  } catch(e) {
    if(e instanceof TypeError) { exibirTransacao({metodo,url,request,response:{erro:e.message},status:null}); throw new Error(`Não foi possível conectar à API em ${url}. Confira se o servidor está disponível e se o CORS permite a origem do frontend.`); }
    throw e;
  }
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
    if(c.tipo==="number") input.min="0";
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
    const detalhe=document.createElement("button"); detalhe.type="button"; detalhe.textContent="Ver response"; detalhe.onclick=()=>{exibirTransacao({metodo:"GET",url:apiUrl(`${modulos[moduloAtual].endpoint}/${item.id}`),status:200,statusText:"(registro carregado na lista)",response:item});};
    const editar=document.createElement("button"); editar.type="button"; editar.textContent="Editar"; editar.onclick=()=>editarItem(item);
    const excluir=document.createElement("button"); excluir.type="button"; excluir.className="danger"; excluir.textContent="Excluir"; excluir.onclick=()=>excluirItem(item);
    acoes.append(detalhe,editar,excluir); card.append(titulo,resumo,acoes); lista.append(card);
  });
}
async function carregarLista() {
  lista.textContent="Carregando...";
  const path=`${modulos[moduloAtual].endpoint}?page=${paginaAtual}&size=${tamanhoPagina}&sort=id,asc`;
  try { const {corpo}=await requisicao(path,{headers:{Accept:"application/hal+json, application/json"}}); renderizarLista(corpo||{}); const p=corpo?.page||corpo?.página||{}; $("#paginacaoInfo").textContent=`Página ${Number(p.number??p.número??paginaAtual)+1}${p.totalPages!=null?` de ${p.totalPages}`:""} · ${p.totalElements??p.totalElementos??itensAtuais.length} registro(s)`; }
  catch(e) { lista.textContent="Falha ao carregar os dados."; mostrarMensagem(e.message,true); }
}
function coletarValores() { return Object.fromEntries(new FormData(formulario).entries()); }
async function salvar() {
  if(!formulario.reportValidity()) return;
  const config=modulos[moduloAtual], valores=coletarValores();
  const corpo=config.montar?config.montar(valores):valores;
  const metodo=registroEditando===null?"POST":"PUT";
  const path=registroEditando===null?config.endpoint:`${config.endpoint}/${registroEditando}`;
  try {
    const {status}=await requisicao(path,{method:metodo,headers:{"Content-Type":"application/json",Accept:"application/hal+json, application/json"},body:JSON.stringify(corpo)});
    mostrarMensagem(`${config.singular} ${registroEditando===null?"cadastrado":"atualizado"} com sucesso. HTTP ${status}.`);
    registroEditando=null; $("#tituloFormulario").textContent="Cadastrar registro"; $("#btnCadastrar").textContent=`Cadastrar ${config.singular}`; $("#btnCancelar").hidden=true; criarCampos(); await carregarLista();
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
  try { const {status}=await requisicao(`${modulos[moduloAtual].endpoint}/${item.id}`,{method:"DELETE",headers:{Accept:"application/json"}}); mostrarMensagem(`Exclusão concluída. HTTP ${status}.`); await carregarLista(); }
  catch(e) { mostrarMensagem(e.message,true); }
}
function cancelarEdicao() { registroEditando=null; $("#tituloFormulario").textContent="Cadastrar registro"; $("#btnCadastrar").textContent=`Cadastrar ${modulos[moduloAtual].singular}`; $("#btnCancelar").hidden=true; criarCampos(); mostrarMensagem(""); }
$("#menu").addEventListener("click",e=>{const b=e.target.closest("button[data-modulo]");if(!b)return;moduloAtual=b.dataset.modulo;paginaAtual=0;registroEditando=null;document.querySelectorAll("#menu button").forEach(x=>x.classList.toggle("active",x===b));criarTela();});
$("#formulario").addEventListener("submit",e=>e.preventDefault());
$("#btnCadastrar").addEventListener("click",salvar);
$("#btnCancelar").addEventListener("click",cancelarEdicao);
$("#btnAtualizar").addEventListener("click",carregarLista);
$("#origemApi").addEventListener("change",e=>{origem=e.target.value;localStorage.setItem("biblioteca-api-origem",origem);mostrarMensagem(`API selecionada: ${API_ORIGENS[origem]}`);carregarLista();});
$("#paginaAnterior").addEventListener("click",()=>{if(paginaAtual>0){paginaAtual--;carregarLista();}});
$("#proximaPagina").addEventListener("click",()=>{paginaAtual++;carregarLista();});
$("#tamanhoPagina").addEventListener("change",e=>{tamanhoPagina=Number(e.target.value);paginaAtual=0;carregarLista();});
$("#limparInspector").addEventListener("click",()=>exibirTransacao({}));
criarTela();
