package com.exemplo.bibliotecafilmes.assembler;

import com.exemplo.bibliotecafilmes.controller.AvaliacaoController;
import com.exemplo.bibliotecafilmes.controller.UsuarioController;
import com.exemplo.bibliotecafilmes.dto.UsuarioResponseDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UsuarioModelAssembler implements RepresentationModelAssembler<UsuarioResponseDTO, EntityModel<UsuarioResponseDTO>> {

    @Override
    public EntityModel<UsuarioResponseDTO> toModel(UsuarioResponseDTO dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(UsuarioController.class).buscarPorId(dto.id())).withSelfRel(),
                linkTo(methodOn(UsuarioController.class).atualizar(dto.id(), null)).withRel("update"),
                linkTo(methodOn(UsuarioController.class).remover(dto.id())).withRel("delete"),
                linkTo(UsuarioController.class).withRel("all"),
                linkTo(AvaliacaoController.class).slash("usuario").slash(dto.id()).withRel("avaliacoes"));
    }
}
