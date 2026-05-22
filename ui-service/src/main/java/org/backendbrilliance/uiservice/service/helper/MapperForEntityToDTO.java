package org.backendbrilliance.uiservice.service.helper;

import org.backendbrilliance.uiservice.dtos.EndpointResponse;
import org.backendbrilliance.uiservice.dtos.UserResponse;
import org.backendbrilliance.uiservice.dtos.WebhookRequestResponse;
import org.backendbrilliance.uiservice.entity.Endpoint;
import org.backendbrilliance.uiservice.entity.User;
import org.backendbrilliance.uiservice.entity.WebhookRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MapperForEntityToDTO {

    @Value("${capture.baseurl:http://localhost:8080}")
    private String captureBaseUrl;

    //WebHookRequest to WebhookRequestResponse
    public WebhookRequestResponse webhookEntityToDTO(WebhookRequest request) {
        return new WebhookRequestResponse(
                request.getId().toString(),
                request.getMethod(),
                request.getSourceIp(),
                request.getContentType(),
                request.getBodySize(),
                request.getHeaders(),
                request.getBody(),
                request.getQueryParams(),
                request.getReceivedAt()
        );
    }

    //Endpoint to EndpointResponse
    public EndpointResponse endpointEntityToDTO(Endpoint request) {
        return new EndpointResponse(
                request.getId().toString(),
                request.getSlug(),
                request.getLabel(),
                captureBaseUrl + "/h/" + request.getSlug(),
                request.getCreatedAt(),
                request.getExpiresAt()
        );
    }

    //User to UserResponse
    public UserResponse userEntityToDTO(User user) {
        return new UserResponse(
                user.getId().toString(),
                user.getEmail(),
                user.getTier().toString(),
                user.getRole()
        );
    }
}
