package com.biblioteca.api.controller;

import com.biblioteca.api.model.Editora;
import com.biblioteca.api.service.EditoraService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/editoras")
public class EditoraController {

    private final EditoraService editoraService;

    public EditoraController(EditoraService editoraService) {
        this.editoraService = editoraService;
    }

    @PostMapping
    public ResponseEntity<Editora> criar(@Valid @RequestBody Editora editora) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(editoraService.salvar(editora));
    }

    @GetMapping
    public ResponseEntity<Page<Editora>> listar(Pageable pageable) {
        return ResponseEntity.ok(
                editoraService.listar(pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Editora> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(
                editoraService.buscarPorId(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Editora> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody Editora editora) {

        return ResponseEntity.ok(
                editoraService.atualizar(id, editora)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        editoraService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    public ResponseEntity<Page<Editora>> buscarPorNome(
            @RequestParam String nome,
            Pageable pageable) {

        return ResponseEntity.ok(
                editoraService.buscarPorNome(nome, pageable)
        );
    }
}