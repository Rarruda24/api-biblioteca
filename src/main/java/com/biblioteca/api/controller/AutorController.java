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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
                                              "nome": "Rodrigo Arruda",
                                              "nacionalidade": "Brasileira",
                                              "dataNascimento": "2006-08-24"
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
                                              "nome": "Rodrigo Arruda",
                                              "nacionalidade": "Brasileira",
                                              "dataNascimento": "2006-08-24"
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
                autorService.listar(
                        PageRequest.of(page, size)
                )
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
                                              "nome": "Rodrigo Arruda",
                                              "nacionalidade": "Brasileira",
                                              "dataNascimento": "2006-08-24"
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
                    example = "Rodrigo"
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
                autorService.buscarPorNome(
                        nome,
                        PageRequest.of(page, size)
                )
        );
    }
}