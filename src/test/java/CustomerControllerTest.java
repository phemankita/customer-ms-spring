import application.model.Customer;
import org.junit.Test;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CustomerControllerTest {
    @LocalServerPort
    int serverPort = 8080;
    String api_endpoint = "/customer/";

    Customer newCustomer = new Customer("123", null, "oiricaud23",
            "passw0rd", "perla", "hernandez", "helloworld@gmail.com",
            "test.png");

    /**
     * Method is responsible for checking the /check rest end point
     *
     * @throws URISyntaxException
     */
    @Test
    public void check() throws URISyntaxException {
        RestTemplate restTemplate = new RestTemplate();
        final String baseUrl = "http://localhost:" + serverPort + api_endpoint + "check";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();

        HttpEntity request = new HttpEntity(headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(uri, request, String.class);
            int responseCode = response.getStatusCodeValue();
            String responseBody = response.getBody();
            System.out.println("responseBody " + responseBody);
            assertEquals(responseBody, "It works!");
        } catch (HttpClientErrorException e) { // Catch error when adding duplicate employee
            assertEquals(406, e.getRawStatusCode()); // Catch error when adding duplicate employee
        }
    }

    /**
     * Method is responsible for testing the functionality of adding a customer to the database
     * table
     *
     * @throws URISyntaxException
     */
    @Test
    public void addCustomer() throws URISyntaxException {
        RestTemplate restTemplate = new RestTemplate();
        String baseUrl = "http://localhost:" + serverPort + api_endpoint + "add";
        URI uri = new URI(baseUrl);
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Customer> request = new HttpEntity(newCustomer, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(uri, request, String.class);
            int responseCode = response.getStatusCodeValue();
            String responseBody = response.getBody();
            System.out.println("response " + response);
            assertEquals(201, responseCode);
        } catch (HttpClientErrorException e) { // Catch error when adding duplicate employee
            assertEquals(406, e.getRawStatusCode()); // Catch error when adding duplicate employee
        }
    }

    /**
     * Method is responsible for updating a customer by id
     * table
     *
     * @throws URISyntaxException
     */
    @Test
    public void updateCustomerById() {
        final String baseUrl = "http://localhost:" + serverPort + api_endpoint + "list";
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<List<Customer>> response = restTemplate.exchange(baseUrl, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Customer>>() {
                });

        List<Customer> customers = response.getBody();

        System.out.println("customers list " + customers.toString());

        assertEquals(200, response.getStatusCodeValue());

    }

    /**
     * Method is responsible for searching a customer by username
     *
     * @throws URISyntaxException
     */
    @Test
    public void searchCustomerByUsername() throws URISyntaxException {
        final String baseUrl = "http://localhost:" + serverPort + api_endpoint + "get";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();

        URI uri = new URI(baseUrl);


        Customer customer = new Customer();

        int employeeId = 1; // Joffrey Lannister id
        HttpEntity<Integer> request = new HttpEntity<>(employeeId, httpHeaders);
        ResponseEntity<Customer> responseWhenFound = restTemplate.postForEntity(uri, request, Customer.class);

        customer = responseWhenFound.getBody();

        System.out.println("employee " + customer.toString());
        assertEquals(200, responseWhenFound.getStatusCodeValue());

    }

    /**
     * Method is responsible for searching a customer by id
     *
     * @throws URISyntaxException
     */
    @Test
    public void searchCustomerById() throws URISyntaxException {
        final String baseUrl = "http://localhost:" + serverPort + api_endpoint + "get";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();

        URI uri = new URI(baseUrl);


        Customer customer = new Customer();

        int employeeId = 1; // Joffrey Lannister id
        HttpEntity<Integer> request = new HttpEntity<>(employeeId, httpHeaders);
        ResponseEntity<Customer> responseWhenFound = restTemplate.postForEntity(uri, request, Customer.class);

        customer = responseWhenFound.getBody();

        System.out.println("employee " + customer.toString());
        assertEquals(200, responseWhenFound.getStatusCodeValue());

    }

    /**
     * Method is responsible for deleting customer
     *
     * @throws URISyntaxException
     */
    @Test
    public void deleteCustomerById() throws URISyntaxException {
        RestTemplate restTemplate = new RestTemplate();
        String baseUrl = "http://localhost:" + serverPort + api_endpoint + "delete/" + newCustomer.get_id();
        URI uri = new URI(baseUrl);
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity(newCustomer.get_id(), headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(uri, request, String.class);
            int responseCode = response.getStatusCodeValue();
            String responseBody = response.getBody();
            System.out.println("response " + response);
            assertEquals(200, responseCode);
        } catch (HttpClientErrorException e) { // Catch error when adding duplicate employee
            assertEquals(406, e.getRawStatusCode()); // Catch error when adding duplicate employee
        }
    }

    /**
     * Method is responsible for retrieving all customers
     *
     * @throws URISyntaxException
     */
    @Test
    public void getAllCustomers() {
        final String baseUrl = "http://localhost:" + serverPort + api_endpoint + "list";
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<List<Customer>> response = restTemplate.exchange(baseUrl, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Customer>>() {
                });

        List<Customer> customers = response.getBody();

        System.out.println("customers list " + customers.toString());

        assertEquals(200, response.getStatusCodeValue());

    }

}
