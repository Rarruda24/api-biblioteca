package com.biblioteca.api.controller;

import com.biblioteca.api.assembler.LeitorModelAssembler;
import com.biblioteca.api.model.Leitor;
import com.biblioteca.api.service.LeitorService;
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
@RequestMapping("/api/leitores")
@Tag(
        name = "Leitores",
        description = "Gerenciamento de leitores."
)
public class LeitorController {

    private final LeitorService leitorService;
    private final LeitorModelAssembler assembler;
    private final PagedResourcesAssembler<Leitor> pagedResourcesAssembler;

    public LeitorController(
            LeitorService leitorService,
            LeitorModelAssembler assembler,
            PagedResourcesAssembler<Leitor> pagedResourcesAssembler) {

        this.leitorService = leitorService;
        this.assembler = assembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @PostMapping
    @Operation(
            summary = "Cadastrar leitor",
            description = "Cadastra um novo leitor."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Leitor cadastrado com sucesso."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos."
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "E-mail já cadastrado."
            )
    })
    public ResponseEntity<EntityModel<Leitor>> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados do leitor.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Leitor.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "nome": "Rodrigo Arruda",
                                      "email": "rodrigo@teste.com",
                                      "telefone": "11999999999"
                                    }
                                    """
                            )
                    )
            )
            @Valid
            @RequestBody Leitor leitor) {

        Leitor leitorCriado =
                leitorService.salvar(leitor);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assembler.toModel(leitorCriado));
    }

    @GetMapping
    @Operation(
            summary = "Listar leitores",
            description = "Retorna uma lista paginada de leitores utilizando Pageable."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Leitores listados com sucesso."
    )
    public ResponseEntity<PagedModel<EntityModel<Leitor>>> listar(
            @ParameterObject
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "id"
            )
            Pageable pageable) {

        Page<Leitor> pagina =
                leitorService.listar(pageable);

        return ResponseEntity.ok(
                pagedResourcesAssembler.toModel(
                        pagina,
                        assembler
                )
        );
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar leitor por ID",
            description = "Consulta um leitor pelo identificador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Leitor encontrado com sucesso."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Leitor não encontrado."
            )
    })
    public EntityModel<Leitor> buscarPorId(
            @PathVariable Long id) {

        Leitor leitor =
                leitorService.buscarPorId(id);

        return assembler.toModel(leitor);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar leitor",
            description = "Atualiza os dados de um leitor existente."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Leitor atualizado com sucesso."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Leitor não encontrado."
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "E-mail já utilizado por outro leitor."
            )
    })
    public ResponseEntity<EntityModel<Leitor>> atualizar(
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Novos dados do leitor.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Leitor.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "nome": "Rodrigo Arruda",
                                      "email": "rodrigo@teste.com",
                                      "telefone": "11999999999"
                                    }
                                    """
                            )
                    )
            )
            @Valid
            @RequestBody Leitor leitor) {

        Leitor leitorAtualizado =
                leitorService.atualizar(id, leitor);

        return ResponseEntity.ok(
                assembler.toModel(leitorAtualizado)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Excluir leitor",
            description = "Exclui um leitor pelo identificador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Leitor excluído com sucesso."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Leitor não encontrado."
            )
    })
    public ResponseEntity<Void> excluir(
            @PathVariable Long id) {

        leitorService.excluir(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar leitores por nome",
            description = "Consulta leitores pelo nome ou parte do nome utilizando paginação."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Busca realizada com sucesso."
    )
    public ResponseEntity<PagedModel<EntityModel<Leitor>>> buscarPorNome(
            @RequestParam String nome,
            @ParameterObject
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "id"
            )
            Pageable pageable) {

        Page<Leitor> pagina =
                leitorService.buscarPorNome(
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