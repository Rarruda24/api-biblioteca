package com.biblioteca.api.controller;

import com.biblioteca.api.model.Livro;
import com.biblioteca.api.service.LivroService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/livros")
public class LivroController {

    private final LivroService livroService;

    public LivroController(LivroService livroService) {
        this.livroService = livroService;
    }

    @PostMapping
    public ResponseEntity<Livro> criar(@Valid @RequestBody Livro livro) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(livroService.salvar(livro));
    }

    @GetMapping
    public ResponseEntity<Page<Livro>> listar(Pageable pageable) {
        return ResponseEntity.ok(
                livroService.listar(pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Livro> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(
                livroService.buscarPorId(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Livro> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody Livro livro) {

        return ResponseEntity.ok(
                livroService.atualizar(id, livro)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        livroService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    public ResponseEntity<Page<Livro>> buscarPorTitulo(
            @RequestParam String titulo,
            Pageable pageable) {

        return ResponseEntity.ok(
                livroService.buscarPorTitulo(titulo, pageable)
        );
    }
}