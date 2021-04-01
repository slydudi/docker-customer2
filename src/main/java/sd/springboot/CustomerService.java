package sd.springboot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;



@Service
public class CustomerService {

    @Autowired
    private CustomerDao customerDao;

    //private final JaegerTracer jaegerTracer;
    
    @Autowired
    private Tracer tracer;
    
    /*
    CustomerService () {
    	jaegerTracer 		= getTracer("CustomerService");
    }
    */
    
    public void add(Customer customer) {
 	   try (Scope scope = tracer.buildSpan("CustomerService.add").startActive(true)) {
 		  customerDao.addCustomer(customer);   
 	   }
    }


    public Iterable<Customer> findByPesel(String pesel) {
	   try (Scope scope = tracer.buildSpan("CustomerService.findByPesel").startActive(true)) {
	        Iterable<Customer>result =  customerDao.findByPesel(pesel);
	        return result;
	    }
    }

    public Iterable<Customer> findByLastName(String lastName) {
    	try (Scope scope = tracer.buildSpan("CustomerService.findByLastName").startActive(true)) {
    		Iterable<Customer> result = customerDao.findByLastName(lastName);
    		return result;
    	}
    }


    public Iterable<Customer> findAll() {
    	try (Scope scope = tracer.buildSpan("CustomerService.findAll").startActive(true)) {
	    	Iterable<Customer>  result = customerDao.findAll();
	    	return result;
    	}
    }
    
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