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
import java.text.*;
import java.util.Date;


/*
先看一下cart里面有没有， 没有就是312， else检查applied code里面有没有这个code，有的话，
if(cart里面没有，即没有买东西){
    Case 312: Shopping cart item does not exist.
}else{
    if(code valid,即存在于discount里面，证明是有这个code){
        if(今天日期对得上，即在due day之前)
        {
            if(appiled code里面没有，即之前没有用过){
                insert ，加进去applied code里面，使用次数为1
                假如有问题，则Case 3640: Unable to apply discount code.
            }else{
                if(applied code数量少于limit){
                    update applied code， 使用次数++
                }else{
                    Case 3630: Discount code limit exceeded.
                }
            }
        }else{
            Case 3620: Discount code is expired.
        }
    }else{
        Case 3610: Discount code is invalid..
    }
}
        Case 3600: Discount code successfully applied.
 */

@Path("discount")
public class testPage_discount_apply {
    @Path("apply")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response forDiscount_apply(@Context HttpHeaders headers, String jsonText_apply){
        // Set the path of the endpoint we want to communicate with
        MoviesConfigs configs = BillingService.getMoviesConfigs();
        String servicePath = configs.getScheme() + configs.getHostName() + ":" + configs.getPort() + configs.getPath();//"http://localhost:2942/activity";
        String endpointPath = "/thumbnail";

        // Declare models
        RequestModel_discount_apply requestModel = new RequestModel_discount_apply();
        RequestModel_thumbnail requestModel_thumbnail = new RequestModel_thumbnail();
        ResponseModel_discount_apply responseModel = new ResponseModel_discount_apply(0, "", null);
        ResponseModel_thumbnail responseModel_thumbnail = null;
        ObjectMapper mapper = new ObjectMapper();

        // Get header strings
        // If there is no header with given key, it will be null
        String email = headers.getHeaderString("email");
        String session_id = headers.getHeaderString("session_id");
        String transaction_id = headers.getHeaderString("transaction_id");

        try{
            ServiceLogger.LOGGER.severe("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            requestModel = mapper.readValue(jsonText_apply, RequestModel_discount_apply.class);
            ServiceLogger.LOGGER.severe("requestModel email: " + requestModel.getEmail());

            //get the movie_id that this users had in cart from database
            ArrayList<String> purchasedIDList = new ArrayList<String>();
            String base_query = "SELECT DISTINCT cart.* " + //, genre.name AS 'genre'
                    "FROM cart " +
                    "WHERE cart.email = ?"; // and cart.quantity = ?
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
            if(purchasedIDList.size() !=0){
                ServiceLogger.LOGGER.info(purchasedIDList.get(0));
            }

            //if the cart is empty for this users, then we print does not exist, else we use these movie id to thunmnail, and get the info of the movie
            //Case 312: Shopping cart item does not exist.
            if(purchasedIDList.isEmpty()){
                responseModel = new ResponseModel_discount_apply(312, "Shopping cart item does not exist.", null);
                ServiceLogger.LOGGER.severe("Shopping cart item does not exist.");
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }
            else{
                //Todo: get the info from the discount_code table
                //get the movie_id that this users had in cart from database
                String end_date = null;
                float discount = 0;
                int usage_limit = 0;
                int counter = 0;
                String discount_code_query = "SELECT DISTINCT discount_code.* " + //, genre.name AS 'genre'
                        "FROM discount_code " +
                        "WHERE discount_code.code = ?"; // and cart.quantity = ?
                // Create the prepared statement
                PreparedStatement discount_code_ps = BillingService.getCon().prepareStatement(discount_code_query);

                discount_code_ps.setString(1, requestModel.getDiscount_code());
                // base_ps.setInt(2, requestModel.getQuantity());

                ServiceLogger.LOGGER.info("Trying selection: " + discount_code_ps.toString());
                ResultSet discount_code_rs = discount_code_ps.executeQuery();
                ServiceLogger.LOGGER.info("selection succeeded.");

                while (discount_code_rs.next()) {
                    end_date = discount_code_rs.getString("sale_end");
                    discount = discount_code_rs.getFloat("discount");
                    usage_limit = discount_code_rs.getInt("usage_limit");
                    counter++;
                }
                ServiceLogger.LOGGER.severe("counter: " + counter);
                ServiceLogger.LOGGER.severe("end_date: " + end_date);
                ServiceLogger.LOGGER.severe("usage_limit: " + usage_limit);
                //Case 3610: Discount code is invalid..  不在 discount_code table
                if(counter == 0){
                    responseModel = new ResponseModel_discount_apply(3610, "Discount code is invalid.", null);
                    ServiceLogger.LOGGER.severe("Discount code is invalid.");
                    return Response.status(Response.Status.OK).entity(responseModel).build();
                }
                else{
                    //Case 3620: Discount code is expired.      看discount_code table里面的expire time
                    //get current time:
                    Date date=new Date();
                    SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");
                    // String today= formatDate.format(date);
                    Date expireDay=formatDate.parse(end_date);
                    if(date.after(expireDay)){
                        responseModel = new ResponseModel_discount_apply(3620, "Discount code is expired.", null);
                        ServiceLogger.LOGGER.severe("Discount code is expired.");
                        return Response.status(Response.Status.OK).entity(responseModel).build();
                    }else{
                        //Todo: 检查applied code是否存在
                        //Todo: get the info from the applied_code table
                        int timeThatUsed = 0;
                        ArrayList<String> tempListForCode = new ArrayList<String>();
                        String applied_code_query = "SELECT DISTINCT applied_code.*, discount_code.* " + //, genre.name AS 'genre'
                                "FROM applied_code " +
                                "LEFT JOIN discount_code on applied_code.discount_id = discount_code.discount_id " +
                                "WHERE applied_code.email = ? and discount_code.code =? "; // and cart.quantity = ?
                        // Create the prepared statement
                        PreparedStatement applied_code_ps = BillingService.getCon().prepareStatement(applied_code_query);

                        applied_code_ps.setString(1, requestModel.getEmail());
                        applied_code_ps.setString(2, requestModel.getDiscount_code());
                        // base_ps.setInt(2, requestModel.getQuantity());

                        ServiceLogger.LOGGER.info("Trying selection: " + applied_code_ps.toString());
                        ResultSet applied_code_rs = applied_code_ps.executeQuery();
                        ServiceLogger.LOGGER.info("selection succeeded.");

                        while (applied_code_rs.next()) {
                            ServiceLogger.LOGGER.info("in the loop!");
                            timeThatUsed = applied_code_rs.getInt("times_used");
                            ServiceLogger.LOGGER.info("timeThatUsed：" + timeThatUsed);

                            tempListForCode.add(applied_code_rs.getString("code"));
                            ServiceLogger.LOGGER.info("tempListForCode[0]: " + tempListForCode.get(0));
                        }
                        ServiceLogger.LOGGER.info("tempListForCode.size: " + tempListForCode.size());
                        if(!tempListForCode.isEmpty()){
                           if(timeThatUsed < usage_limit){
                               //update
                               // Construct the query
                               String update_query =  "UPDATE applied_code " +
                                       "SET times_used = times_used + 1 " +
                                       "WHERE email = ?";

                               // Create the prepared statement
                               PreparedStatement update_ps = BillingService.getCon().prepareStatement(update_query);

                               // Set the arguments
                              // update_ps.setInt(1, requestModel.getQuantity());
                               update_ps.setString(1, requestModel.getEmail());


                               // Save the query result to a ResultSet so records may be retrieved
                               ServiceLogger.LOGGER.info("Trying update: " + update_ps.toString());
                               // code = ps_user_status.executeUpdate();
                               // code = ps_privilege_level.executeUpdate();
                               update_ps.executeUpdate();
                               ServiceLogger.LOGGER.info("update succeeded.");
                               // ServiceLogger.LOGGER.info(query);

                           }else{
                               //Case 3630: Discount code limit exceeded. 看一下里面的记录，用了几次没用完就insert，不能insert就

                               responseModel = new ResponseModel_discount_apply(3630, "Discount code limit exceeded.", null);
                               ServiceLogger.LOGGER.severe("Discount code limit exceeded.");
                               return Response.status(Response.Status.OK).entity(responseModel).build();

                           }

                        }else{
                            //insert
                            try{
                                // Construct the query
                                String applied_code_insert_query =  "INSERT INTO applied_code (email, discount_id, times_used)" +
                                        " VALUE (?, (SELECT discount_id from discount_code where discount_code.code = ? ), ?) ";

                                // Create the prepared statement
                                PreparedStatement applied_code_insert_ps = BillingService.getCon().prepareStatement(applied_code_insert_query);

                                // Set the arguments
                                applied_code_insert_ps.setString(1, requestModel.getEmail());
                                applied_code_insert_ps.setString(2, requestModel.getDiscount_code());
                                applied_code_insert_ps.setInt(3, 1);      //might change later

                                // Save the query result to a ResultSet so records may be retrieved
                                ServiceLogger.LOGGER.info("Trying insertion: " + applied_code_insert_ps.toString());
                                // code = ps_user_status.executeUpdate();
                                // code = ps_privilege_level.executeUpdate();
                                applied_code_insert_ps.executeUpdate();
                                ServiceLogger.LOGGER.info("Insertion succeeded.");
                                // ServiceLogger.LOGGER.info(query);
                            }catch(SQLException e){
                                e.printStackTrace();
                                //Case 3640: Unable to apply discount code.
                                responseModel = new ResponseModel_discount_apply(3640, "Unable to apply discount code.", null);
                                ServiceLogger.LOGGER.severe("Unable to apply discount code.");
                                return Response.status(Response.Status.OK).entity(responseModel).build();
                            }
                        }

                    }
                }


            }
            //获取item
            requestModel_thumbnail.setMovie_id(purchasedIDList);

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
            Response response = invocationBuilder.post(Entity.entity(requestModel_thumbnail, MediaType.APPLICATION_JSON));
            ServiceLogger.LOGGER.info("Request sent.");

            ServiceLogger.LOGGER.info("Received status " + response.getStatus());
            try {
                String jsonText = response.readEntity(String.class);
                responseModel_thumbnail = mapper.readValue(jsonText, ResponseModel_thumbnail.class);
                ServiceLogger.LOGGER.info("Successfully mapped response to POJO.");

                //make a templist to save info
                ArrayList<itemModel> templist = new ArrayList<itemModel>();
                ServiceLogger.LOGGER.info("pursechased ID list size is: " + purchasedIDList.size());
                for(int i = 0; i < purchasedIDList.size(); i++){
                    itemModel newItem = new itemModel();
                    newItem.setEmail(requestModel.getEmail());
                    newItem.setMovie_id(responseModel_thumbnail.getThumbNailList().get(i).getMovie_id());
                    newItem.setMovie_title(responseModel_thumbnail.getThumbNailList().get(i).getTitle());
                    newItem.setBackdrop_path(responseModel_thumbnail.getThumbNailList().get(i).getBackdrop_path());
                    newItem.setPoster_path(responseModel_thumbnail.getThumbNailList().get(i).getPoster_path());


                    //try to get discount, unit_price and etc
                    //mySQL

                    String item_query = "SELECT DISTINCT movie_price.*, cart.*, discount_code.discount As newDiscount " +
                            "FROM movie_price " +
                            "LEFT OUTER JOIN cart ON cart.movie_id = movie_price.movie_id " +
                            "LEFT OUTER JOIN applied_code ON cart.email = applied_code.email " +
                            "LEFT OUTER JOIN discount_code ON discount_code.discount_id = applied_code.discount_id " +
                            "WHERE cart.email = ? and discount_code.code = ?";
                    // Create the prepared statement
                    PreparedStatement item_ps = BillingService.getCon().prepareStatement(item_query);

                    item_ps.setString(1, newItem.getEmail());
                    item_ps.setString(2, requestModel.getDiscount_code());

                    ServiceLogger.LOGGER.info("Trying selection: " + item_ps.toString());
                    ResultSet item_rs = item_ps.executeQuery();
                    ServiceLogger.LOGGER.info("selection succeeded.");

                    while (item_rs.next()) {
                        newItem.setDiscount(item_rs.getFloat("newDiscount"));
                        newItem.setUnit_price(item_rs.getFloat("unit_price"));
                        newItem.setEmail(item_rs.getString("email"));
                        newItem.setQuantity(item_rs.getInt("quantity"));
                    }
                    ServiceLogger.LOGGER.info(newItem.getMovie_id());
                    ServiceLogger.LOGGER.info(newItem.getMovie_title());
                    ServiceLogger.LOGGER.info(newItem.getEmail());
                    ServiceLogger.LOGGER.info(String.valueOf(newItem.getDiscount()));
                    ServiceLogger.LOGGER.info(newItem.getBackdrop_path());
                    templist.add(newItem);
                    ServiceLogger.LOGGER.info(String.valueOf(templist.size()));
                    try{
                        ServiceLogger.LOGGER.info(String.valueOf(templist.isEmpty()));
                        responseModel.setItems(templist);
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
                    responseModel = new ResponseModel_discount_apply(resultCode, "JSON Parse Exception", null);
                    ServiceLogger.LOGGER.warning("Unable to map JSON to POJO");
                    return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
                } else if (e instanceof JsonMappingException) {

                    resultCode = -2;
                    responseModel = new ResponseModel_discount_apply(resultCode, "JSON Mapping Exception", null);
                    ServiceLogger.LOGGER.info("*****************************" );
                    ServiceLogger.LOGGER.warning("Unable to map JSON to POJO");
                    return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
                } else {
                    resultCode = -1;
                    responseModel = new ResponseModel_discount_apply(resultCode, "Internal Server Error", null);
                    ServiceLogger.LOGGER.severe("Internal Server Error");
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(responseModel).build();
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }





        //Case 3600: Discount code successfully applied.
        responseModel = new ResponseModel_discount_apply(3600, "Discount code successfully applied.", responseModel.getItems());
        ServiceLogger.LOGGER.severe("Discount code successfully applied.");
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

//3610 3620
/*

 */

//3630
/*
//Case 3630: Discount code limit exceeded. 看一下里面的记录，用了几次没用完就insert，不能insert就
                        if(timeThatUsed < usage_limit) {
                            responseModel = new ResponseModel_discount_apply(3630, "Discount code limit exceeded.", null);
                            ServiceLogger.LOGGER.severe("Discount code limit exceeded.");
                            return Response.status(Response.Status.OK).entity(responseModel).build();
                        }
 */
/*
 //Todo: get the info from the applied_code table
                int timeThatUsed = 0;
                String applied_code_query = "SELECT DISTINCT applied_code.*,  " + //, genre.name AS 'genre'
                        "FROM applied_code " +
                        "LEFT JOIN discount_code on applied_code.discount_id = discount_code.discount_id " +
                        "WHERE applied_code.email = ? and discount_code.code =? "; // and cart.quantity = ?
                // Create the prepared statement
                PreparedStatement applied_code_ps = BillingService.getCon().prepareStatement(applied_code_query);

                applied_code_ps.setString(1, requestModel.getEmail());
                applied_code_ps.setString(2, requestModel.getDiscount_code());
                // base_ps.setInt(2, requestModel.getQuantity());

                ServiceLogger.LOGGER.info("Trying selection: " + applied_code_ps.toString());
                ResultSet applied_code_rs = applied_code_ps.executeQuery();
                ServiceLogger.LOGGER.info("selection succeeded.");

                while (rs.next()) {
                    timeThatUsed = applied_code_rs.getInt("usage_limit");

                }
                //Case 3630: Discount code limit exceeded. 看一下里面的记录，用了几次没用完就insert，不能insert就
                if(timeThatUsed < usage_limit) {
                    responseModel = new ResponseModel_discount_apply(3630, "Discount code limit exceeded.", null);
                    ServiceLogger.LOGGER.severe("Discount code limit exceeded.");
                    return Response.status(Response.Status.OK).entity(responseModel).build();
                }
 */

//3640
/*
/Todo:insert into applied code
                // Construct the query
                String applied_code_insert_query =  "INSERT INTO cart (email, movie_id, quantity)" +
                        " VALUE (?, ?, ?)";

                // Create the prepared statement
                PreparedStatement applied_code_insert_ps = BillingService.getCon().prepareStatement(applied_code_insert_query);

                // Set the arguments
                applied_code_insert_ps.setString(1, requestModel.getEmail());
                applied_code_insert_ps.setString(2, requestModel.getMovie_id());
                applied_code_insert_ps.setInt(3, requestModel.getQuantity());      //might change later

                // Save the query result to a ResultSet so records may be retrieved
                ServiceLogger.LOGGER.info("Trying insertion: " + applied_code_insert_ps.toString());
                // code = ps_user_status.executeUpdate();
                // code = ps_privilege_level.executeUpdate();
                applied_code_insert_ps.executeUpdate();
                ServiceLogger.LOGGER.info("Insertion succeeded.");
                // ServiceLogger.LOGGER.info(query);

                //Case 3640: Unable to apply discount code.
 */

/*
//try to get discount, unit_price and etc
                        //mySQL

                        String item_query = "SELECT DISTINCT movie_price.*, cart.* " +
                                "FROM movie_price " +
                                "LEFT OUTER JOIN cart ON cart.movie_id = movie_price.movie_id " +
                                "WHERE movie_price.movie_id = ?";
                        // Create the prepared statement
                        PreparedStatement item_ps = BillingService.getCon().prepareStatement(item_query);

                        item_ps.setString(1, newItem.getMovie_id());
                        //movie_ps.setInt(2, requestModel.getQuantity());

                        ServiceLogger.LOGGER.info("Trying selection: " + item_ps.toString());
                        ResultSet item_rs = item_ps.executeQuery();
                        ServiceLogger.LOGGER.info("selection succeeded.");

                        while (item_rs.next()) {
                            newItem.setDiscount(item_rs.getFloat("discount"));
                            newItem.setUnit_price(item_rs.getFloat("unit_price"));
                            newItem.setEmail(item_rs.getString("email"));
                            newItem.setQuantity(item_rs.getInt("quantity"));
                        }
                        ServiceLogger.LOGGER.info(newItem.getMovie_id());
                        ServiceLogger.LOGGER.info(newItem.getMovie_title());
                        ServiceLogger.LOGGER.info(newItem.getEmail());
                        ServiceLogger.LOGGER.info(String.valueOf(newItem.getDiscount()));
                        ServiceLogger.LOGGER.info(newItem.getBackdrop_path());
                        templist.add(newItem);
                        ServiceLogger.LOGGER.info(String.valueOf(templist.size()));
                        try{
                            ServiceLogger.LOGGER.info(String.valueOf(templist.isEmpty()));
                            responseModel.setItems(templist);
                        }catch(Exception e){
                            e.printStackTrace();
                        }
 */