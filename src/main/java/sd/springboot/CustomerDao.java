package sd.springboot;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;

/*
import io.jaegertracing.Configuration;
import io.jaegertracing.internal.JaegerTracer;
import io.opentracing.Span;
*/
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/* Data Access Object for Products
   Note: We could have optionally used the Object Mapper distributed with the Cassandra Java driver
   See documentation: https://docs.datastax.com/en/developer/java-driver/latest/manual/mapper/
 */
public class CustomerDao {

    private PreparedStatement insertCustomer;
    private PreparedStatement selectCustomerByPesel;
    private PreparedStatement selectCustomerByLastName;
    private PreparedStatement selectCustomerAll;
    private CqlSession session;
  
    final Logger logger = LoggerFactory.getLogger(CustomerController.class);
    
    
    @Autowired
    private Tracer tracer;
    
    private static final String customerTableName = "customer";

    public CustomerDao(CqlSession session, String keyspace, String localDataCenter, String isAstra){
        this.session = session;
        maybeCreateProductSchema(keyspace, localDataCenter, isAstra);
        this.insertCustomer 			= session.prepare(getInsertProductStmt(keyspace));
        this.selectCustomerByPesel 		= session.prepare(getSelectCustomerByPeselStmt(keyspace));
        this.selectCustomerByLastName 	= session.prepare(getSelectCustomerByLastNameStmt(keyspace));
        this.selectCustomerAll 			= session.prepare(getSelectCustomerAllStmt(keyspace));
       // maybeInsertTestData(keyspace, localDataCenter, isAstra);
    }


    
    private void maybeCreateProductSchema(String keyspace, String localDataCenter, String isAstra){
        if (isAstra.equals("none")){
            session.execute(String.format("" +
                    "CREATE KEYSPACE IF NOT EXISTS %s WITH REPLICATION = {'class': 'NetworkTopologyStrategy', '%s': 1};",
                    keyspace, localDataCenter));
        }
        session.execute(String.format("CREATE TABLE IF NOT EXISTS %s.%s (id uuid, pesel text, " +
                "first_name text, last_name text, PRIMARY KEY (id));", keyspace, customerTableName));
    }

    public void maybeInsertTestData(){
    	logger.info("maybeInsertTestData");
    	addCustomer(new Customer(			UUID.fromString("9edca711-9ba0-4b36-9235-307b72400e41"),
											"PeselValue1",
											"firstNameValue1",
											"lastNameValue1"));

		addCustomer(new Customer(			UUID.fromString("9edca711-9ba0-4b36-9235-307b72400e42"),
											"PeselValue2",
											"firstNameValue2",
											"lastNameValue2"));
		
		
		addCustomer(new Customer(			UUID.fromString("9edca711-9ba0-4b36-9235-307b72400e43"),
											"PeselValue3",
											"firstNameValue3",
											"lastNameValue3"));
		
		
		addCustomer(new Customer(			UUID.fromString("9edca711-9ba0-4b36-9235-307b72400e44"),
											"PeselValue4",
											"firstNameValue4",
											"lastNameValue4"));
    }

    
    private String getInsertProductStmt(String keyspace){
        return String.format("" +
                "INSERT INTO %s.%s (id, pesel, first_name, last_name) " +
                "VALUES (?,?,?,?);", keyspace, customerTableName);
    }

    private String getSelectCustomerByPeselStmt(String keyspace){
        return String.format("SELECT id, pesel, first_name, last_name FROM %s.%s WHERE pesel=? ALLOW FILTERING;",
                keyspace, customerTableName);
        
    }

    private String getSelectCustomerByLastNameStmt(String keyspace){
        return String.format("SELECT id, pesel, first_name, last_name FROM %s.%s WHERE last_name=? ALLOW FILTERING;",
                keyspace, customerTableName);
    }

    private String getSelectCustomerAllStmt(String keyspace){
        return String.format("SELECT id, pesel, first_name, last_name FROM %s.%s ALLOW FILTERING;",
                keyspace, customerTableName);
    }


    public Iterable<Customer> findByPesel(String pesel){
    	logger.info("findByPesel");
    	try (Scope scope = tracer.buildSpan("CustomerDao.findByPesel").startActive(true)) {
            ResultSet rs = session.execute(selectCustomerByPesel.bind(pesel));
            List<Customer> customers = new ArrayList<>(rs.getAvailableWithoutFetching());
            for (Row row : rs){
            	customers.add(new Customer(row.getUuid("id"), row.getString("pesel"), row.getString("first_name"), row.getString("last_name")));
            }
            return customers;
    	}
    }

    public Iterable<Customer>  findByLastName(String lastName){
    	logger.info("findByLastName");
    	try (Scope scope = tracer.buildSpan("CustomerDao.findByLastName").startActive(true)) {
	        ResultSet rs = session.execute(selectCustomerByLastName.bind(lastName));
	        List<Customer> customers = new ArrayList<>(rs.getAvailableWithoutFetching());
	        for (Row row : rs){
	        	customers.add(new Customer(row.getUuid("id"), row.getString("pesel"), row.getString("first_name"), row.getString("last_name")));
	        }
	        return customers;
    	}
    }
	
    

    public Iterable<Customer>  findAll() {
    	logger.info("findAll");
    	try (Scope scope = tracer.buildSpan("CustomerDao.findAll").startActive(true)) {
	        ResultSet rs = session.execute(selectCustomerAll.bind());
	        List<Customer> customers = new ArrayList<>(rs.getAvailableWithoutFetching());
	        for (Row row : rs){
	        	customers.add(new Customer(row.getUuid("id"), row.getString("pesel"), row.getString("first_name"), row.getString("last_name")));
	        }
	        return customers;
    	}
    }
    
    public void addCustomer(Customer customer){
    	logger.info("addCustomer");
    	try (Scope scope = tracer.buildSpan("CustomerDao.addCustomer").startActive(true)) {
    		session.execute(insertCustomer.bind(customer.getId(), customer.getPesel(), customer.getFirstName(), customer.getLastName()));
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
