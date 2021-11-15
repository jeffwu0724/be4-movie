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

@Path("order")
public class testPage_order_retrieve {
    @Path("retrieve")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response forOrder_place(@Context HttpHeaders headers, String jsonText_retrieve) {
        // Declare models
        ArrayList<transactionModel> empty = new ArrayList<transactionModel>();
        RequestModel_order_retrieve requestModel; //= new RequestModel_cart_update();
        ResponseModel_order_retrieve responseModel = new ResponseModel_order_retrieve(0, "",empty);
        ArrayList<transactionModel> output = new ArrayList<transactionModel>();

        amountModel amount = new amountModel();
        transaction_feeModel fee = new transaction_feeModel();
        transactionModel  transaction = new transactionModel();
        order_itemModel item = new order_itemModel();

        // Get header strings
        // If there is no header with given key, it will be null
        String email = headers.getHeaderString("email");
        String session_id = headers.getHeaderString("session_id");
        String transaction_id = headers.getHeaderString("transaction_id");



        try {
            ObjectMapper mapper = new ObjectMapper();
            requestModel = mapper.readValue(jsonText_retrieve, RequestModel_order_retrieve.class);
            ServiceLogger.LOGGER.info("request Model email: " + requestModel.getEmail());
            ServiceLogger.LOGGER.info("Successfully mapped response to POJO.");

            //set the value
            /*



          ITEMS (itemModel[], optional)
               EMAIL (string, required)
               MOVIE_ID (string, required)
               QUANTITY (int, required)
               UNIT_PRICE (float, required)
               DISCOUNT (float, required)
               SALE_DATE (string, required)

             */
            ServiceLogger.LOGGER.info(PayPalOrderClient.capture_id);
            ServiceLogger.LOGGER.info(PayPalOrderClient.state);
            transaction.setCapture_id(PayPalOrderClient.capture_id);
            transaction.setState(PayPalOrderClient.state);

            amount.setTotal(PayPalOrderClient.total);
            amount.setCurrency(PayPalOrderClient.currency);
            transaction.setAmount(amount);

            fee.setValue(PayPalOrderClient.value);
            fee.setCurrency(PayPalOrderClient.currency);
            transaction.setTransaction_fee(fee);

            transaction.setCreate_time(PayPalOrderClient.create_time);
            transaction.setUpdate_time(PayPalOrderClient.update_time);

            try{

                //get the movie_id that this users had in cart from database
                ArrayList<String> purchasedIDList = new ArrayList<String>();
                String base_query = "SELECT DISTINCT transaction.* " + //, genre.name AS 'genre'
                        "FROM transaction " +
                        "WHERE transaction.token = ?"; // and cart.quantity = ?
                // Create the prepared statement
                PreparedStatement base_ps = BillingService.getCon().prepareStatement(base_query);

                ServiceLogger.LOGGER.info(PayPalOrderClient.order_id);
                base_ps.setString(1,PayPalOrderClient.order_id );
                // base_ps.setInt(2, requestModel.getQuantity());

                ServiceLogger.LOGGER.info("Trying selection: " + base_ps.toString());
                ResultSet rs = base_ps.executeQuery();
                ServiceLogger.LOGGER.info("selection succeeded.");

                while (rs.next()) {
                    purchasedIDList.add(rs.getString("token"));
                }

                ArrayList<order_itemModel> templist = new ArrayList<order_itemModel>();
                ServiceLogger.LOGGER.info("pursechased ID list size is: " + purchasedIDList.size());
                for(int i = 0; i < purchasedIDList.size(); i++){
                    order_itemModel newItem = new order_itemModel();
                    //try to get discount, unit_price and etc
                    //mySQL

                    String item_query = "SELECT DISTINCT sale.email, sale.quantity, movie_price.*, sale.sale_date, transaction.token " +
                            "FROM movie_price " +
                            "LEFT OUTER JOIN sale ON sale.movie_id = movie_price.movie_id " +
                            "LEFT OUTER JOIN transaction ON sale.sale_id = transaction.sale_id " +
                            "WHERE transaction.token = ?";
                    // Create the prepared statement
                    PreparedStatement item_ps = BillingService.getCon().prepareStatement(item_query);

                    item_ps.setString(1, purchasedIDList.get(i));
                    //movie_ps.setInt(2, requestModel.getQuantity());

                    ServiceLogger.LOGGER.info("Trying selection: " + item_ps.toString());
                    ResultSet item_rs = item_ps.executeQuery();
                    ServiceLogger.LOGGER.info("selection succeeded.");

                    while (item_rs.next()) {
                        newItem.setMovie_id(item_rs.getString("movie_id"));
                        newItem.setDiscount(item_rs.getFloat("discount"));
                        newItem.setUnit_price(item_rs.getFloat("unit_price"));
                        newItem.setEmail(item_rs.getString("email"));
                        newItem.setQuantity(item_rs.getInt("quantity"));
                        newItem.setSale_date(item_rs.getString("sale_date"));
                    }
                    ServiceLogger.LOGGER.info(newItem.getMovie_id());

                    ServiceLogger.LOGGER.info(newItem.getEmail());
                    ServiceLogger.LOGGER.info(String.valueOf(newItem.getDiscount()));

                    templist.add(newItem);
                    ServiceLogger.LOGGER.info(String.valueOf(templist.size()));
                    try{
                        ServiceLogger.LOGGER.info(String.valueOf(templist.isEmpty()));
                        transaction.setItems(templist);
                        output.add(transaction);
                        responseModel.setTransactions(output);

                    }catch(Exception e){
                        e.printStackTrace();
                    }

                }
            }catch(Exception e){
                e.printStackTrace();
            }


        } catch (IOException e) {
            //ServiceLogger.LOGGER.warning("Unable to map response to POJO.");
            int resultCode;
            e.printStackTrace();

            if (e instanceof JsonParseException) {
                resultCode = -3;
                responseModel = new ResponseModel_order_retrieve(resultCode, "JSON Parse Exception", responseModel.getTransactions());
                ServiceLogger.LOGGER.warning("Unable to map JSON to POJO");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            } else if (e instanceof JsonMappingException) {

                resultCode = -2;
                responseModel = new ResponseModel_order_retrieve(resultCode, "JSON Mapping Exception", responseModel.getTransactions());
                ServiceLogger.LOGGER.info("*****************************");
                ServiceLogger.LOGGER.warning("Unable to map JSON to POJO");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            } else {
                resultCode = -1;
                responseModel = new ResponseModel_order_retrieve(resultCode, "Internal Server Error", responseModel.getTransactions());
                ServiceLogger.LOGGER.severe("Internal Server Error");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(responseModel).build();
            }
    }

       // Case 3410: Orders retrieved successfully.
        responseModel = new ResponseModel_order_retrieve(3410, "Orders retrieved successfully.", responseModel.getTransactions());
        ServiceLogger.LOGGER.severe("Orders retrieved successfully.");
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
