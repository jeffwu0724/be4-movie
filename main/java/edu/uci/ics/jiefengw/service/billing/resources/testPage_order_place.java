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
import java.util.List;

import com.braintreepayments.http.serializer.Json;
import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import com.braintreepayments.http.HttpResponse;
import com.braintreepayments.http.exceptions.HttpException;
import com.paypal.orders.*;

import static edu.uci.ics.jiefengw.service.billing.models.PayPalOrderClient.approve_url;
import static edu.uci.ics.jiefengw.service.billing.models.PayPalOrderClient.capture_id;

@Path("order")
public class testPage_order_place {
    @Path("place")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response forOrder_place(@Context HttpHeaders headers, String jsonText_place) {
        // Declare models
        RequestModel_order_place requestModel; //= new RequestModel_cart_update();
        ResponseModel_order_place responseModel = new ResponseModel_order_place(0, "", "", "");

        double sum = 0;

        // Get header strings
        // If there is no header with given key, it will be null
        String email = headers.getHeaderString("email");
        String session_id = headers.getHeaderString("session_id");
        String transaction_id = headers.getHeaderString("transaction_id");



        try {
            ObjectMapper mapper = new ObjectMapper();
            requestModel = mapper.readValue(jsonText_place, RequestModel_order_place.class);
            ServiceLogger.LOGGER.info("request Model email: " + requestModel.getEmail());
            ServiceLogger.LOGGER.info("Successfully mapped response to POJO.");

            //Todo: insert into sale table
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

                //Case 312: Shopping cart item does not exist.
                if(purchasedIDList.isEmpty()){
                    //Todo: may need to change the response model
                    responseModel = new ResponseModel_order_place(312, "Shopping cart item does not exist.", null, null);
                    ServiceLogger.LOGGER.severe("Shopping cart item does not exist.");
                    return Response.status(Response.Status.OK).entity(responseModel).build();

                }else{

                    //Todo: delete test later
                    /*
                    //test
                    responseModel = new ResponseModel_order_place(1000, "test.", responseModel.getApprove_url(), responseModel.getToken());
                    ServiceLogger.LOGGER.severe("test");
                    return Response.status(Response.Status.OK).entity(responseModel).build();
                     */
                    //Todo: insert info into table sale
                    // Construct the query
                    String sale_query =  "INSERT INTO sale (email, movie_id, quantity, sale_date) " +
                            "SELECT ?, cart.movie_id, quantity, NOW() FROM cart " +
                            "WHERE email = ?";

                    // Create the prepared statement
                    PreparedStatement sale_ps = BillingService.getCon().prepareStatement(sale_query);

                    // Set the arguments
                    sale_ps.setString(1, requestModel.getEmail());
                    sale_ps.setString(2, requestModel.getEmail());

                    // Save the query result to a ResultSet so records may be retrieved
                    ServiceLogger.LOGGER.info("Trying insertion: " + sale_ps.toString());
                    // code = ps_user_status.executeUpdate();
                    // code = ps_privilege_level.executeUpdate();
                    sale_ps.executeUpdate();
                    ServiceLogger.LOGGER.info("Insertion succeeded.");
                    // ServiceLogger.LOGGER.info(query);

                    //Todo:get the sum and send to paypal
//                    String get_sum_query =  "SELECT SUM(unit_price*quantity*(1-discount)) FROM sale " +
//                            "JOIN movie_price ON sale.movie_id = movie_price.movie_id " +
//                            "WHERE email = ?";
                    String get_sum_query =  "SELECT SUM(unit_price*quantity*(1-discount)) FROM cart " +
                            "JOIN movie_price ON cart.movie_id = movie_price.movie_id " +
                            "WHERE email = ?";

                    // Create the prepared statement
                    PreparedStatement get_sum_ps = BillingService.getCon().prepareStatement(get_sum_query);

                    // Set the arguments
                    get_sum_ps.setString(1, requestModel.getEmail());
                    //get_sum_ps.setString(2, requestModel.getEmail());

                    // Save the query result to a ResultSet so records may be retrieved
                    ServiceLogger.LOGGER.info("Trying selection: " + get_sum_ps.toString());
                    // code = ps_user_status.executeUpdate();
                    // code = ps_privilege_level.executeUpdate();

                    ResultSet  get_sum_rs = get_sum_ps.executeQuery();
                    ServiceLogger.LOGGER.info("selection succeeded.");

                    while ( get_sum_rs.next()) {
                       sum = get_sum_rs.getDouble("SUM(unit_price*quantity*(1-discount))");
                       sum= (double) Math.round(sum * 100) / 100;
                       ServiceLogger.LOGGER.info("sum is: " + sum);
                    }
                    ServiceLogger.LOGGER.info("sum is: " + sum);

                    //Todo: send request to paypal
                    //paypal
                    String orderid;
                    PayPalOrderClient ppOrderclient = new PayPalOrderClient();
                    System.out.println("---------- Creating order -----------");
                    orderid = ppOrderclient.createPayPalOrder(ppOrderclient, true, sum);
                    //set orderID
                    responseModel.setToken( orderid);
                    //set approveURL
                    responseModel.setApprove_url(approve_url);


                    // uncomment capturing part after you approve the payment
                    // orderid = "8M024637NR407413J";
                    try {
                        // System.out.println("---------- Capturing order -----------");
                        // captureOrder(orderid, ppOrderclient);
                        System.out.println("---------- Getting order details-----------");
                        //ppOrderclient.captureOrder(orderid,ppOrderclient);
                        ppOrderclient.getOrder(orderid, ppOrderclient);

                    } catch (Exception  e) {
                        e.printStackTrace();
                        //Case 342: Order creation failed.
                        responseModel = new ResponseModel_order_place(342, "Order creation failed.", null, null);
                        ServiceLogger.LOGGER.severe("Order creation failed.");
                        return Response.status(Response.Status.OK).entity(responseModel).build();
                    }

                    //Todo: add order_id and sale_id to table transaction
                    // Construct the query
                    String transaction_query =  "INSERT INTO transaction (sale_id, token, capture_id) " +
                            " VALUE((SELECT sale_id from sale  WHERE email = ? ),?,?)";

                    // Create the prepared statement
                    PreparedStatement transaction_ps = BillingService.getCon().prepareStatement(transaction_query);

                    // Set the arguments
                    transaction_ps.setString(1, requestModel.getEmail());
                   //transaction_ps.setString(2, requestModel.getMovie_id());
                    transaction_ps.setString(2, responseModel.getToken());
                    transaction_ps.setString(3, capture_id);

                    // Save the query result to a ResultSet so records may be retrieved
                    ServiceLogger.LOGGER.info("Trying insertion: " + transaction_ps.toString());
                    // code = ps_user_status.executeUpdate();
                    // code = ps_privilege_level.executeUpdate();
                    transaction_ps.executeUpdate();
                    ServiceLogger.LOGGER.info("Insertion succeeded.");
                    // ServiceLogger.LOGGER.info(query);


                }
            } catch (SQLException e) {
                ServiceLogger.LOGGER.warning("Insertion failed.");
                e.printStackTrace();
            }



            //Todo: get the token, and add infomation to transcation
        } catch (IOException e) {
            //ServiceLogger.LOGGER.warning("Unable to map response to POJO.");
            int resultCode;
            e.printStackTrace();

            if (e instanceof JsonParseException) {
                resultCode = -3;
                responseModel = new ResponseModel_order_place(resultCode, "JSON Parse Exception", null, null);
                ServiceLogger.LOGGER.warning("Unable to map JSON to POJO");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            } else if (e instanceof JsonMappingException) {

                resultCode = -2;
                responseModel = new ResponseModel_order_place(resultCode, "JSON Mapping Exception", null, null);
                ServiceLogger.LOGGER.info("*****************************");
                ServiceLogger.LOGGER.warning("Unable to map JSON to POJO");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            } else {
                resultCode = -1;
                responseModel = new ResponseModel_order_place(resultCode, "Internal Server Error", null, null);
                ServiceLogger.LOGGER.severe("Internal Server Error");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(responseModel).build();
            }
        }

        //Case 3400: Order placed successfully.
        responseModel = new ResponseModel_order_place(3400, "Order placed successfully.", responseModel.getApprove_url(), responseModel.getToken() );
        ServiceLogger.LOGGER.severe("Order placed successfully.");
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



