package com.biblioteca.api.controller;

import com.biblioteca.api.assembler.CategoriaModelAssembler;
import com.biblioteca.api.model.Categoria;
import com.biblioteca.api.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
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
    private final CategoriaModelAssembler assembler;
    private final PagedResourcesAssembler<Categoria> pagedResourcesAssembler;

    public CategoriaController(
            CategoriaService categoriaService,
            CategoriaModelAssembler assembler,
            PagedResourcesAssembler<Categoria> pagedResourcesAssembler) {

        this.categoriaService = categoriaService;
        this.assembler = assembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @PostMapping
    @Operation(
            summary = "Cadastrar categoria",
            description = "Cadastra uma nova categoria."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Categoria cadastrada com sucesso."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos."
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Categoria já cadastrada."
            )
    })
    public ResponseEntity<EntityModel<Categoria>> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
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
            @Valid
            @RequestBody Categoria categoria) {

        Categoria categoriaCriada =
                categoriaService.salvar(categoria);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assembler.toModel(categoriaCriada));
    }

    @GetMapping
    @Operation(
            summary = "Listar categorias",
            description = "Retorna uma lista paginada de categorias utilizando Pageable."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Categorias listadas com sucesso."
    )
    public ResponseEntity<PagedModel<EntityModel<Categoria>>> listar(
            @ParameterObject
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "id"
            )
            Pageable pageable) {

        Page<Categoria> pagina =
                categoriaService.listar(pageable);

        return ResponseEntity.ok(
                pagedResourcesAssembler.toModel(
                        pagina,
                        assembler
                )
        );
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar categoria por ID",
            description = "Consulta uma categoria pelo identificador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categoria encontrada com sucesso."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Categoria não encontrada."
            )
    })
    public EntityModel<Categoria> buscarPorId(
            @PathVariable Long id) {

        Categoria categoria =
                categoriaService.buscarPorId(id);

        return assembler.toModel(categoria);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar categoria",
            description = "Atualiza os dados de uma categoria existente."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categoria atualizada com sucesso."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Categoria não encontrada."
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Nome já utilizado por outra categoria."
            )
    })
    public ResponseEntity<EntityModel<Categoria>> atualizar(
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
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
            @Valid
            @RequestBody Categoria categoria) {

        Categoria categoriaAtualizada =
                categoriaService.atualizar(id, categoria);

        return ResponseEntity.ok(
                assembler.toModel(categoriaAtualizada)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Excluir categoria",
            description = "Exclui uma categoria pelo identificador."
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
            @PathVariable Long id) {

        categoriaService.excluir(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar categorias por nome",
            description = "Consulta categorias pelo nome ou parte do nome utilizando paginação."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Busca realizada com sucesso."
    )
    public ResponseEntity<PagedModel<EntityModel<Categoria>>> buscarPorNome(
            @RequestParam String nome,
            @ParameterObject
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "id"
            )
            Pageable pageable) {

        Page<Categoria> pagina =
                categoriaService.buscarPorNome(
                        nome,
                        pageable
                );

        return ResponseEntity.ok(
                pagedResourcesAssembler.toModel(
                        pagina,
                        assembler
                )
        );
    }
}