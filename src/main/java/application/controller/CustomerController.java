package application.controller;

import application.model.CloudantProperties;
import application.model.Customer;
import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.Response;
import com.cloudant.client.api.query.QueryResult;
import com.cloudant.client.org.lightcouch.NoDocumentException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller to manage Customer database
 */
@RestController
@RequestMapping("/customer")
@Api(value = "Customer Management System", description = "Operations pertaining to customer in Customer Management System")
public class CustomerController {
    final private static Logger logger = LoggerFactory.getLogger(CustomerController.class);

    private Database cloudant;

    @Autowired
    private CloudantProperties cloudantProperties = new CloudantProperties();


    @PostConstruct
    private void init() throws MalformedURLException {
        logger.debug(cloudantProperties.toString());

        try {
            logger.info("Connecting to cloudant at: " + cloudantProperties.getProtocol() + "://" + cloudantProperties.getHost() + ":" + cloudantProperties.getPort());
            final CloudantClient cloudantClient = ClientBuilder.url(new URL(cloudantProperties.getProtocol() + "://" + cloudantProperties.getHost() + ":" + cloudantProperties.getPort()))
                    .username(cloudantProperties.getUsername())
                    .password(cloudantProperties.getPassword())
                    .build();

            cloudant = cloudantClient.database(cloudantProperties.getDatabase(), true);

            // create the design document if it doesn't exist
            if (!cloudant.contains("_design/username_searchIndex")) {
                final Map<String, Object> names = new HashMap<String, Object>();
                names.put("index", "function(doc){index(\"usernames\", doc.username); }");

                final Map<String, Object> indexes = new HashMap<String, Object>();
                indexes.put("usernames", names);

                final Map<String, Object> view_ddoc = new HashMap<String, Object>();
                view_ddoc.put("_id", "_design/username_searchIndex");
                view_ddoc.put("indexes", indexes);

                cloudant.save(view_ddoc);
            }

        } catch (MalformedURLException e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
    }

    public Database getCloudant() {
        return cloudant;
    }


    /**
     * @return customer by username
     */
    @ApiOperation(value = "Search a customer by username", response = Customer.class)
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    protected @ResponseBody
    ResponseEntity<?> searchCustomers(@RequestHeader Map<String, String> headers, @RequestParam(required = true) String username) {
        System.out.println("Searching for customer " + username);
        try {
            if (username == null) {
                return ResponseEntity.badRequest().body("Missing username");
            }
            return ResponseEntity.ok(getCustomerByUsername(username));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    public String getCustomerByUsername(String username) {
        String query = "{ \"selector\": { \"username\": \"" + username + "\" } }";
        System.out.println("username " + username + "  temp " + getCloudant());
        final QueryResult<Customer> customers = getCloudant().query(query, Customer.class);
        return customers.getDocs().toString();
    }


    private String getCustomerId() {
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

    /**
     * @return all customer
     * @throws Exception
     */

    @ApiOperation(value = "View a list of available customers", response = Iterable.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved list"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    }
    )
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    protected ResponseEntity<?> getCustomers() throws Exception {
        try {
            final String customerId = getCustomerId();
            if (customerId == null) {
                // if no user passed in, this is a bad request
                return ResponseEntity.badRequest().body("Invalid Bearer Token: Missing customer ID");
            }

            logger.debug("caller: " + customerId);
            final Customer cust = getCloudant().find(Customer.class, customerId);

            return ResponseEntity.ok(Arrays.asList(cust));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        }

    }

    /**
     * @return customer by id
     */
    @ApiOperation(value = "Search a customer by id", response = Customer.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    protected ResponseEntity<?> getById(@RequestHeader Map<String, String> headers, @PathVariable String id) {
        try {
            final String customerId = getCustomerId();
            if (customerId == null) {
                // if no user passed in, this is a bad request
                return ResponseEntity.badRequest().body("Invalid Bearer Token: Missing customer ID");
            }

            logger.debug("caller: " + customerId);

            if (!customerId.equals(id)) {
                // if i'm getting a customer ID that doesn't match my own ID, then return 401
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            final Customer cust = getCloudant().find(Customer.class, customerId);

            return ResponseEntity.ok(cust);
        } catch (NoDocumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer with ID " + id + " not found");
        }
    }

    /**
     * Add customer
     *
     * @return transaction status
     */
    @ApiOperation(value = "Add a customer")
    @RequestMapping(value = "/add", method = RequestMethod.POST, consumes = "application/json")
    protected ResponseEntity<?> create(@RequestHeader Map<String, String> headers, @RequestBody Customer payload) {
        try {
            // TODO: no one should have access to do this, it's not exposed to APIC
            final Database cloudant = getCloudant();

            if (payload.get_id() != null && cloudant.contains(payload.get_id())) {
                return ResponseEntity.badRequest().body("Id " + payload.get_id() + " already exists");
            }

            String customer = getCustomerByUsername(payload.getUsername());
            if (!customer.isEmpty()) {
                return ResponseEntity.badRequest().body("Customer with name " + payload.getUsername() + " already exists");
            }

            // TODO: hash password
            //cust.setPassword(payload.getPassword());

            final Response resp = cloudant.save(payload);

            if (resp.getError() == null) {
                // HTTP 201 CREATED
                final URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(resp.getId()).toUri();
                return ResponseEntity.created(location).build();
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp.getError());
            }

        } catch (Exception ex) {
            logger.error("Error creating customer: " + ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating customer: " + ex.toString());
        }

    }

    /**
     * Update customer
     *
     * @return transaction status
     */
    @ApiOperation(value = "Update customer by id")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.PUT, consumes = "application/json")
    protected ResponseEntity<?> update(@RequestHeader Map<String, String> headers, @PathVariable String id, @RequestBody Customer payload) {

        try {
            final String customerId = getCustomerId();
            if (customerId == null) {
                // if no user passed in, this is a bad request
                return ResponseEntity.badRequest().body("Invalid Bearer Token: Missing customer ID");
            }

            logger.info("caller: " + customerId);
            if (!customerId.equals("id")) {
                // if i'm getting a customer ID that doesn't match my own ID, then return 401
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            final Database cloudant = getCloudant();
            final Customer cust = getCloudant().find(Customer.class, id);

            cust.setFirstName(payload.getFirstName());
            cust.setLastName(payload.getLastName());
            cust.setImageUrl(payload.getImageUrl());
            cust.setEmail(payload.getEmail());

            // TODO: hash password
            cust.setPassword(payload.getPassword());

            cloudant.save(payload);
        } catch (NoDocumentException e) {
            logger.error("Customer not found: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer with ID " + id + " not found");
        } catch (Exception ex) {
            logger.error("Error updating customer: " + ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating customer: " + ex.toString());
        }

        return ResponseEntity.ok().build();
    }

    /**
     * Delete customer
     *
     * @return transaction status
     */
    @ApiOperation(value = "Delete a customer by id")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
    protected ResponseEntity<?> delete(@RequestHeader Map<String, String> headers, @PathVariable String id) {
        System.out.println("deleting customer id " + id);
        // TODO: no one should have access to do this, it's not exposed to APIC
        try {
            final Database cloudant = getCloudant();
            final Customer cust = getCloudant().find(Customer.class, id);
            cloudant.remove(cust);
        } catch (NoDocumentException e) {
            logger.error("Customer not found: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer with ID " + id + " not found");
        } catch (Exception ex) {
            logger.error("Error deleting customer: " + ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting customer: " + ex.toString());
        }
        return ResponseEntity.ok().build();
    }
}
