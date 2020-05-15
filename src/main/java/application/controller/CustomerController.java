package application.controller;

import application.model.CloudantProperties;
import application.model.Customer;
import application.repository.CustomerRepository;
import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.Response;
import com.cloudant.client.org.lightcouch.NoDocumentException;
import com.google.common.base.Strings;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import javax.annotation.PostConstruct;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Class is responsible for handling rest end points
 */
@RestController
@RequestMapping("/customer")
@Api(value = "Customer Management System")
public class CustomerController {
    final private CustomerRepository customerRepository = new CustomerRepository();
    final private static Logger logger = LoggerFactory.getLogger(CustomerController.class);
    final private CloudantProperties cloudantProperties;
    private Database cloudant;

    public CustomerController(CloudantProperties cloudantProperties) {
        this.cloudantProperties = cloudantProperties;
    }

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
            if (!cloudant.contains("_design/username_search" +
                    "" +
                    "Index")) {
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

    public Database database() {
        return cloudant;
    }

    /**
     * check
     */
    @RequestMapping("/check")
    protected @ResponseBody
    ResponseEntity<String> check() {
        // test the cloudant connection
        try {
            database().info();
            return ResponseEntity.ok("It works!");
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Add customer
     *
     * @return transaction status
     */
    @ApiOperation(value = "Add a customer")
    @RequestMapping(value = "/add", method = RequestMethod.POST, consumes = "application/json")
    protected ResponseEntity<?> addCustomer(@RequestHeader Map<String, String> headers, @RequestBody Customer payload) {
        System.out.println("adding customer " + payload + "headers " + headers);
        try {
            // TODO: no one should have access to do this, it's not exposed to APIC
            final Database cloudant = database();
            System.out.println("alpha ");

            if (payload.get_id() != null && cloudant.contains(payload.get_id())) {
                System.out.println("beta ");
                return ResponseEntity.badRequest().body("Id " + payload.get_id() + " already exists");
            }

            String customer = customerRepository.getCustomerByUsername(database(), payload.getUsername());
            System.out.println("payload " + payload.toString());
            System.out.println("customer " + customer);
            if (Strings.isNullOrEmpty(customer)) {
                System.out.println("delta");
                return ResponseEntity.badRequest().body("Customer with name " + payload.getUsername() + " already exists");
            }

            final Response resp = cloudant.save(payload);
            System.out.println("response " + resp);
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
     * @return customer by username
     */
    @ApiOperation(value = "Search a customer by username", response = Customer.class)
    @RequestMapping(value = "/search", method = RequestMethod.POST)
    protected @ResponseBody
    ResponseEntity<?> searchCustomerByUsername(@RequestHeader Map<String, String> headers, @RequestBody String username) {
        System.out.println("Searching by username " + username);

        try {
            if (username == null) {
                return ResponseEntity.badRequest().body("Missing username");
            }
            return ResponseEntity.ok(customerRepository.getCustomerByUsername(database(), username));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * @return customer by username
     */
    @ApiOperation(value = "Search a customer by id", response = Customer.class)
    @RequestMapping(value = "/search/{id}", method = RequestMethod.POST)
    protected @ResponseBody
    ResponseEntity<?> searchCustomerById(@RequestHeader Map<String, String> headers, @PathVariable String id) {
        System.out.println("Searching by id " + id);

        try {
            if (id == null) {
                return ResponseEntity.badRequest().body("Missing username");
            }
            return ResponseEntity.ok(customerRepository.getCustomerById(database(), id));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete customer
     *
     * @return transaction status
     */
    @ApiOperation(value = "Delete a customer by id")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    protected ResponseEntity<?> deleteCustomerById(@RequestHeader Map<String, String> headers, @PathVariable String id) {
        System.out.println("deleting customer id " + id);
        // TODO: no one should have access to do this, it's not exposed to APIC
        try {
            final Database cloudant = database();
            final Customer cust = database().find(Customer.class, id);
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

    /**
     * @return all customers
     * @throws Exception
     */

    /**
     * Update customer
     *
     * @return transaction status
     */
    @ApiOperation(value = "Update customer by id")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST, consumes = "application/json")
    protected ResponseEntity<?> updateCustomerById(@RequestHeader Map<String, String> isAuthenticated, @PathVariable String id, @RequestBody Customer payload) {
        System.out.println("updating customer by id " + id);
        try {
            //final String customerId = customerRepository.getCustomerId();
            if (isAuthenticated == null) {
                // if no user passed in, this is a bad request
                return ResponseEntity.badRequest().body("Invalid Bearer Token: Missing customer ID");
            }
            if (isAuthenticated.get("securitycontext").equals("false")) {
                return ResponseEntity.badRequest().body("User does not have enough access to make such query");
            }

            logger.info("caller: " + payload.get_id());
            if (!payload.get_id().equals(id)) {
                // if i'm getting a customer ID that doesn't match my own ID, then return 401
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            final Database cloudant = database();

            // Find the customer with the old values
            Customer customer = database().find(Customer.class, id);

            // _rev is set to null from the test case, get the _rev and set it to the payload
            payload.set_rev(customer.get_rev());

            // set the payload to be the customer
            customer = payload;

            // update the database
            cloudant.update(customer);

        } catch (NoDocumentException e) {
            logger.error("Customer not found: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer with ID " + id + " not found");
        } catch (Exception ex) {
            logger.error("Error updating customer: " + ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating customer: " + ex.toString());
        }

        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "View a list of available customers", response = Iterable.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved list"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    }
    )
    @RequestMapping(value ="/list", method = RequestMethod.GET)
    protected ResponseEntity<?> getAllCustomers() {
        return ResponseEntity.ok(customerRepository.getCustomers(database()));
    }

}
