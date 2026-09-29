package com.biblioteca.api.controller;

import com.biblioteca.api.model.Emprestimo;
import com.biblioteca.api.model.StatusEmprestimo;
import com.biblioteca.api.service.EmprestimoService;
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
@RequestMapping("/api/emprestimos")
@Tag(
        name = "Empréstimos",
        description = "Gerenciamento de empréstimos de livros."
)
public class EmprestimoController {

    private final EmprestimoService emprestimoService;

    public EmprestimoController(EmprestimoService emprestimoService) {
        this.emprestimoService = emprestimoService;
    }

    @PostMapping
    @Operation(
            operationId = "criarEmprestimo",
            summary = "Cadastrar empréstimo",
            description = "Cadastra um novo empréstimo de livro."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Empréstimo cadastrado com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Emprestimo.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "id": 1,
                                              "dataEmprestimo": "2026-09-29",
                                              "dataPrevistaDevolucao": "2026-10-06",
                                              "dataDevolucao": null,
                                              "status": "ATIVO",
                                              "leitor": {
                                                "id": 1
                                              },
                                              "livro": {
                                                "id": 1
                                              }
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
    public ResponseEntity<Emprestimo> criar(
            @RequestBody(
                    description = "Dados do empréstimo.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Emprestimo.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "dataEmprestimo": "2026-09-29",
                                              "dataPrevistaDevolucao": "2026-10-06",
                                              "dataDevolucao": null,
                                              "status": "ATIVO",
                                              "leitor": {
                                                "id": 1
                                              },
                                              "livro": {
                                                "id": 1
                                              }
                                            }
                                            """
                            )
                    )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody Emprestimo emprestimo) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(emprestimoService.salvar(emprestimo));
    }

    @GetMapping
    @Operation(
            operationId = "listarEmprestimos",
            summary = "Listar empréstimos",
            description = "Lista os empréstimos cadastrados com paginação."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Empréstimos listados com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            )
    })
    public ResponseEntity<Page<Emprestimo>> listar(
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
                emprestimoService.listar(PageRequest.of(page, size))
        );
    }

    @GetMapping("/{id}")
    @Operation(
            operationId = "buscarEmprestimoPorId",
            summary = "Buscar empréstimo por ID",
            description = "Consulta um empréstimo pelo ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Empréstimo encontrado com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Emprestimo.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Empréstimo não encontrado."
            )
    })
    public ResponseEntity<Emprestimo> buscarPorId(
            @Parameter(
                    description = "ID do empréstimo.",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id) {

        return ResponseEntity.ok(
                emprestimoService.buscarPorId(id)
        );
    }

    @PutMapping("/{id}")
    @Operation(
            operationId = "atualizarEmprestimo",
            summary = "Atualizar empréstimo",
            description = "Atualiza os dados de um empréstimo existente."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Empréstimo atualizado com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Emprestimo.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Empréstimo não encontrado."
            )
    })
    public ResponseEntity<Emprestimo> atualizar(
            @Parameter(
                    description = "ID do empréstimo.",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id,

            @RequestBody(
                    description = "Novos dados do empréstimo.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Emprestimo.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "dataEmprestimo": "2026-09-29",
                                              "dataPrevistaDevolucao": "2026-10-06",
                                              "dataDevolucao": "2026-10-05",
                                              "status": "DEVOLVIDO",
                                              "leitor": {
                                                "id": 1
                                              },
                                              "livro": {
                                                "id": 1
                                              }
                                            }
                                            """
                            )
                    )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody Emprestimo emprestimo) {

        return ResponseEntity.ok(
                emprestimoService.atualizar(id, emprestimo)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(
            operationId = "excluirEmprestimo",
            summary = "Excluir empréstimo",
            description = "Exclui um empréstimo pelo ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Empréstimo excluído com sucesso."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Empréstimo não encontrado."
            )
    })
    public ResponseEntity<Void> excluir(
            @Parameter(
                    description = "ID do empréstimo.",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id) {

        emprestimoService.excluir(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    @Operation(
            operationId = "buscarEmprestimosPorStatus",
            summary = "Buscar empréstimos por status",
            description = "Consulta empréstimos pelo status, com paginação."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Busca realizada com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Status informado é inválido."
            )
    })
    public ResponseEntity<Page<Emprestimo>> buscarPorStatus(
            @Parameter(
                    description = "Status do empréstimo.",
                    required = true,
                    example = "ATIVO",
                    schema = @Schema(implementation = StatusEmprestimo.class)
            )
            @RequestParam StatusEmprestimo status,

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
                emprestimoService.buscarPorStatus(
                        status,
                        PageRequest.of(page, size)
                )
        );
    }
}