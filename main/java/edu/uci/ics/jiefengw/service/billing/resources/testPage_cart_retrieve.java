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
public class testPage_cart_retrieve {
    @Path("retrieve")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response forCart_retrieve(@Context HttpHeaders headers, String jsonText_retrieve){
        // Set the path of the endpoint we want to communicate with
        MoviesConfigs configs = BillingService.getMoviesConfigs();
        String servicePath = configs.getScheme() + configs.getHostName() + ":" + configs.getPort() + configs.getPath();//"http://localhost:2942/activity";
        String endpointPath = "/thumbnail";

        // Declare models
        RequestModel_cart_retrieve requestModel = new RequestModel_cart_retrieve();
        RequestModel_thumbnail requestModel_thumbnail = new RequestModel_thumbnail();
        ResponseModel_cart_retrieve responseModel = new ResponseModel_cart_retrieve(0, "", null);
        ResponseModel_thumbnail responseModel_thumbnail = null;
        ObjectMapper mapper = new ObjectMapper();

        // Get header strings
        // If there is no header with given key, it will be null
        String email = headers.getHeaderString("email");
        String session_id = headers.getHeaderString("session_id");
        String transaction_id = headers.getHeaderString("transaction_id");

        try{
            ServiceLogger.LOGGER.severe("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            requestModel = mapper.readValue(jsonText_retrieve, RequestModel_cart_retrieve.class);
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
                responseModel = new ResponseModel_cart_retrieve(312, "Shopping cart item does not exist.", null);
                ServiceLogger.LOGGER.severe("Shopping cart item does not exist.");
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }else{

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


                //we save the thumbanil info into  responseModel_thumbnail, and try to save to item
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

                    }


                } catch (IOException e) {
                    //ServiceLogger.LOGGER.warning("Unable to map response to POJO.");
                    int resultCode;
                    e.printStackTrace();

                    if (e instanceof JsonParseException) {
                        resultCode = -3;
                        responseModel = new ResponseModel_cart_retrieve(resultCode, "JSON Parse Exception", null);
                        ServiceLogger.LOGGER.warning("Unable to map JSON to POJO");
                        return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
                    } else if (e instanceof JsonMappingException) {

                        resultCode = -2;
                        responseModel = new ResponseModel_cart_retrieve(resultCode, "JSON Mapping Exception", null);
                        ServiceLogger.LOGGER.info("*****************************" );
                        ServiceLogger.LOGGER.warning("Unable to map JSON to POJO");
                        return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
                    } else {
                        resultCode = -1;
                        responseModel = new ResponseModel_cart_retrieve(resultCode, "Internal Server Error", null);
                        ServiceLogger.LOGGER.severe("Internal Server Error");
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(responseModel).build();
                    }
                }

            }


        }catch(Exception e){
            e.printStackTrace();
        }





        //Case 3130: Shopping cart retrieved successfully.
        responseModel = new ResponseModel_cart_retrieve(3130, "Shopping cart retrieved successfully.", responseModel.getItems());
        ServiceLogger.LOGGER.severe("Shopping cart retrieved successfully.");
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

/*

 */