package org.backendbrilliance.uiservice.controller;

import org.backendbrilliance.uiservice.dtos.EndpointRequest;
import org.backendbrilliance.uiservice.dtos.EndpointResponse;
import org.backendbrilliance.uiservice.service.EndpointService;
import org.backendbrilliance.uiservice.service.helper.MapperForEntityToDTO;
import org.backendbrilliance.uiservice.service.security.AuthenticatedUser;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/endpoints", produces = MediaType.APPLICATION_JSON_VALUE)
public class EndpointController {

    private final EndpointService endpointService;
    private final AuthenticatedUser authenticatedUser;
    private final MapperForEntityToDTO mapper;

    public EndpointController(
            EndpointService endpointService,
            AuthenticatedUser authenticatedUser, MapperForEntityToDTO mapper) {
        this.endpointService = endpointService;
        this.authenticatedUser = authenticatedUser;
        this.mapper = mapper;
    }

    @GetMapping
    public List<EndpointResponse> list() {
        var user = authenticatedUser.get().orElseThrow();
        return endpointService.getEndpointsForUser(user.getId())
                .stream().map(mapper::endpointEntityToDTO).toList();
    }

    @PostMapping
    public ResponseEntity<EndpointResponse> create(@RequestBody EndpointRequest req) {
        var user = authenticatedUser.get().orElseThrow();
        var endpoint = endpointService.createEndpoint(req.label(), user.getId());
        return ResponseEntity.status(201).body(mapper.endpointEntityToDTO(endpoint));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        var user = authenticatedUser.get().orElseThrow();
        endpointService.deleteEndpoint(id);
        return ResponseEntity.noContent().build();
    }
}
