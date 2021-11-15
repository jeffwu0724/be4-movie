package edu.uci.ics.jiefengw.service.billing.models;

import com.braintreepayments.http.HttpResponse;
import com.braintreepayments.http.exceptions.HttpException;
import com.braintreepayments.http.serializer.Json;
import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import com.paypal.orders.*;
import edu.uci.ics.jiefengw.service.billing.logger.ServiceLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class PayPalOrderClient {
    /*
    //intialize sandbox stuff
    private static final String clientId = "\n" +
            "*";
    private static final String clientSecret = "*";

    //setup paypal envrionment
    public static PayPalEnvironment environment = new PayPalEnvironment.Sandbox(clientId, clientSecret);
    //Create client for environment
    private static PayPalHttpClient client = new PayPalHttpClient(environment);

     */
    private static String clientId = "*";
    private static String clientSecret = "*";

    //setup paypal envrionment
    public static PayPalEnvironment environment = new PayPalEnvironment.Sandbox(clientId, clientSecret);
    //Create client for environment
    public static PayPalHttpClient client = new PayPalHttpClient(environment);

    public static String approve_url;
    public static String payer_id;
    public static String order_id;

    public static String capture_id;
    public static String state;
    public static String total;
    public static String currency;
    public static String value;
    public static String create_time;
    public static String update_time;




    public static String createPayPalOrder(PayPalOrderClient client, boolean debug, double sum)
    {
        //Construct a request object and set desired parameters
        //Here orderscreaterequest creates a post request to v2/checkout/orders

        OrderRequest orderRequest = new OrderRequest();

        //MUST use this method instead of intent to create capture.
        orderRequest.checkoutPaymentIntent("CAPTURE");

        //Create application context with return url upon payer completion.
       // String URL = "127.0.0.1/12345/api/billing/order/complete?token=" +order_id+ "&payer_id ="+payer_id;
        ApplicationContext applicationContext = new ApplicationContext().returnUrl("http://localhost:12348/api/billing/order/complete");//


        orderRequest.applicationContext(applicationContext);

        List<PurchaseUnitRequest> purchaseUnits = new ArrayList<>();
        purchaseUnits
                .add(new PurchaseUnitRequest().amountWithBreakdown(new AmountWithBreakdown().currencyCode("USD").value(String.valueOf(sum))));
        orderRequest.purchaseUnits(purchaseUnits);
        OrdersCreateRequest request = new OrdersCreateRequest().requestBody(orderRequest);

        try {
            // Call API with your client and get a response for your call
            HttpResponse<Order> response = client.client.execute(request);

            // If call returns body in response, you can get the de-serialized version by
            // calling result() on the response
            if (debug) {
                if (response.statusCode() == 201) {
                    System.out.println("Status Code: " + response.statusCode());
                    System.out.println("Status: " + response.result().status());
                    System.out.println("Order ID: " + response.result().id());
                    response.result().links().forEach(link -> System.out.println(link.rel() + " => " + link.method() + ":" + link.href()));
                    //try to get approve_url
                    ArrayList<String> linkList = new ArrayList<String>();
                    response.result().links().forEach(link -> linkList.add(link.href()));
                    approve_url = linkList.get(1);
                }
            }
            order_id = response.result().id();
            return response.result().id();
        } catch (IOException ioe) {
            System.err.println("*******COULD NOT CREATE ORDER*******");
            if (ioe instanceof HttpException) {
                // Something went wrong server-side
                HttpException he = (HttpException) ioe;
                System.out.println(he.getMessage());
                he.headers().forEach(x -> System.out.println(x + " :" + he.headers().header(x)));
            } else {


                // Something went wrong client-side
            }
            return null;
        }

    }

    public static void captureOrder(String orderID, PayPalOrderClient orderClient)
    {
        Order order = null;
        OrdersCaptureRequest request = new OrdersCaptureRequest(orderID);

        try {
            // Call API with your client and get a response for your call
            HttpResponse<Order> response = orderClient.client.execute(request);

            // If call returns body in response, you can get the de-serialized version by
            // calling result() on the response
            order = response.result();

            System.out.println("Capture ID: " + order.purchaseUnits().get(0).payments().captures().get(0).id());
            capture_id = order.purchaseUnits().get(0).payments().captures().get(0).id();
            value = order.purchaseUnits().get(0).payments().captures().get(0).sellerReceivableBreakdown().paypalFee().value();
            total = order.purchaseUnits().get(0).payments().captures().get(0).amount().value();
            currency = order.purchaseUnits().get(0).payments().captures().get(0).sellerReceivableBreakdown().paypalFee().currencyCode();
            state = response.result().status();
            order.purchaseUnits().get(0).payments().captures().get(0).links()
                    .forEach(link -> System.out.println(link.rel() + " => " + link.method() + ":" + link.href()));
        } catch (IOException ioe) {
            if (ioe instanceof HttpException) {
                // Something went wrong server-side

                HttpException he = (HttpException) ioe;
                System.out.println(he.getMessage());
                he.headers().forEach(x -> System.out.println(x + " :" + he.headers().header(x)));
            } else {
                // Something went wrong client-side
            }
        }

    }

    public static void getOrder(String orderId, PayPalOrderClient orderClient) throws IOException {

        OrdersGetRequest request = new OrdersGetRequest(orderId);
        HttpResponse<Order> response = orderClient.client.execute(request);
        System.out.println("Full response body:" + (new Json().serialize(response.result())));

        state = response.result().status();
        create_time = response.result().createTime();
        update_time = response.result().updateTime();
        //value = response.result().

    }
}




