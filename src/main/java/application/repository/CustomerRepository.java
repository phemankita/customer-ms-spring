package application.repository;

import application.controller.CustomerController;
import application.model.Customer;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.query.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import java.util.LinkedList;
import java.util.List;


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

    public List<Customer> getCustomers(Database database) {
        String query = "{" +
                "   \"selector\": {" +
                "      \"_id\": {" +
                "         \"$gt\": null" +
                "      }" +
                "   }" +
                "}";
        final QueryResult<Customer> customers = database.query(query, Customer.class);
        System.out.println("customers " + customers.getDocs());
        return customers.getDocs();
    }

    public String getCustomerByUsername(Database database, String username) {
        String query = "{ \"selector\": { \"username\": \"" + username + "\" } }";
        System.out.println("username " + username + "  temp " + database);
        final QueryResult<Customer> customers = database.query(query, Customer.class);
        System.out.println("customers.toString" + customers.getDocs().toString());
        return customers.getDocs().toString();
    }

    public String getCustomerById(Database database, String id) {
        String query = "{ \"selector\": { \"_id\": \"" + id + "\" } }";
        System.out.println("id " + id + "  temp " + database);
        final QueryResult<Customer> customers = database.query(query, Customer.class);
        System.out.println("customers.toString" + customers.getDocs().toString());
        return customers.getDocs().toString();
    }
}
