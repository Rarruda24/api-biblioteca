package com.biblioteca.api.controller;

import com.biblioteca.api.model.Categoria;
import com.biblioteca.api.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @PostMapping
    public ResponseEntity<Categoria> criar(
            @Valid @RequestBody Categoria categoria) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoriaService.salvar(categoria));
    }

    @GetMapping
    public ResponseEntity<Page<Categoria>> listar(Pageable pageable) {
        return ResponseEntity.ok(
                categoriaService.listar(pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Categoria> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                categoriaService.buscarPorId(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Categoria> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody Categoria categoria) {

        return ResponseEntity.ok(
                categoriaService.atualizar(id, categoria)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id) {

        categoriaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    public ResponseEntity<Page<Categoria>> buscarPorNome(
            @RequestParam String nome,
            Pageable pageable) {

        return ResponseEntity.ok(
                categoriaService.buscarPorNome(nome, pageable)
        );
    }
}