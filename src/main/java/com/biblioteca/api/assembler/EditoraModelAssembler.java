package com.biblioteca.api.assembler;

import com.biblioteca.api.controller.EditoraController;
import com.biblioteca.api.model.Editora;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

@Component
public class EditoraModelAssembler
        implements RepresentationModelAssembler<Editora, EntityModel<Editora>> {

    @Override
    public EntityModel<Editora> toModel(Editora editora) {

        return EntityModel.of(
                editora,

                WebMvcLinkBuilder
                        .linkTo(
                                WebMvcLinkBuilder
                                        .methodOn(EditoraController.class)
                                        .buscarPorId(editora.getId())
                        )
                        .withSelfRel(),

                WebMvcLinkBuilder
                        .linkTo(
                                WebMvcLinkBuilder
                                        .methodOn(EditoraController.class)
                                        .excluir(editora.getId())
                        )
                        .withRel("delete")
        );
    }
}