package com.biblioteca.api.repository;

import com.biblioteca.api.model.Leitor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeitorRepository extends JpaRepository<Leitor, Long> {

    Page<Leitor> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}