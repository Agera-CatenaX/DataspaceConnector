/*
 * Copyright 2020 Fraunhofer Institute for Software and Systems Engineering
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dataspaceconnector.controller.resource.type;

import io.dataspaceconnector.config.BasePath;
import io.dataspaceconnector.controller.resource.base.BaseResourceController;
import io.dataspaceconnector.controller.resource.base.tag.ResourceDescription;
import io.dataspaceconnector.controller.resource.base.tag.ResourceName;
import io.dataspaceconnector.controller.resource.view.route.RouteView;
import io.dataspaceconnector.controller.util.ResponseCode;
import io.dataspaceconnector.controller.util.ResponseDescription;
import io.dataspaceconnector.model.route.Route;
import io.dataspaceconnector.model.route.RouteDesc;
import io.dataspaceconnector.service.resource.type.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.UUID;

/**
 * Offers the endpoints for managing routes.
 */
@RestController
@RequestMapping(BasePath.ROUTES)
@Tag(name = ResourceName.ROUTES, description = ResourceDescription.ROUTES)
@RequiredArgsConstructor
public class RouteController extends BaseResourceController<Route, RouteDesc, RouteView,
        RouteService> {

    /**
     * @param routeId    The id of the route.
     * @param endpointId The id of the endpoint.
     * @return response status OK, if start endpoint is created.
     */
    @PutMapping("{id}/endpoint/start")
    @Operation(summary = "Creates start endpoint for the route")
    @ApiResponses(value = {
            @ApiResponse(responseCode = ResponseCode.NO_CONTENT,
                    description = ResponseDescription.NO_CONTENT),
            @ApiResponse(responseCode = ResponseCode.UNAUTHORIZED,
                    description = ResponseDescription.UNAUTHORIZED)})
    public ResponseEntity<String> createStartEndpoint(
            @Valid @PathVariable(name = "id") final UUID routeId,
            @RequestBody final UUID endpointId) {
        getService().setStartEndpoint(routeId, endpointId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * @param routeId The id of the route.
     * @return response status OK, if start endpoint is deleted.
     */
    @DeleteMapping("{id}/endpoint/start")
    @Operation(summary = "Deletes the start endpoint of the route")
    @ApiResponses(value = {
            @ApiResponse(responseCode = ResponseCode.NO_CONTENT,
                    description = ResponseDescription.NO_CONTENT),
            @ApiResponse(responseCode = ResponseCode.UNAUTHORIZED,
                    description = ResponseDescription.UNAUTHORIZED)})
    public ResponseEntity<String> deleteStartEndpoint(
            @Valid @PathVariable(name = "id") final UUID routeId) {
        getService().removeStartEndpoint(routeId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * @param routeId    The id of the route.
     * @param endpointId The id of the endpoint.
     * @return response status OK, if last endpoint is created.
     */
    @PutMapping("{id}/endpoint/end")
    @Operation(summary = "Creates last endpoint for the route")
    @ApiResponses(value = {
            @ApiResponse(responseCode = ResponseCode.NO_CONTENT,
                    description = ResponseDescription.NO_CONTENT),
            @ApiResponse(responseCode = ResponseCode.UNAUTHORIZED,
                    description = ResponseDescription.UNAUTHORIZED)})
    public ResponseEntity<String> createLastEndpoint(
            @Valid @PathVariable(name = "id") final UUID routeId,
            @RequestBody final UUID endpointId) {
        getService().setLastEndpoint(routeId, endpointId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * @param routeId The id of the route.
     * @return response status OK, if last endpoint is deleted.
     */
    @DeleteMapping("{id}/endpoint/end")
    @Operation(summary = "Deletes the start endpoint of the route")
    @ApiResponses(value = {
            @ApiResponse(responseCode = ResponseCode.NO_CONTENT,
                    description = ResponseDescription.NO_CONTENT),
            @ApiResponse(responseCode = ResponseCode.UNAUTHORIZED,
                    description = ResponseDescription.UNAUTHORIZED)})
    public ResponseEntity<String> deleteLastEndpoint(
            @Valid @PathVariable(name = "id") final UUID routeId) {
        getService().removeLastEndpoint(routeId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
