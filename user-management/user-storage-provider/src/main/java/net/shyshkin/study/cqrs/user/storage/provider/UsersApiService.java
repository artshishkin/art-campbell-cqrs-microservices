package net.shyshkin.study.cqrs.user.storage.provider;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/api/v1/users/provider")
@Consumes(MediaType.APPLICATION_JSON)
public interface UsersApiService {

    @GET
    @Path("/username/{username}")
    User getUserDetailsByUsername(@PathParam("username") String username);

    @GET
    @Path("/email/{email}")
    User getUserDetailsByEmail(@PathParam("email") String email);

    @POST
    @Path("/username/{username}/verify-password")
    @Produces(MediaType.APPLICATION_JSON)
    VerificationPasswordResponse verifyUserPasswordByUsername(
            @PathParam("username") String username,
            String password);

    @POST
    @Path("/email/{email}/verify-password")
    @Produces(MediaType.APPLICATION_JSON)
    VerificationPasswordResponse verifyUserPasswordByEmail(
            @PathParam("email") String email,
            String password);
}
