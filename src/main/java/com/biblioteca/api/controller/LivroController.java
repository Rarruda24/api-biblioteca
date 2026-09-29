package com.biblioteca.api.controller;

import com.biblioteca.api.model.Livro;
import com.biblioteca.api.service.LivroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/livros")
@Tag(
        name = "Livros",
        description = "Gerenciamento de livros."
)
public class LivroController {

    private final LivroService livroService;

    public LivroController(LivroService livroService) {
        this.livroService = livroService;
    }

    @PostMapping
    @Operation(
            operationId = "criarLivro",
            summary = "Cadastrar livro",
            description = "Cadastra um novo livro."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Livro cadastrado com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Livro.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "id": 1,
                                              "titulo": "Dom Casmurro",
                                              "isbn": "9788535911665",
                                              "anoPublicacao": 1899,
                                              "quantidadePaginas": 256,
                                              "autor": {
                                                "id": 1
                                              },
                                              "editora": {
                                                "id": 1
                                              },
                                              "categorias": [
                                                {
                                                  "id": 1
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos."
            )
    })
    public ResponseEntity<Livro> criar(
            @RequestBody(
                    description = "Dados do livro.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Livro.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "titulo": "Dom Casmurro",
                                              "isbn": "9788535911665",
                                              "anoPublicacao": 1899,
                                              "quantidadePaginas": 256,
                                              "autor": {
                                                "id": 1
                                              },
                                              "editora": {
                                                "id": 1
                                              },
                                              "categorias": [
                                                {
                                                  "id": 1
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody Livro livro) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(livroService.salvar(livro));
    }

    @GetMapping
    @Operation(
            operationId = "listarLivros",
            summary = "Listar livros",
            description = "Lista os livros cadastrados com paginação."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Livros listados com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            )
    })
    public ResponseEntity<Page<Livro>> listar(
            @Parameter(
                    description = "Número da página.",
                    example = "0"
            )
            @RequestParam(defaultValue = "0") int page,

            @Parameter(
                    description = "Quantidade de registros por página.",
                    example = "20"
            )
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                livroService.listar(
                        PageRequest.of(page, size)
                )
        );
    }

    @GetMapping("/{id}")
    @Operation(
            operationId = "buscarLivroPorId",
            summary = "Buscar livro por ID",
            description = "Consulta um livro pelo ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Livro encontrado com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Livro.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Livro não encontrado."
            )
    })
    public ResponseEntity<Livro> buscarPorId(
            @Parameter(
                    description = "ID do livro.",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id) {

        return ResponseEntity.ok(
                livroService.buscarPorId(id)
        );
    }

    @PutMapping("/{id}")
    @Operation(
            operationId = "atualizarLivro",
            summary = "Atualizar livro",
            description = "Atualiza os dados de um livro existente."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Livro atualizado com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Livro.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Livro não encontrado."
            )
    })
    public ResponseEntity<Livro> atualizar(
            @Parameter(
                    description = "ID do livro.",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id,

            @RequestBody(
                    description = "Novos dados do livro.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Livro.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "titulo": "Dom Casmurro",
                                              "isbn": "9788535911665",
                                              "anoPublicacao": 1899,
                                              "quantidadePaginas": 256,
                                              "autor": {
                                                "id": 1
                                              },
                                              "editora": {
                                                "id": 1
                                              },
                                              "categorias": [
                                                {
                                                  "id": 1
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody Livro livro) {

        return ResponseEntity.ok(
                livroService.atualizar(id, livro)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(
            operationId = "excluirLivro",
            summary = "Excluir livro",
            description = "Exclui um livro pelo ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Livro excluído com sucesso."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Livro não encontrado."
            )
    })
    public ResponseEntity<Void> excluir(
            @Parameter(
                    description = "ID do livro.",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id) {

        livroService.excluir(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    @Operation(
            operationId = "buscarLivrosPorTitulo",
            summary = "Buscar livros por título",
            description = "Consulta livros pelo título ou parte do título, com paginação."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Busca realizada com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            )
    })
    public ResponseEntity<Page<Livro>> buscarPorTitulo(
            @Parameter(
                    description = "Título ou parte do título do livro.",
                    required = true,
                    example = "Dom"
            )
            @RequestParam String titulo,

            @Parameter(
                    description = "Número da página.",
                    example = "0"
            )
            @RequestParam(defaultValue = "0") int page,

            @Parameter(
                    description = "Quantidade de registros por página.",
                    example = "20"
            )
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                livroService.buscarPorTitulo(
                        titulo,
                        PageRequest.of(page, size)
                )
        );
    }
}