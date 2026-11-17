package com.exemplo.bibliotecafilmes.assembler;

import com.exemplo.bibliotecafilmes.controller.ApiKeyController;
import com.exemplo.bibliotecafilmes.controller.UsuarioController;
import com.exemplo.bibliotecafilmes.dto.ApiKeyResponseDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ApiKeyModelAssembler implements RepresentationModelAssembler<ApiKeyResponseDTO, EntityModel<ApiKeyResponseDTO>> {

    @Override
    public EntityModel<ApiKeyResponseDTO> toModel(ApiKeyResponseDTO dto) {
        return EntityModel.of(dto, links(dto.id(), dto.usuarioId()));
    }

    /** Links de uma chave; também usado na resposta de criação (que traz o valor secreto). */
    public List<Link> links(Long id, Long usuarioId) {
        return List.of(
                linkTo(methodOn(ApiKeyController.class).buscarPorId(id)).withSelfRel(),
                linkTo(methodOn(ApiKeyController.class).atualizar(id, null)).withRel("update"),
                linkTo(methodOn(ApiKeyController.class).revogar(id)).withRel("delete"),
                linkTo(ApiKeyController.class).withRel("all"),
                linkTo(methodOn(UsuarioController.class).buscarPorId(usuarioId)).withRel("usuario"));
    }
}
