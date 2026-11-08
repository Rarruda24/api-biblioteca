package com.biblioteca.api.controller;

import com.biblioteca.api.model.PlanoAcesso;
import com.biblioteca.api.service.ChaveApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chaves-api")
@Tag(name = "Chaves de API", description = "Geração, consulta e revogação de chaves. Planos: BASICO 30, PROFISSIONAL 100 e PREMIUM 300 requisições por minuto. Ao exceder o limite, a API responde 429 com Retry-After e os cabeçalhos X-RateLimit.")
public class ChaveApiController {
    private final ChaveApiService service;

    public ChaveApiController(ChaveApiService service) { this.service = service; }

    @PostMapping
    @Operation(summary = "Gerar chave de API",
            description = "Cria uma chave vinculada a um plano. O valor completo e exibido somente nesta resposta.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Chave criada; o segredo sera exibido uma unica vez."),
            @ApiResponse(responseCode = "400", description = "Nome ou plano invalido.")
    })
    public ResponseEntity<ChaveApiService.CriacaoChave> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Informe nome e plano. Esta operacao publica nao exige X-API-Key.",
                    required = true,
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                            {"nome":"Aplicacao Biblioteca","plano":"BASICO"}
                            """)))
            @Valid @RequestBody CriarChaveRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(request.nome(), request.plano()));
    }

    @GetMapping
    @Operation(summary = "Listar chaves de API", description = "Requer X-API-Key de uma chave PREMIUM. Retorna metadados; o segredo nunca e retornado. No Swagger, use Authorize para enviar a chave.")
    public ResponseEntity<?> listar() { return ResponseEntity.ok(service.listar()); }

    @DeleteMapping("/{id}")
    @Operation(summary = "Revogar chave de API", description = "Requer X-API-Key de uma chave PREMIUM no cabeçalho. No Swagger, use Authorize para enviar a chave.")
    public ResponseEntity<Void> revogar(@PathVariable Long id) {
        service.revogar(id);
        return ResponseEntity.noContent().build();
    }

    public record CriarChaveRequest(
            @NotBlank(message = "O nome da chave e obrigatorio")
            @Size(min = 3, max = 80, message = "O nome deve ter entre 3 e 80 caracteres") String nome,
            @NotNull(message = "O plano e obrigatorio") PlanoAcesso plano) { }
}
