package com.biblioteca.api.repository;

import com.biblioteca.api.model.Editora;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EditoraRepository extends JpaRepository<Editora, Long> {

    Page<Editora> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}