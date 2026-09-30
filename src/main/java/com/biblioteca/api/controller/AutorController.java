package com.biblioteca.api.controller;

import com.biblioteca.api.assembler.AutorModelAssembler;
import com.biblioteca.api.model.Autor;
import com.biblioteca.api.service.AutorService;
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
@RequestMapping("/api/autores")
@Tag(
        name = "Autores",
        description = "Gerenciamento de autores."
)
public class AutorController {

    private final AutorService autorService;
    private final AutorModelAssembler assembler;
    private final PagedResourcesAssembler<Autor> pagedResourcesAssembler;

    public AutorController(
            AutorService autorService,
            AutorModelAssembler assembler,
            PagedResourcesAssembler<Autor> pagedResourcesAssembler) {

        this.autorService = autorService;
        this.assembler = assembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @PostMapping
    @Operation(
            summary = "Cadastrar autor",
            description = "Cadastra um novo autor."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Autor cadastrado com sucesso."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos."
            )
    })
    public ResponseEntity<EntityModel<Autor>> criar(

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
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

            @Valid
            @RequestBody Autor autor) {

        Autor autorCriado = autorService.salvar(autor);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assembler.toModel(autorCriado));
    }

    @GetMapping
    @Operation(
            summary = "Listar autores",
            description = "Retorna uma lista paginada de autores utilizando Pageable."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Autores listados com sucesso."
    )
    public ResponseEntity<PagedModel<EntityModel<Autor>>> listar(

            @ParameterObject
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "id"
            )
            Pageable pageable) {

        Page<Autor> pagina = autorService.listar(pageable);

        return ResponseEntity.ok(
                pagedResourcesAssembler.toModel(
                        pagina,
                        assembler
                )
        );
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar autor por ID",
            description = "Consulta um autor pelo identificador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Autor encontrado com sucesso."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Autor não encontrado."
            )
    })
    public EntityModel<Autor> buscarPorId(
            @PathVariable Long id) {

        Autor autor = autorService.buscarPorId(id);

        return assembler.toModel(autor);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar autor",
            description = "Atualiza os dados de um autor existente."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Autor atualizado com sucesso."
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
    public ResponseEntity<EntityModel<Autor>> atualizar(

            @PathVariable Long id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
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

            @Valid
            @RequestBody Autor autor) {

        Autor autorAtualizado = autorService.atualizar(id, autor);

        return ResponseEntity.ok(
                assembler.toModel(autorAtualizado)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Excluir autor",
            description = "Exclui um autor pelo identificador."
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
            @PathVariable Long id) {

        autorService.excluir(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar autores por nome",
            description = "Consulta autores pelo nome ou parte do nome utilizando paginação."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Busca realizada com sucesso."
    )
    public ResponseEntity<PagedModel<EntityModel<Autor>>> buscarPorNome(

            @RequestParam String nome,

            @ParameterObject
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "id"
            )
            Pageable pageable) {

        Page<Autor> pagina =
                autorService.buscarPorNome(
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