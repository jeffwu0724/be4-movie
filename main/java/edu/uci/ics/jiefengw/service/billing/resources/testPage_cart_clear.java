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
public class testPage_cart_clear {
    @Path("clear")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response forCart_clear(@Context HttpHeaders headers, String jsonText_clear){
        // Declare models
        RequestModel_cart_clear requestModel; //= new RequestModel_cart_update();
        ResponseModel_cart_clear responseModel = null;
        ObjectMapper mapper = new ObjectMapper();

        // Get header strings
        // If there is no header with given key, it will be null
        String email = headers.getHeaderString("email");
        String session_id = headers.getHeaderString("session_id");
        String transaction_id = headers.getHeaderString("transaction_id");



        try {
            //requestModel = mapper.readValue(jsonText, RequestModel_register.class);
            requestModel = mapper.readValue(jsonText_clear, RequestModel_cart_clear.class);
            ServiceLogger.LOGGER.severe("requestModel email: " + requestModel.getEmail());
            ServiceLogger.LOGGER.info("Successfully mapped response to POJO.");



            //try insert into database
            try {
                //get the data from database
                ArrayList<String> purchasedIDList = new ArrayList<String>();
                String base_query = "SELECT DISTINCT cart.* " + //, genre.name AS 'genre'
                        "FROM cart " +
                        "WHERE cart.email = ? ";
                // Create the prepared statement
                PreparedStatement base_ps = BillingService.getCon().prepareStatement(base_query);

                base_ps.setString(1, requestModel.getEmail());
                // base_ps.setInt(2, requestModel.getQuantity());

                ServiceLogger.LOGGER.info("Trying selection: " + base_ps.toString());
                ResultSet rs = base_ps.executeQuery();
                ServiceLogger.LOGGER.info("selection succeeded.");

                while (rs.next()) {
                    purchasedIDList.add(rs.getString("movie_id"));
                }
                ServiceLogger.LOGGER.severe(String.valueOf(purchasedIDList.size()));
                //Case 312: Shopping cart item does not exist.
                if(purchasedIDList.isEmpty()){
                    responseModel = new ResponseModel_cart_clear(312, "Shopping cart item does not exist.");
                    ServiceLogger.LOGGER.severe("Shopping cart item does not exist.");
                    Response.ResponseBuilder builder;
                    builder = Response.status(Response.Status.OK).entity(responseModel);

                    // Pass along headers
                    builder.header("email", email);
                    builder.header("session_id", session_id);
                    builder.header("transaction_id", transaction_id);

                    // Return the response
                    return builder.build();
                    //return Response.status(Response.Status.OK).entity(responseModel).build();
                }



                // Construct the query
                String query =  "DELETE FROM cart " +
                        "WHERE cart.email = ?";

                // Create the prepared statement
                PreparedStatement ps = BillingService.getCon().prepareStatement(query);

                // Set the arguments
                ps.setString(1, requestModel.getEmail());

                // Save the query result to a ResultSet so records may be retrieved
                ServiceLogger.LOGGER.info("Trying delete: " + ps.toString());
                // code = ps_user_status.executeUpdate();
                // code = ps_privilege_level.executeUpdate();
                ps.executeUpdate();
                ServiceLogger.LOGGER.info("delete succeeded.");
                // ServiceLogger.LOGGER.info(query);

            } catch (SQLException e) {
                ServiceLogger.LOGGER.warning("delete failed.");
                e.printStackTrace();
                responseModel = new ResponseModel_cart_clear(3150, "Shopping cart operation failed.");
                ServiceLogger.LOGGER.severe("Shopping cart operation failed.");
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }
        } catch (IOException e) {
            //ServiceLogger.LOGGER.warning("Unable to map response to POJO.");
            int resultCode;
            e.printStackTrace();

            if (e instanceof JsonParseException) {
                resultCode = -3;
                responseModel = new ResponseModel_cart_clear(resultCode, "JSON Parse Exception");
                ServiceLogger.LOGGER.warning("Unable to map JSON to POJO");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            } else if (e instanceof JsonMappingException) {

                resultCode = -2;
                responseModel = new ResponseModel_cart_clear(resultCode, "JSON Mapping Exception");
                ServiceLogger.LOGGER.info("*****************************" );
                ServiceLogger.LOGGER.warning("Unable to map JSON to POJO");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            } else {
                resultCode = -1;
                responseModel = new ResponseModel_cart_clear(resultCode, "Internal Server Error");
                ServiceLogger.LOGGER.severe("Internal Server Error");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(responseModel).build();
            }
        }

        //Case 3120: Shopping cart item deleted successfully.
        responseModel = new ResponseModel_cart_clear(3120, "Shopping cart item deleted successfully.");
        ServiceLogger.LOGGER.severe("Shopping cart item deleted successfully.");
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
