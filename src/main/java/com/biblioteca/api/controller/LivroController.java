package com.biblioteca.api.controller;

import com.biblioteca.api.assembler.LivroModelAssembler;
import com.biblioteca.api.model.Livro;
import com.biblioteca.api.service.LivroService;
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
@RequestMapping("/api/livros")
@Tag(
        name = "Livros",
        description = "Gerenciamento de livros da biblioteca"
)
public class LivroController {

    private final LivroService livroService;
    private final LivroModelAssembler assembler;
    private final PagedResourcesAssembler<Livro> pagedResourcesAssembler;

    public LivroController(
            LivroService livroService,
            LivroModelAssembler assembler,
            PagedResourcesAssembler<Livro> pagedResourcesAssembler) {

        this.livroService = livroService;
        this.assembler = assembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }


    @GetMapping
    @Operation(
            summary = "Listar livros",
            description = "Retorna uma lista paginada de livros utilizando Pageable."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Livros listados com sucesso."
    )
    public ResponseEntity<PagedModel<EntityModel<Livro>>> listar(
            @ParameterObject
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "id"
            )
            Pageable pageable) {


        Page<Livro> pagina = livroService.listar(pageable);

        return ResponseEntity.ok(
                pagedResourcesAssembler.toModel(
                        pagina,
                        assembler
                )
        );
    }


    @PostMapping
    @Operation(
            summary = "Cadastrar livro",
            description = "Realiza o cadastro de um novo livro."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Livro cadastrado com sucesso."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos."
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "ISBN já cadastrado."
            )
    })
    public ResponseEntity<EntityModel<Livro>> criar(

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados do livro.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = Livro.class
                            ),
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

            @Valid
            @RequestBody Livro livro) {


        Livro livroCriado = livroService.salvar(livro);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assembler.toModel(livroCriado));
    }


    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar livro por ID",
            description = "Retorna um livro através do identificador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Livro encontrado."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Livro não encontrado."
            )
    })
    public EntityModel<Livro> buscarPorId(
            @PathVariable Long id) {


        Livro livro = livroService.buscarPorId(id);

        return assembler.toModel(livro);
    }


    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar livro",
            description = "Atualiza os dados de um livro existente."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Livro atualizado com sucesso."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Livro não encontrado."
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "ISBN já utilizado por outro livro."
            )
    })
    public ResponseEntity<EntityModel<Livro>> atualizar(

            @PathVariable Long id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Novos dados do livro.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = Livro.class
                            ),
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

            @Valid
            @RequestBody Livro livro) {


        Livro livroAtualizado = livroService.atualizar(id, livro);

        return ResponseEntity.ok(
                assembler.toModel(livroAtualizado)
        );
    }


    @DeleteMapping("/{id}")
    @Operation(
            summary = "Excluir livro",
            description = "Remove um livro pelo identificador."
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
            @PathVariable Long id) {


        livroService.excluir(id);

        return ResponseEntity.noContent().build();
    }


    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar livros por título",
            description = "Realiza uma busca personalizada pelo título utilizando paginação."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Busca realizada com sucesso."
    )
    public ResponseEntity<PagedModel<EntityModel<Livro>>> buscarPorTitulo(

            @RequestParam String titulo,

            @ParameterObject
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "id"
            )
            Pageable pageable) {


        Page<Livro> pagina =
                livroService.buscarPorTitulo(
                        titulo,
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