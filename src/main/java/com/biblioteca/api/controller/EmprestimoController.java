package com.biblioteca.api.controller;

import com.biblioteca.api.model.Emprestimo;
import com.biblioteca.api.model.StatusEmprestimo;
import com.biblioteca.api.service.EmprestimoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/emprestimos")
public class EmprestimoController {

    private final EmprestimoService emprestimoService;

    public EmprestimoController(EmprestimoService emprestimoService) {
        this.emprestimoService = emprestimoService;
    }

    @PostMapping
    public ResponseEntity<Emprestimo> criar(
            @Valid @RequestBody Emprestimo emprestimo) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(emprestimoService.salvar(emprestimo));
    }

    @GetMapping
    public ResponseEntity<Page<Emprestimo>> listar(Pageable pageable) {
        return ResponseEntity.ok(
                emprestimoService.listar(pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Emprestimo> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                emprestimoService.buscarPorId(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Emprestimo> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody Emprestimo emprestimo) {

        return ResponseEntity.ok(
                emprestimoService.atualizar(id, emprestimo)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id) {

        emprestimoService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    public ResponseEntity<Page<Emprestimo>> buscarPorStatus(
            @RequestParam StatusEmprestimo status,
            Pageable pageable) {

        return ResponseEntity.ok(
                emprestimoService.buscarPorStatus(status, pageable)
        );
    }
}