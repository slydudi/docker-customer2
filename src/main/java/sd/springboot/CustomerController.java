package sd.springboot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.prometheus.client.Counter;

@RestController
//@RequestMapping("/api")

public class CustomerController {

	static final Counter requestsAddCustomer = Counter.build().name("CustomerController:addCustomer").help("Total requests CustomerController:addCustomer.").register();
	static final Counter requestsfindCustomerAll = Counter.build().name("CustomerController:findCustomerAll").help("Total requests CustomerController:findCustomerAll.").register();
	static final Counter requestsfindCustomersByPesel = Counter.build().name("CustomerController:findCustomersByPesel").help("Total requests CustomerController:findCustomersByPesel.").register();
		
	  
	final Logger logger = LoggerFactory.getLogger(CustomerController.class);
	 
    @Autowired
    private CustomerService customerService;

    
    @PostMapping("customer/add")
    @WriteOperation
    public ResponseEntity<String> addCustomer(@RequestBody Customer customer){
    	logger.info("addCustomer");
    	requestsAddCustomer.inc();
	    customerService.add(customer);
        return ResponseEntity.ok(customer.getPesel());
    }

    
    @GetMapping("customer/search")
    public ResponseEntity<Iterable<Customer>> findCustomerAll() {
    	logger.info("findCustomerAll");
    	requestsfindCustomerAll.inc();
    	ResponseEntity<Iterable<Customer>> result =ResponseEntity.ok(customerService.findAll());  
    	return result;
    }
    
    @GetMapping("customer/search/{pesel}")
    public ResponseEntity<Iterable<Customer>> findCustomersByPesel(@PathVariable String pesel) {
    	logger.info("findCustomersByPesel");
    	requestsfindCustomersByPesel.inc();
    	ResponseEntity<Iterable<Customer>> result =  ResponseEntity.ok(customerService.findByPesel(pesel));
    	//span.finish();
    	return result;
    }
    

/*
    @DeleteMapping("products/delete/{name}/{id}")
    public ResponseEntity<String> removeProductByNameAndId(@PathVariable String name, @PathVariable UUID id){
        productService.remove(name, id);
        return ResponseEntity.ok(name + "," + id);
    }
	  @GetMapping("/listValues")
	  List <Customer>  listValues() {
		  Span span = jaegerTracer.buildSpan("listValues").start();
		  	
		  List <Customer> list = employeeRepository.findAll();
		  
		  span.finish();
		  
		  return list;
	  }
	  */
    /*
	  public static JaegerTracer getTracer(String service ) {
		  Configuration.SenderConfiguration senderConfig = Configuration.SenderConfiguration.fromEnv()
                  .withAgentHost("127.0.0.1")
                  .withAgentPort(6831);

		  Configuration.SamplerConfiguration samplerConfig = Configuration.SamplerConfiguration.fromEnv().withType("const").withParam(1).withManagerHostPort("6831");
	      Configuration.ReporterConfiguration reporterConfig = Configuration.ReporterConfiguration.fromEnv().withLogSpans(true).withSender(senderConfig);
	      Configuration config = new Configuration(service).withSampler(samplerConfig).withReporter(reporterConfig);
	      return config.getTracer();

	  }
	  */
}
