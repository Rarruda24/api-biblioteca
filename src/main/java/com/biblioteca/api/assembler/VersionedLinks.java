package com.biblioteca.api.assembler;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.hateoas.Link;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

final class VersionedLinks {
    private VersionedLinks() { }

    static Link forCurrentRequest(String resource, Long id, String relation) {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) return null;

        HttpServletRequest request = attributes.getRequest();
        String path = request.getRequestURI().substring(request.getContextPath().length());
        String versionPrefix;
        if (path.startsWith("/api/v1/" + resource)) versionPrefix = "/api/v1";
        else if (path.startsWith("/api/v2/" + resource)) versionPrefix = "/api/v2";
        else return null;

        String href = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(versionPrefix + "/{resource}/{id}")
                .buildAndExpand(resource, id)
                .toUriString();
        return Link.of(href).withRel(relation);
    }
}
