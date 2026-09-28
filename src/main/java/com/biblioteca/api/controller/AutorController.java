package com.biblioteca.api.controller;

import com.biblioteca.api.model.Autor;
import com.biblioteca.api.service.AutorService;
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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/autores")
@Tag(
        name = "Autores",
        description = "Gerenciamento de autores."
)
public class AutorController {

    private final AutorService autorService;

    public AutorController(AutorService autorService) {
        this.autorService = autorService;
    }

    @PostMapping
    @Operation(
            operationId = "criarAutor",
            summary = "Cadastrar autor",
            description = "Cadastra um novo autor."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Autor cadastrado com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Autor.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "id": 1,
                                              "nome": "Machado de Assis",
                                              "nacionalidade": "Brasileira",
                                              "dataNascimento": "1839-06-21"
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
    public ResponseEntity<Autor> criar(
            @RequestBody(
                    description = "Dados do autor.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Autor.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "nome": "Machado de Assis",
                                              "nacionalidade": "Brasileira",
                                              "dataNascimento": "1839-06-21"
                                            }
                                            """
                            )
                    )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody Autor autor) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(autorService.salvar(autor));
    }

    @GetMapping
    @Operation(
            operationId = "listarAutores",
            summary = "Listar autores",
            description = "Lista os autores cadastrados com paginação."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Autores listados com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            )
    })
    public ResponseEntity<Page<Autor>> listar(
            @ParameterObject Pageable pageable) {

        return ResponseEntity.ok(
                autorService.listar(pageable)
        );
    }

    @GetMapping("/{id}")
    @Operation(
            operationId = "buscarAutorPorId",
            summary = "Buscar autor por ID",
            description = "Consulta um autor pelo ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Autor encontrado com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Autor.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Autor não encontrado."
            )
    })
    public ResponseEntity<Autor> buscarPorId(
            @Parameter(
                    description = "ID do autor.",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id) {

        return ResponseEntity.ok(
                autorService.buscarPorId(id)
        );
    }

    @PutMapping("/{id}")
    @Operation(
            operationId = "atualizarAutor",
            summary = "Atualizar autor",
            description = "Atualiza os dados de um autor existente."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Autor atualizado com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Autor.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Autor não encontrado."
            )
    })
    public ResponseEntity<Autor> atualizar(
            @Parameter(
                    description = "ID do autor.",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id,

            @RequestBody(
                    description = "Novos dados do autor.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Autor.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "nome": "Machado de Assis",
                                              "nacionalidade": "Brasileira",
                                              "dataNascimento": "1839-06-21"
                                            }
                                            """
                            )
                    )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody Autor autor) {

        return ResponseEntity.ok(
                autorService.atualizar(id, autor)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(
            operationId = "excluirAutor",
            summary = "Excluir autor",
            description = "Exclui um autor pelo ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Autor excluído com sucesso."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Autor não encontrado."
            )
    })
    public ResponseEntity<Void> excluir(
            @Parameter(
                    description = "ID do autor.",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id) {

        autorService.excluir(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    @Operation(
            operationId = "buscarAutoresPorNome",
            summary = "Buscar autores por nome",
            description = "Consulta autores pelo nome ou parte do nome, com paginação."
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
    public ResponseEntity<Page<Autor>> buscarPorNome(
            @Parameter(
                    description = "Nome ou parte do nome do autor.",
                    required = true,
                    example = "Machado"
            )
            @RequestParam String nome,

            @ParameterObject Pageable pageable) {

        return ResponseEntity.ok(
                autorService.buscarPorNome(nome, pageable)
        );
    }
}