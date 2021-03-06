/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.rest.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.rest.api.model.PropertyModel;
import org.wso2.carbon.registry.rest.api.security.RestAPIAuthContext;
import org.wso2.carbon.registry.rest.api.security.RestAPISecurityUtils;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

/**
 * This is to handle the REST verbs GET,POST,DELETE related to property.
 */

@Path("/property")
@Api(value = "/property",
     description = "Rest api for doing operations on a specific single property",
     produces = MediaType.APPLICATION_JSON)
public class Property extends RegistryRestSuper {

    private Log log = LogFactory.getLog(Property.class);

    /**
     * Method retrieves the specified property for the given resource
     *
     * @param resourcePath - Resource path in the registry space
     * @param propertyName - Name of the property to be fetched
     * @return - JSON property model element. HTTP 200 OK.
     */
    @GET
    @Produces("application/json")
    @ApiOperation(value = "Get specific property",
                  httpMethod = "GET",
                  notes = "Fetch details about a specific property",
                  response = PropertyModel.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Found the specific property and returned in body"),
                            @ApiResponse(code = 401, message = "Invalid credentials provided"),
                            @ApiResponse(code = 404, message = "Given specific resource not found"),
                            @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Response getProperty(@QueryParam("path") String resourcePath,
                                @QueryParam("name") String propertyName,
                                @HeaderParam("X-JWT-Assertion") String JWTToken) {
        RestAPIAuthContext authContext = RestAPISecurityUtils.getAuthContext
                (PrivilegedCarbonContext.getThreadLocalCarbonContext(), JWTToken);

        if (!authContext.isAuthorized()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        Response response;
        try {
            Registry registry = getUserRegistry(authContext.getUserName(), authContext.getTenantId());
            if (!registry.resourceExists(resourcePath)) {
                return Response.status(Response.Status.NOT_FOUND).entity(
                        RestAPIConstants.RESOURCE_NOT_FOUND + resourcePath).build();
            }
            Resource resource = registry.get(resourcePath);
            java.util.Properties prop = resource.getProperties();
            if (prop.containsKey(propertyName)) {
                return getSingleProperty(propertyName, prop);
            } else {
                response = Response.status(Response.Status.NOT_FOUND).build();
            }
            return response;
        } catch (RegistryException e) {
            log.error("user is not allowed to read a specified property", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }


    /**
     * This method add array of JSON property model for the specified resource
     *
     * @param resourcePath - resource path
     * @param name         - property name
     * @param value        - property value
     * @return Response    - HTTP 204 No Content.
     */
    @POST
    @Consumes("application/json")
    @ApiOperation(value = "Add a property to a resource",
                  httpMethod = "POST",
                  notes = "Add a property to a resource")
    @ApiResponses(value = { @ApiResponse(code = 204, message = "property added successfully"),
                            @ApiResponse(code = 401, message = "Invalid credentials provided"),
                            @ApiResponse(code = 404, message = "Specified resource not found"),
                            @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Response addProperty(@QueryParam("path") String resourcePath,
                                @QueryParam("name") String name,
                                @QueryParam("value") String value,
                                @HeaderParam("X-JWT-Assertion") String JWTToken) {
        RestAPIAuthContext authContext = RestAPISecurityUtils.getAuthContext
                (PrivilegedCarbonContext.getThreadLocalCarbonContext(), JWTToken);

        if (!authContext.isAuthorized()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        try {
            Registry registry = getUserRegistry(authContext.getUserName(), authContext.getTenantId());
            if (!registry.resourceExists(resourcePath)) {
                return Response.status(Response.Status.NOT_FOUND).entity(
                        RestAPIConstants.RESOURCE_NOT_FOUND + resourcePath).build();
            }
            Resource resource = registry.get(resourcePath);
            resource.setProperty(name, value);
            registry.put(resourcePath, resource);

            if (log.isDebugEnabled()) {
                log.debug("specified property added for the given resource");
            }
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (RegistryException e) {
            log.error("user is not allowed to add properties to a resource", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /**
     * This method delete the specified property of the resource
     *
     * @param resourcePath - path of the resource
     * @param name         - property name to be deleted
     * @return Response    - array of property model. HTTP 204 No Content.
     */
    @DELETE
    @Produces("application/json")
    @ApiOperation(value = "Delete a property",
                  httpMethod = "DELETE",
                  notes = "Delete a property")
    @ApiResponses(value = { @ApiResponse(code = 204, message = "property deleted successfully"),
                            @ApiResponse(code = 401, message = "Invalid credentials provided"),
                            @ApiResponse(code = 404, message = "Specified resource not found"),
                            @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Response deleteProperty(@QueryParam("path") String resourcePath,
                                   @QueryParam("name") String name,
                                   @HeaderParam("X-JWT-Assertion") String JWTToken) {
        RestAPIAuthContext authContext = RestAPISecurityUtils.getAuthContext
                (PrivilegedCarbonContext.getThreadLocalCarbonContext(), JWTToken);

        if (!authContext.isAuthorized()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        try {
            Registry registry = getUserRegistry(authContext.getUserName(), authContext.getTenantId());
            if (!registry.resourceExists(resourcePath)) {
                return Response.status(Response.Status.NOT_FOUND).entity(
                        RestAPIConstants.RESOURCE_NOT_FOUND + resourcePath).build();
            }
            Resource resource = registry.get(resourcePath);
            resource.removeProperty(name);
            registry.put(resourcePath, resource);
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (RegistryException e) {
            log.error("user is not allowed to delete properties on a resource", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /**
     * Method get the specific property
     *
     * @param prop java.util.Properties variable
     * @return hashmap <property>
     */
    private Response getSingleProperty(String propName, Properties prop) {
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        ArrayList<String> propVal = (ArrayList<String>) prop.get(propName);
        String[] values = propVal.toArray(new String[propVal.size()]);
        map.put(propName, values);

        return Response.ok(map).build();
    }

}

