package com.biblioteca.api.assembler;

import com.biblioteca.api.controller.EmprestimoController;
import com.biblioteca.api.model.Emprestimo;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

@Component
public class EmprestimoModelAssembler
        implements RepresentationModelAssembler<Emprestimo, EntityModel<Emprestimo>> {

    @Override
    public EntityModel<Emprestimo> toModel(Emprestimo emprestimo) {

        return EntityModel.of(
                emprestimo,

                WebMvcLinkBuilder
                        .linkTo(
                                WebMvcLinkBuilder
                                        .methodOn(EmprestimoController.class)
                                        .buscarPorId(emprestimo.getId())
                        )
                        .withSelfRel(),

                WebMvcLinkBuilder
                        .linkTo(
                                WebMvcLinkBuilder
                                        .methodOn(EmprestimoController.class)
                                        .excluir(emprestimo.getId())
                        )
                        .withRel("delete")
        );
    }
}