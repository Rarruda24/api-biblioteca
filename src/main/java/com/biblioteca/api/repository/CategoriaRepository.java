package com.biblioteca.api.repository;

import com.biblioteca.api.model.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Page<Categoria> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}