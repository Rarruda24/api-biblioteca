package com.biblioteca.api.repository;

import com.biblioteca.api.model.Emprestimo;
import com.biblioteca.api.model.StatusEmprestimo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {

    Page<Emprestimo> findByStatus(
            StatusEmprestimo status,
            Pageable pageable
    );
}