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


@Path("cart")
public class testPage_cart_insert {
    @Path("insert")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response forCart_insert(@Context HttpHeaders headers, String jsonText_request){
        // Set the path of the endpoint we want to communicate with
        IdmConfigs configs = BillingService.getIdmConfigs();
        String servicePath = configs.getScheme() + configs.getHostName() + ":" + configs.getPort() + configs.getPath();//"http://localhost:2942/activity";
        String endpointPath = "/privilege";

        // Declare models
        RequestModel_cart_insert requestModel = new RequestModel_cart_insert();
        RequestModel_privilege requestModel_privilege = new RequestModel_privilege();
        ResponseModel_cart_insert responseModel = null;

        // Get header strings
        // If there is no header with given key, it will be null
        String email = headers.getHeaderString("email");
        String session_id = headers.getHeaderString("session_id");
        String transaction_id = headers.getHeaderString("transaction_id");

        //gather the email info
        requestModel_privilege.setEmail(email);
        requestModel_privilege.setPlevel(4);        //registerd users

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

        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonText = response.readEntity(String.class);
            responseModel = mapper.readValue(jsonText, ResponseModel_cart_insert.class);
            ServiceLogger.LOGGER.info("Successfully mapped response to POJO.");

            //no user exist
            if(responseModel.getResultCode() == 14){
                responseModel = new ResponseModel_cart_insert(14, "User not found.");
                ServiceLogger.LOGGER.severe("User not found.");
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }
            //user exist
            else{

                requestModel = mapper.readValue(jsonText_request, RequestModel_cart_insert.class);
                ServiceLogger.LOGGER.severe("requestModel email: " + requestModel.getEmail());
                //Case 33: Quantity has invalid value.
                if(requestModel.getQuantity() <= 0){
                    responseModel = new ResponseModel_cart_insert(33, "Quantity has invalid value.");
                    ServiceLogger.LOGGER.severe("Quantity has invalid value.");
                    return Response.status(Response.Status.OK).entity(responseModel).build();
                }

                //try insert into database
                try {
                    //get the data from database
                    ArrayList<String> purchasedIDList = new ArrayList<String>();
                    String base_query = "SELECT DISTINCT cart.* " + //, genre.name AS 'genre'
                            "FROM cart " +
                            "WHERE cart.email = ? and cart.quantity = ? ";
                    // Create the prepared statement
                    PreparedStatement base_ps = BillingService.getCon().prepareStatement(base_query);

                    base_ps.setString(1, requestModel.getEmail());
                    base_ps.setInt(2, requestModel.getQuantity());

                    ServiceLogger.LOGGER.info("Trying selection: " + base_ps.toString());
                    ResultSet rs = base_ps.executeQuery();
                    ServiceLogger.LOGGER.info("selection succeeded.");

                    while (rs.next()) {
                        purchasedIDList.add(rs.getString("movie_id"));
                    }

                    //Case 311: Duplicate insertion.
                    if(purchasedIDList.contains(requestModel.getMovie_id())){
                        responseModel = new ResponseModel_cart_insert(311, "Duplicate insertion.");
                        ServiceLogger.LOGGER.severe("Duplicate insertion.");
                        return Response.status(Response.Status.OK).entity(responseModel).build();
                    }

                    //TODO: need to change this part when there is invalid movie_id
                    //get the data from database
                    ArrayList<String> movieIDList = new ArrayList<String>();
                    String movieid_query = "SELECT DISTINCT movie_price.* " +
                            "FROM movie_price ";
                    // Create the prepared statement
                    PreparedStatement movie_ps = BillingService.getCon().prepareStatement(movieid_query);

                    //movie_ps.setString(1, requestModel.getEmail());
                    //movie_ps.setInt(2, requestModel.getQuantity());

                    ServiceLogger.LOGGER.info("Trying selection: " + movie_ps.toString());
                    ResultSet movie_rs = movie_ps.executeQuery();
                    ServiceLogger.LOGGER.info("selection succeeded.");

                    while (movie_rs.next()) {
                        movieIDList.add(movie_rs.getString("movie_id"));
                    }
                    //Case 3150: Shopping cart operation failed.
                    if (!movieIDList.contains(requestModel.getMovie_id())){
                        responseModel = new ResponseModel_cart_insert(3150, "Shopping cart operation failed.");
                        ServiceLogger.LOGGER.severe("Shopping cart operation failed.");
                        return Response.status(Response.Status.OK).entity(responseModel).build();
                    }

                    // Construct the query
                    String query =  "INSERT INTO cart (email, movie_id, quantity)" +
                            " VALUE (?, ?, ?)";

                    // Create the prepared statement
                    PreparedStatement ps = BillingService.getCon().prepareStatement(query);

                    // Set the arguments
                    ps.setString(1, requestModel.getEmail());
                    ps.setString(2, requestModel.getMovie_id());
                    ps.setInt(3, requestModel.getQuantity());      //might change later

                    // Save the query result to a ResultSet so records may be retrieved
                    ServiceLogger.LOGGER.info("Trying insertion: " + ps.toString());
                    // code = ps_user_status.executeUpdate();
                    // code = ps_privilege_level.executeUpdate();
                    ps.executeUpdate();
                    ServiceLogger.LOGGER.info("Insertion succeeded.");
                   // ServiceLogger.LOGGER.info(query);




                } catch (SQLException e) {
                    ServiceLogger.LOGGER.warning("Insertion failed.");
                    e.printStackTrace();
                    responseModel = new ResponseModel_cart_insert(3150, "Shopping cart operation failed.");
                    ServiceLogger.LOGGER.severe("Shopping cart operation failed.");
                    return Response.status(Response.Status.OK).entity(responseModel).build();
                }





            }
        } catch (IOException e) {
            //ServiceLogger.LOGGER.warning("Unable to map response to POJO.");
            int resultCode;
            e.printStackTrace();

            if (e instanceof JsonParseException) {
                resultCode = -3;
                responseModel = new ResponseModel_cart_insert(resultCode, "JSON Parse Exception");
                ServiceLogger.LOGGER.warning("Unable to map JSON to POJO");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            } else if (e instanceof JsonMappingException) {

                resultCode = -2;
                responseModel = new ResponseModel_cart_insert(resultCode, "JSON Mapping Exception");
                ServiceLogger.LOGGER.info("*****************************" );
                ServiceLogger.LOGGER.warning("Unable to map JSON to POJO");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            } else {
                resultCode = -1;
                responseModel = new ResponseModel_cart_insert(resultCode, "Internal Server Error");
                ServiceLogger.LOGGER.severe("Internal Server Error");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(responseModel).build();
            }
        }

        //Case 3100: Shopping cart item inserted successfully.
        responseModel = new ResponseModel_cart_insert(3100, "Shopping cart item inserted successfully.");
        ServiceLogger.LOGGER.severe("Shopping cart item inserted successfully.");
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
