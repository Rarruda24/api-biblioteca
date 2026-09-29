package com.biblioteca.api.controller;

import com.biblioteca.api.model.Leitor;
import com.biblioteca.api.service.LeitorService;
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
@RequestMapping("/api/leitores")
@Tag(
        name = "Leitores",
        description = "Gerenciamento de leitores."
)
public class LeitorController {

    private final LeitorService leitorService;

    public LeitorController(LeitorService leitorService) {
        this.leitorService = leitorService;
    }

    @PostMapping
    @Operation(
            operationId = "criarLeitor",
            summary = "Cadastrar leitor",
            description = "Cadastra um novo leitor."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Leitor cadastrado com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Leitor.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "id": 1,
                                              "nome": "Rodrigo Arruda",
                                              "email": "rodrigo@teste.com",
                                              "telefone": "11999999999"
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
    public ResponseEntity<Leitor> criar(
            @RequestBody(
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
            @Valid @org.springframework.web.bind.annotation.RequestBody Leitor leitor) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(leitorService.salvar(leitor));
    }

    @GetMapping
    @Operation(
            operationId = "listarLeitores",
            summary = "Listar leitores",
            description = "Lista os leitores cadastrados com paginação."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Leitores listados com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            )
    })
    public ResponseEntity<Page<Leitor>> listar(
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
                leitorService.listar(
                        PageRequest.of(page, size)
                )
        );
    }

    @GetMapping("/{id}")
    @Operation(
            operationId = "buscarLeitorPorId",
            summary = "Buscar leitor por ID",
            description = "Consulta um leitor pelo ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Leitor encontrado com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Leitor.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Leitor não encontrado."
            )
    })
    public ResponseEntity<Leitor> buscarPorId(
            @Parameter(
                    description = "ID do leitor.",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id) {

        return ResponseEntity.ok(
                leitorService.buscarPorId(id)
        );
    }

    @PutMapping("/{id}")
    @Operation(
            operationId = "atualizarLeitor",
            summary = "Atualizar leitor",
            description = "Atualiza os dados de um leitor existente."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Leitor atualizado com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Leitor.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Leitor não encontrado."
            )
    })
    public ResponseEntity<Leitor> atualizar(
            @Parameter(
                    description = "ID do leitor.",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id,

            @RequestBody(
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
            @Valid @org.springframework.web.bind.annotation.RequestBody Leitor leitor) {

        return ResponseEntity.ok(
                leitorService.atualizar(id, leitor)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(
            operationId = "excluirLeitor",
            summary = "Excluir leitor",
            description = "Exclui um leitor pelo ID."
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
            @Parameter(
                    description = "ID do leitor.",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id) {

        leitorService.excluir(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    @Operation(
            operationId = "buscarLeitoresPorNome",
            summary = "Buscar leitores por nome",
            description = "Consulta leitores pelo nome ou parte do nome, com paginação."
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
    public ResponseEntity<Page<Leitor>> buscarPorNome(
            @Parameter(
                    description = "Nome ou parte do nome do leitor.",
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
                leitorService.buscarPorNome(
                        nome,
                        PageRequest.of(page, size)
                )
        );
    }
}