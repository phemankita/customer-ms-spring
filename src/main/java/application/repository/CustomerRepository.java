package application.repository;

import application.controller.CustomerController;
import application.model.Customer;
import com.cloudant.client.api.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import java.util.List;


public class CustomerRepository {
    final private static Logger logger = LoggerFactory.getLogger(CustomerController.class);

    public String getCustomerId() {
        final SecurityContext ctx = SecurityContextHolder.getContext();
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
        final List<Customer> customers = cloudant.findByIndex(
                "{ \"selector\": { \"username\": \"" + username + "\" } }",
                Customer.class);

        //  query index
        //return  ResponseEntity.ok(customers);

//        String query = "{ \"selector\": { \"username\": \"" + username + "\" } }";
//        System.out.println("username " + username + "  temp " + getCloudant());
//        final QueryResult<Customer> customers = getCloudant().query(query, Customer.class);
        return customers.toString();
    }

}
