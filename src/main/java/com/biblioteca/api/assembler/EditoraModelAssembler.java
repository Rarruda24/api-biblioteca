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
        var selfVersionado = VersionedLinks.forCurrentRequest("editoras", editora.getId(), "self");
        var deleteVersionado = VersionedLinks.forCurrentRequest("editoras", editora.getId(), "delete");
        var updateVersionado = VersionedLinks.forCurrentRequest("editoras", editora.getId(), "update");
        if (selfVersionado != null && deleteVersionado != null && updateVersionado != null)
            return EntityModel.of(editora, selfVersionado, updateVersionado, deleteVersionado);

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
