package application.repository;

import application.controller.CustomerController;
import application.model.Customer;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.query.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import java.util.List;
import java.util.Map;


public class CustomerRepository {
    final private static Logger logger = LoggerFactory.getLogger(CustomerController.class);

    public String getCustomerId() {
        final SecurityContext ctx = SecurityContextHolder.getContext();
        System.out.println("getCustomerId " + ctx.getAuthentication());

        if (ctx.getAuthentication() == null) {
            return null;
        }

        if (!ctx.getAuthentication().isAuthenticated()) {
            return null;
        }

        final OAuth2Authentication oauth = (OAuth2Authentication) ctx.getAuthentication();

        logger.debug("CustomerID: " + oauth.getName());

        return oauth.getName();
    }

    public String getCustomerByUsername(Database cloudant, String username) {
        String query = "{ \"selector\": { \"username\": \"" + username + "\" } }";
        System.out.println("username " + username + "  temp " + cloudant);
        final QueryResult<Customer> customers = cloudant.query(query, Customer.class);
        System.out.println("customers.toString" + customers.getDocs().toString());
        return customers.getDocs().toString();
    }

    public String getCustomerById(Database cloudant, String id) {
        String query = "{ \"selector\": { \"_id\": \"" + id + "\" } }";
        System.out.println("id " + id + "  temp " + cloudant);
        final QueryResult<Customer> customers = cloudant.query(query, Customer.class);
        System.out.println("customers.toString" + customers.getDocs().toString());
        return customers.getDocs().toString();
    }
}
