package edu.uci.ics.jiefengw.service.billing.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uci.ics.jiefengw.service.billing.logger.*;
import edu.uci.ics.jiefengw.service.billing.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RequestModel_privilege {
    @JsonProperty(value = "email", required = true)
    protected String email;


    @JsonProperty(value = "plevel", required = true)
    private int plevel;


    public RequestModel_privilege(String email, int plevel) {
        this.email = email;
        this.plevel = plevel;
    }
    public RequestModel_privilege() {}

    //Setter and Getter
    public String getEmail()
    {
        return email;
    }
    public void setEmail(String email)
    {
        this.email = email;
    }

    public int getPlevel() {return plevel;}
    public void setPlevel(int plevel) { this.plevel = plevel; }


    public boolean checkSufficientPLever() {
        boolean sufficient = true;
        try {
            String query = "SELECT plevel from user WHERE email = ?";
            PreparedStatement ps = BillingService.getCon().prepareStatement(query);
            ps.setString(1, email);
            ResultSet resultSet = ps.executeQuery();
            if(resultSet.next()) {
                int plevelInDB = resultSet.getInt("plevel");

                ServiceLogger.LOGGER.info("plevel to check:" + plevel);
                ServiceLogger.LOGGER.info("plevelInDB:" + plevelInDB);
                if(plevel >= plevelInDB) {
                    sufficient = true;
                }else{
                    sufficient = false;
                }
            }

        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve student records.");
            e.printStackTrace();
        }
        return sufficient;
    }
}
