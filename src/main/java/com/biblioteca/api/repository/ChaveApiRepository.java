package com.biblioteca.api.repository;

import com.biblioteca.api.model.ChaveApi;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ChaveApiRepository extends JpaRepository<ChaveApi, Long> {
    Optional<ChaveApi> findByHashChaveAndAtivaTrue(String hashChave);
}
