package com.biblioteca.api.controller;

import com.biblioteca.api.model.Editora;
import com.biblioteca.api.service.EditoraService;
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
@RequestMapping("/api/editoras")
@Tag(
        name = "Editoras",
        description = "Gerenciamento de editoras."
)
public class EditoraController {

    private final EditoraService editoraService;

    public EditoraController(EditoraService editoraService) {
        this.editoraService = editoraService;
    }

    @PostMapping
    @Operation(
            operationId = "criarEditora",
            summary = "Cadastrar editora",
            description = "Cadastra uma nova editora."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Editora cadastrada com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Editora.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "id": 1,
                                              "nome": "Arruda Editora",
                                              "pais": "Brasil"
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
    public ResponseEntity<Editora> criar(
            @RequestBody(
                    description = "Dados da editora.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Editora.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "nome": "Arruda Editora",
                                              "pais": "Brasil"
                                            }
                                            """
                            )
                    )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody Editora editora) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(editoraService.salvar(editora));
    }

    @GetMapping
    @Operation(
            operationId = "listarEditoras",
            summary = "Listar editoras",
            description = "Lista as editoras cadastradas com paginação."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Editoras listadas com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            )
    })
    public ResponseEntity<Page<Editora>> listar(
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
                editoraService.listar(PageRequest.of(page, size))
        );
    }

    @GetMapping("/{id}")
    @Operation(
            operationId = "buscarEditoraPorId",
            summary = "Buscar editora por ID",
            description = "Consulta uma editora pelo ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Editora encontrada com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Editora.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Editora não encontrada."
            )
    })
    public ResponseEntity<Editora> buscarPorId(
            @Parameter(
                    description = "ID da editora.",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id) {

        return ResponseEntity.ok(
                editoraService.buscarPorId(id)
        );
    }

    @PutMapping("/{id}")
    @Operation(
            operationId = "atualizarEditora",
            summary = "Atualizar editora",
            description = "Atualiza os dados de uma editora existente."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Editora atualizada com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Editora.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Editora não encontrada."
            )
    })
    public ResponseEntity<Editora> atualizar(
            @Parameter(
                    description = "ID da editora.",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id,

            @RequestBody(
                    description = "Novos dados da editora.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Editora.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "nome": "Arruda Editora",
                                              "pais": "Brasil"
                                            }
                                            """
                            )
                    )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody Editora editora) {

        return ResponseEntity.ok(
                editoraService.atualizar(id, editora)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(
            operationId = "excluirEditora",
            summary = "Excluir editora",
            description = "Exclui uma editora pelo ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Editora excluída com sucesso."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Editora não encontrada."
            )
    })
    public ResponseEntity<Void> excluir(
            @Parameter(
                    description = "ID da editora.",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id) {

        editoraService.excluir(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    @Operation(
            operationId = "buscarEditorasPorNome",
            summary = "Buscar editoras por nome",
            description = "Consulta editoras pelo nome ou parte do nome, com paginação."
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
    public ResponseEntity<Page<Editora>> buscarPorNome(
            @Parameter(
                    description = "Nome ou parte do nome da editora.",
                    required = true,
                    example = "Arruda"
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
                editoraService.buscarPorNome(
                        nome,
                        PageRequest.of(page, size)
                )
        );
    }
}