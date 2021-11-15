package edu.uci.ics.jiefengw.service.billing.resources;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.client.*;

import com.fasterxml.jackson.core.JsonParseException;
import edu.uci.ics.jiefengw.service.billing.models.*;
import edu.uci.ics.jiefengw.service.billing.logger.*;
import edu.uci.ics.jiefengw.service.billing.configs.*;
import edu.uci.ics.jiefengw.service.billing.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.jersey.jackson.JacksonFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@Path("discount")
public class testPage_discount_create {
    @Path("create")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response forDiscount_create(@Context HttpHeaders headers, String jsonText_create){
        // Set the path of the endpoint we want to communicate with
        IdmConfigs configs = BillingService.getIdmConfigs();
        String servicePath = configs.getScheme() + configs.getHostName() + ":" + configs.getPort() + configs.getPath();//"http://localhost:2942/activity";
        String endpointPath = "/privilege";

        // Declare models
       // ObjectMapper mapper = new ObjectMapper();
        RequestModel_discount_create requestModel = new RequestModel_discount_create();
        RequestModel_privilege requestModel_privilege = new RequestModel_privilege();
        //requestModel = mapper.readValue(jsonText_create, RequestModel_discount_apply.class);
        ResponseModel_discount_create responseModel = null;


            // Get header strings
            // If there is no header with given key, it will be null
            String email = headers.getHeaderString("email");
            String session_id = headers.getHeaderString("session_id");
            String transaction_id = headers.getHeaderString("transaction_id");


            try {
                ObjectMapper mapper = new ObjectMapper();
                requestModel = mapper.readValue(jsonText_create, RequestModel_discount_create.class);
                //gather the email info
                requestModel_privilege.setEmail(requestModel.getEmail());
                requestModel_privilege.setPlevel(2);        //registerd users

                // Create a new Client
                ServiceLogger.LOGGER.info("Building client...");
                Client client = ClientBuilder.newClient();
                client.register(JacksonFeature.class);

                // Create a WebTarget to send a request at
                ServiceLogger.LOGGER.info("Building WebTarget...");
                WebTarget webTarget = client.target(servicePath).path(endpointPath);

                // Create an InvocationBuilder to create the HTTP request
                ServiceLogger.LOGGER.info("Starting invocation builder...");
                Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON)
                        .header("email", email)
                        .header("session_id", session_id)
                        .header("transaction_id", transaction_id);

                // Send the request and save it to a Response
                ServiceLogger.LOGGER.info("Sending request...");
                Response response = invocationBuilder.post(Entity.entity(requestModel_privilege, MediaType.APPLICATION_JSON));
                ServiceLogger.LOGGER.info("Request sent.");

                ServiceLogger.LOGGER.info("Received status " + response.getStatus());



                String jsonText = response.readEntity(String.class);
                responseModel = mapper.readValue(jsonText, ResponseModel_discount_create.class);
                ServiceLogger.LOGGER.info("Successfully mapped response to POJO.");

                //no user exist
                if(responseModel.getResultCode() == 14){
                    //Case 3210:  Unable to create discount code.
                    responseModel = new ResponseModel_discount_create(3210, "Unable to create discount code.");
                    ServiceLogger.LOGGER.severe("Unable to create discount code.");
                    return Response.status(Response.Status.OK).entity(responseModel).build();
                    //Case 141:  User has insufficient privilege level.
                }else if(responseModel.getResultCode() == 141){
                    //Case 3210:  Unable to create discount code.
                    responseModel = new ResponseModel_discount_create(3210, "Unable to create discount code.");
                    ServiceLogger.LOGGER.severe("Unable to create discount code.");
                    return Response.status(Response.Status.OK).entity(responseModel).build();
                }
                //user exist
                else{

                    requestModel = mapper.readValue(jsonText_create, RequestModel_discount_create.class);
                    ServiceLogger.LOGGER.severe("requestModel email: " + requestModel.getEmail());

                    //MySQL
                    try{
                        // Construct the query
                        String query =  "INSERT INTO discount_code (code, discount, sale_start, sale_end, usage_limit)" +
                                " VALUE (?, ?, ?, ?, ?)";

                        // Create the prepared statement
                        PreparedStatement ps = BillingService.getCon().prepareStatement(query);

                        // Set the arguments
                        ps.setString(1, requestModel.getCode());
                        ps.setFloat(2, requestModel.getDiscount());
                        ps.setString(3, requestModel.getSale_start());
                        ps.setString(4, requestModel.getSale_end());
                        ps.setInt(5, requestModel.getLimit());      //might change later

                        // Save the query result to a ResultSet so records may be retrieved
                        ServiceLogger.LOGGER.info("Trying insertion: " + ps.toString());
                        // code = ps_user_status.executeUpdate();
                        // code = ps_privilege_level.executeUpdate();
                        ps.executeUpdate();
                        ServiceLogger.LOGGER.info("Insertion succeeded.");
                        // ServiceLogger.LOGGER.info(query);
                    }catch(Exception e){
                        e.printStackTrace();
                    }

                }
            } catch (IOException e) {
                //ServiceLogger.LOGGER.warning("Unable to map response to POJO.");
                int resultCode;
                e.printStackTrace();

                if (e instanceof JsonParseException) {
                    resultCode = -3;
                    responseModel = new ResponseModel_discount_create(resultCode, "JSON Parse Exception");
                    ServiceLogger.LOGGER.warning("Unable to map JSON to POJO");
                    return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
                } else if (e instanceof JsonMappingException) {

                    resultCode = -2;
                    responseModel = new ResponseModel_discount_create(resultCode, "JSON Mapping Exception");
                    ServiceLogger.LOGGER.info("*****************************" );
                    ServiceLogger.LOGGER.warning("Unable to map JSON to POJO");
                    return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
                } else {
                    resultCode = -1;
                    responseModel = new ResponseModel_discount_create(resultCode, "Internal Server Error");
                    ServiceLogger.LOGGER.severe("Internal Server Error");
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(responseModel).build();
                }
            }

            //Case 3200: Discount code successfully created.
            responseModel = new ResponseModel_discount_create(3200, "Discount code successfully created.");
            ServiceLogger.LOGGER.severe("Discount code successfully created.");
            //return Response.status(Response.Status.OK).entity(responseModel).build();

            // Do work with data contained in response model
            // Return a response with same headers
            Response.ResponseBuilder builder;

            builder = Response.status(Response.Status.OK).entity(responseModel);



            // Pass along headers
            builder.header("email", email);
            builder.header("session_id", session_id);
            builder.header("transaction_id", transaction_id);

            // Return the response
            return builder.build();



    }
}
