package com.biblioteca.api.controller;

import com.biblioteca.api.model.Categoria;
import com.biblioteca.api.service.CategoriaService;
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
@RequestMapping("/api/categorias")
@Tag(
        name = "Categorias",
        description = "Gerenciamento de categorias."
)
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @PostMapping
    @Operation(
            operationId = "criarCategoria",
            summary = "Cadastrar categoria",
            description = "Cadastra uma nova categoria."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Categoria cadastrada com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Categoria.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "id": 1,
                                              "nome": "Romance",
                                              "descricao": "Obras literárias de romance."
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
    public ResponseEntity<Categoria> criar(
            @RequestBody(
                    description = "Dados da categoria.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Categoria.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "nome": "Romance",
                                              "descricao": "Obras literárias de romance."
                                            }
                                            """
                            )
                    )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody Categoria categoria) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoriaService.salvar(categoria));
    }

    @GetMapping
    @Operation(
            operationId = "listarCategorias",
            summary = "Listar categorias",
            description = "Lista as categorias cadastradas com paginação."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categorias listadas com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            )
    })
    public ResponseEntity<Page<Categoria>> listar(
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
                categoriaService.listar(PageRequest.of(page, size))
        );
    }

    @GetMapping("/{id}")
    @Operation(
            operationId = "buscarCategoriaPorId",
            summary = "Buscar categoria por ID",
            description = "Consulta uma categoria pelo ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categoria encontrada com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Categoria.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Categoria não encontrada."
            )
    })
    public ResponseEntity<Categoria> buscarPorId(
            @Parameter(
                    description = "ID da categoria.",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id) {

        return ResponseEntity.ok(
                categoriaService.buscarPorId(id)
        );
    }

    @PutMapping("/{id}")
    @Operation(
            operationId = "atualizarCategoria",
            summary = "Atualizar categoria",
            description = "Atualiza os dados de uma categoria existente."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categoria atualizada com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Categoria.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Categoria não encontrada."
            )
    })
    public ResponseEntity<Categoria> atualizar(
            @Parameter(
                    description = "ID da categoria.",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id,

            @RequestBody(
                    description = "Novos dados da categoria.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Categoria.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "nome": "Romance",
                                              "descricao": "Obras literárias de romance."
                                            }
                                            """
                            )
                    )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody Categoria categoria) {

        return ResponseEntity.ok(
                categoriaService.atualizar(id, categoria)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(
            operationId = "excluirCategoria",
            summary = "Excluir categoria",
            description = "Exclui uma categoria pelo ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Categoria excluída com sucesso."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Categoria não encontrada."
            )
    })
    public ResponseEntity<Void> excluir(
            @Parameter(
                    description = "ID da categoria.",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id) {

        categoriaService.excluir(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    @Operation(
            operationId = "buscarCategoriasPorNome",
            summary = "Buscar categorias por nome",
            description = "Consulta categorias pelo nome ou parte do nome, com paginação."
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
    public ResponseEntity<Page<Categoria>> buscarPorNome(
            @Parameter(
                    description = "Nome ou parte do nome da categoria.",
                    required = true,
                    example = "Romance"
            )
            @RequestParam String nome,

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
                categoriaService.buscarPorNome(
                        nome,
                        PageRequest.of(page, size)
                )
        );
    }
}