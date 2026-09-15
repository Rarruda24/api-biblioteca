package com.biblioteca.api.controller;

import com.biblioteca.api.model.Leitor;
import com.biblioteca.api.service.LeitorService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leitores")
public class LeitorController {

    private final LeitorService leitorService;

    public LeitorController(LeitorService leitorService) {
        this.leitorService = leitorService;
    }

    @PostMapping
    public ResponseEntity<Leitor> criar(
            @Valid @RequestBody Leitor leitor) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(leitorService.salvar(leitor));
    }

    @GetMapping
    public ResponseEntity<Page<Leitor>> listar(Pageable pageable) {
        return ResponseEntity.ok(
                leitorService.listar(pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Leitor> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                leitorService.buscarPorId(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Leitor> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody Leitor leitor) {

        return ResponseEntity.ok(
                leitorService.atualizar(id, leitor)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id) {

        leitorService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    public ResponseEntity<Page<Leitor>> buscarPorNome(
            @RequestParam String nome,
            Pageable pageable) {

        return ResponseEntity.ok(
                leitorService.buscarPorNome(nome, pageable)
        );
    }
}