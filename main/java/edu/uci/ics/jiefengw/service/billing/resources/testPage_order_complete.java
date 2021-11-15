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

import static edu.uci.ics.jiefengw.service.billing.models.PayPalOrderClient.approve_url;
import static edu.uci.ics.jiefengw.service.billing.models.PayPalOrderClient.capture_id;

@Path("order")
public class testPage_order_complete {
    @Path("complete")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response forCart_insert(@Context HttpHeaders headers,
                                   @QueryParam("token") String token,
                                   @QueryParam("payer_id") String payer_id){

        // Declare models
        //RequestModel_order_complete requestModel; //= new RequestModel_cart_update();
        ResponseModel_order_complete responseModel = new ResponseModel_order_complete(0, "");


        // Get header strings
        // If there is no header with given key, it will be null
        String email = headers.getHeaderString("email");
        String session_id = headers.getHeaderString("session_id");
        String transaction_id = headers.getHeaderString("transaction_id");

        //Todo: send capture request to paypal, get capture id
        //paypal
        String orderid = PayPalOrderClient.order_id;
        ServiceLogger.LOGGER.severe("orderid: " + orderid);
        PayPalOrderClient ppOrderclient = new PayPalOrderClient();
        System.out.println("---------- Creating order -----------");
       // orderid = ppOrderclient.createPayPalOrder(ppOrderclient, true, sum);


        // uncomment capturing part after you approve the payment
        // orderid = "8M024637NR407413J";
        try {
            // System.out.println("---------- Capturing order -----------");
            //ppOrderclient.captureOrder(orderid, ppOrderclient);
            System.out.println("---------- Getting order details-----------");
            ppOrderclient.captureOrder(orderid,ppOrderclient);
            ppOrderclient.getOrder(orderid, ppOrderclient);
            ServiceLogger.LOGGER.severe("capture id: " + capture_id);
        } catch (Exception  e) {
            e.printStackTrace();
            //Case 3422: Order can not be completed.
            responseModel = new ResponseModel_order_complete(3422, "Order can not be completed.");
            ServiceLogger.LOGGER.severe("Order can not be completed.");
            return Response.status(Response.Status.OK).entity(responseModel).build();
        }
        //Todo: check token
        try {
            //get the data from database
            ArrayList<String> tokenList = new ArrayList<String>();
            String token_query = "SELECT DISTINCT transaction.* " + //, genre.name AS 'genre'
                    "FROM transaction " +
                    "WHERE transaction.token = ? ";
            // Create the prepared statement
            PreparedStatement token_ps = BillingService.getCon().prepareStatement(token_query);

            token_ps.setString(1,token);
            // base_ps.setInt(2, requestModel.getQuantity());

            ServiceLogger.LOGGER.info("Trying selection: " + token_ps.toString());
            ResultSet token_rs = token_ps.executeQuery();
            ServiceLogger.LOGGER.info("selection succeeded.");

            while (token_rs.next()) {
                tokenList.add(token_rs.getString("token"));
            }

            if(tokenList.isEmpty()){
                //Case 3421: Token not found.
                responseModel = new ResponseModel_order_complete(3421, "Token not found.");
                ServiceLogger.LOGGER.severe("Token not found.");
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }else{
                //Todo: update transaction
                String update_query = "UPDATE transaction " + //, genre.name AS 'genre'
                        "SET capture_id = ? " +
                        "WHERE transaction.token = ? ";
                // Create the prepared statement
                PreparedStatement update_ps = BillingService.getCon().prepareStatement(update_query);

                update_ps.setString(1,capture_id);
                update_ps.setString(2,token);

                ServiceLogger.LOGGER.info("Trying selection: " + update_ps.toString());
                //ResultSet update_rs = update_ps.executeQuery();
                update_ps.executeUpdate();
                ServiceLogger.LOGGER.info("selection succeeded.");

                //Todo: clear cart
                // Construct the query
                /*
                    DELETE FROM cart
                    WHERE cart.email = (
                    SELECT sale.email from sale
                    LEFT JOIN transaction on transaction.sale_id = sale.sale_id
                    WHERE token = '74K08344Y6272134F'
                    )
                 */
                String query =  "DELETE FROM cart " +
                        "WHERE cart.email = ( " +
                        "    SELECT sale.email from sale " +
                        "    LEFT JOIN transaction on transaction.sale_id = sale.sale_id " +
                        "    WHERE token = ? " +
                        "    )";

                // Create the prepared statement
                PreparedStatement ps = BillingService.getCon().prepareStatement(query);

                // Set the arguments
                ps.setString(1, token);

                // Save the query result to a ResultSet so records may be retrieved
                ServiceLogger.LOGGER.info("Trying delete: " + ps.toString());
                // code = ps_user_status.executeUpdate();
                // code = ps_privilege_level.executeUpdate();
                ps.executeUpdate();
                ServiceLogger.LOGGER.info("delete succeeded.");
                // ServiceLogger.LOGGER.info(query);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }


        //Case 3420: Order is completed successfully.
        responseModel = new ResponseModel_order_complete(3420, "Order is completed successfully.");
        ServiceLogger.LOGGER.severe("Order is completed successfully.");

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
