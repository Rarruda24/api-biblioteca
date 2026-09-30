package com.biblioteca.api.controller;

import com.biblioteca.api.assembler.EmprestimoModelAssembler;
import com.biblioteca.api.model.Emprestimo;
import com.biblioteca.api.model.StatusEmprestimo;
import com.biblioteca.api.service.EmprestimoService;
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
@RequestMapping("/api/emprestimos")
@Tag(
        name = "Empréstimos",
        description = "Gerenciamento de empréstimos de livros."
)
public class EmprestimoController {

    private final EmprestimoService emprestimoService;
    private final EmprestimoModelAssembler assembler;
    private final PagedResourcesAssembler<Emprestimo> pagedResourcesAssembler;

    public EmprestimoController(
            EmprestimoService emprestimoService,
            EmprestimoModelAssembler assembler,
            PagedResourcesAssembler<Emprestimo> pagedResourcesAssembler) {

        this.emprestimoService = emprestimoService;
        this.assembler = assembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @PostMapping
    @Operation(
            summary = "Cadastrar empréstimo",
            description = "Cadastra um novo empréstimo de livro."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Empréstimo cadastrado com sucesso."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos."
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Regra de negócio não permitida. A data prevista de devolução não pode ser anterior à data do empréstimo."
            )
    })
    public ResponseEntity<EntityModel<Emprestimo>> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
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
            @Valid
            @RequestBody Emprestimo emprestimo) {

        Emprestimo emprestimoCriado =
                emprestimoService.salvar(emprestimo);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assembler.toModel(emprestimoCriado));
    }

    @GetMapping
    @Operation(
            summary = "Listar empréstimos",
            description = "Retorna uma lista paginada de empréstimos utilizando Pageable."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Empréstimos listados com sucesso."
    )
    public ResponseEntity<PagedModel<EntityModel<Emprestimo>>> listar(
            @ParameterObject
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "id"
            )
            Pageable pageable) {

        Page<Emprestimo> pagina =
                emprestimoService.listar(pageable);

        return ResponseEntity.ok(
                pagedResourcesAssembler.toModel(
                        pagina,
                        assembler
                )
        );
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar empréstimo por ID",
            description = "Consulta um empréstimo pelo identificador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Empréstimo encontrado com sucesso."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Empréstimo não encontrado."
            )
    })
    public EntityModel<Emprestimo> buscarPorId(
            @PathVariable Long id) {

        Emprestimo emprestimo =
                emprestimoService.buscarPorId(id);

        return assembler.toModel(emprestimo);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar empréstimo",
            description = "Atualiza os dados de um empréstimo existente."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Empréstimo atualizado com sucesso."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Empréstimo não encontrado."
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Regra de negócio não permitida. A data prevista de devolução não pode ser anterior à data do empréstimo."
            )
    })
    public ResponseEntity<EntityModel<Emprestimo>> atualizar(
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
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
            @Valid
            @RequestBody Emprestimo emprestimo) {

        Emprestimo emprestimoAtualizado =
                emprestimoService.atualizar(id, emprestimo);

        return ResponseEntity.ok(
                assembler.toModel(emprestimoAtualizado)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Excluir empréstimo",
            description = "Exclui um empréstimo pelo identificador."
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
            @PathVariable Long id) {

        emprestimoService.excluir(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar empréstimos por status",
            description = "Consulta empréstimos pelo status utilizando paginação."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Busca realizada com sucesso."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Status informado é inválido."
            )
    })
    public ResponseEntity<PagedModel<EntityModel<Emprestimo>>> buscarPorStatus(
            @RequestParam StatusEmprestimo status,
            @ParameterObject
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "id"
            )
            Pageable pageable) {

        Page<Emprestimo> pagina =
                emprestimoService.buscarPorStatus(
                        status,
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