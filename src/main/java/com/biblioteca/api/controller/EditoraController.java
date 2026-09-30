package com.biblioteca.api.controller;

import com.biblioteca.api.assembler.EditoraModelAssembler;
import com.biblioteca.api.model.Editora;
import com.biblioteca.api.service.EditoraService;
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
@RequestMapping("/api/editoras")
@Tag(
        name = "Editoras",
        description = "Gerenciamento de editoras."
)
public class EditoraController {

    private final EditoraService editoraService;
    private final EditoraModelAssembler assembler;
    private final PagedResourcesAssembler<Editora> pagedResourcesAssembler;

    public EditoraController(
            EditoraService editoraService,
            EditoraModelAssembler assembler,
            PagedResourcesAssembler<Editora> pagedResourcesAssembler) {

        this.editoraService = editoraService;
        this.assembler = assembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @PostMapping
    @Operation(
            summary = "Cadastrar editora",
            description = "Cadastra uma nova editora."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Editora cadastrada com sucesso."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos."
            )
    })
    public ResponseEntity<EntityModel<Editora>> criar(

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
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

            @Valid
            @RequestBody Editora editora) {

        Editora editoraCriada = editoraService.salvar(editora);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assembler.toModel(editoraCriada));
    }

    @GetMapping
    @Operation(
            summary = "Listar editoras",
            description = "Retorna uma lista paginada de editoras utilizando Pageable."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Editoras listadas com sucesso."
    )
    public ResponseEntity<PagedModel<EntityModel<Editora>>> listar(

            @ParameterObject
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "id"
            )
            Pageable pageable) {

        Page<Editora> pagina = editoraService.listar(pageable);

        return ResponseEntity.ok(
                pagedResourcesAssembler.toModel(
                        pagina,
                        assembler
                )
        );
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar editora por ID",
            description = "Consulta uma editora pelo identificador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Editora encontrada com sucesso."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Editora não encontrada."
            )
    })
    public EntityModel<Editora> buscarPorId(
            @PathVariable Long id) {

        Editora editora = editoraService.buscarPorId(id);

        return assembler.toModel(editora);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar editora",
            description = "Atualiza os dados de uma editora existente."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Editora atualizada com sucesso."
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
    public ResponseEntity<EntityModel<Editora>> atualizar(

            @PathVariable Long id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
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

            @Valid
            @RequestBody Editora editora) {

        Editora editoraAtualizada =
                editoraService.atualizar(id, editora);

        return ResponseEntity.ok(
                assembler.toModel(editoraAtualizada)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Excluir editora",
            description = "Exclui uma editora pelo identificador."
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
            @PathVariable Long id) {

        editoraService.excluir(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar editoras por nome",
            description = "Consulta editoras pelo nome ou parte do nome utilizando paginação."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Busca realizada com sucesso."
    )
    public ResponseEntity<PagedModel<EntityModel<Editora>>> buscarPorNome(

            @RequestParam String nome,

            @ParameterObject
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "id"
            )
            Pageable pageable) {

        Page<Editora> pagina =
                editoraService.buscarPorNome(
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