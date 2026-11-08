package com.biblioteca.api.assembler;

import com.biblioteca.api.controller.LeitorController;
import com.biblioteca.api.model.Leitor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

@Component
public class LeitorModelAssembler
        implements RepresentationModelAssembler<Leitor, EntityModel<Leitor>> {

    @Override
    public EntityModel<Leitor> toModel(Leitor leitor) {
        var selfVersionado = VersionedLinks.forCurrentRequest("leitores", leitor.getId(), "self");
        var deleteVersionado = VersionedLinks.forCurrentRequest("leitores", leitor.getId(), "delete");
        var updateVersionado = VersionedLinks.forCurrentRequest("leitores", leitor.getId(), "update");
        if (selfVersionado != null && deleteVersionado != null && updateVersionado != null)
            return EntityModel.of(leitor, selfVersionado, updateVersionado, deleteVersionado);

        return EntityModel.of(
                leitor,

                WebMvcLinkBuilder
                        .linkTo(
                                WebMvcLinkBuilder
                                        .methodOn(LeitorController.class)
                                        .buscarPorId(leitor.getId())
                        )
                        .withSelfRel(),

                WebMvcLinkBuilder
                        .linkTo(
                                WebMvcLinkBuilder
                                        .methodOn(LeitorController.class)
                                        .excluir(leitor.getId())
                        )
                        .withRel("delete")
        );
    }
}
