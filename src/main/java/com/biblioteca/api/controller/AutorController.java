package com.biblioteca.api.controller;

import com.biblioteca.api.model.Autor;
import com.biblioteca.api.service.AutorService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/autores")
public class AutorController {

    private final AutorService autorService;

    public AutorController(AutorService autorService) {
        this.autorService = autorService;
    }

    @PostMapping
    public ResponseEntity<Autor> criar(@Valid @RequestBody Autor autor) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(autorService.salvar(autor));
    }

    @GetMapping
    public ResponseEntity<Page<Autor>> listar(Pageable pageable) {
        return ResponseEntity.ok(autorService.listar(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Autor> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(autorService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Autor> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody Autor autor) {

        return ResponseEntity.ok(
                autorService.atualizar(id, autor)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        autorService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    public ResponseEntity<Page<Autor>> buscarPorNome(
            @RequestParam String nome,
            Pageable pageable) {

        return ResponseEntity.ok(
                autorService.buscarPorNome(nome, pageable)
        );
    }
}