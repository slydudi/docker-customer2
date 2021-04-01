package sd.springboot;

import java.net.InetSocketAddress;
import java.nio.file.Paths;
import java.util.Collection;

import org.apache.catalina.util.CustomObjectInputStream;
import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.boot.autoconfigure.cassandra.DriverConfigLoaderBuilderCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;

import io.jaegertracing.Configuration.SamplerConfiguration;
import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.hotspot.DefaultExports;
import io.prometheus.client.spring.boot.SpringBootMetricsCollector;

@Configuration
public class CustomerApplicationConfigration {

	final org.slf4j.Logger logger = LoggerFactory.getLogger(CustomerController.class);

		@Value("${jaegertracing.senderconfig.host:none}")
    private String jaegertracingSenderconfigHost;

    @Value("${jaegertracing.senderconfig.port:none}")
    private String jaegertracingSenderconfigPort;
	    			
	//private static final String localHostIP = "127.0.0.1";
	//"127.0.0.1" "host.docker.internal"
	
    @Value("${astra.secure-connect-bundle:none}")
    private String astraSecureConnectBundle;

    @Value("${cassandra.keyspace:betterbotz}")
    private String keyspace;

    @Value("${cassandra.DB_USERNAME}")
    private String username;

    @Value("${cassandra.DB_PASSWORD}")
    private String password;

    @Value("${cassandra.contact-points}")
    private String contactPoints;

    @Value("${cassandra.port:9042}")
    private Integer port;

    @Value("${cassandra.local-datacenter:datacenter1}")
    private String localDataCenter;

    public String getKeyspace() {
        return this.keyspace;
    }

    public String getAstraSecureConnectBundle() {
        return this.astraSecureConnectBundle;
    }

    public String getLocalDataCenter() {
        return this.localDataCenter;
    }

    @Bean
    public CqlSessionBuilderCustomizer sessionBuilderCustomizer() {
        /* When using DataStax Astra, we must pass the secure connect bundle to the CqlSession
           See documentation: https://docs.datastax.com/en/astra/aws/doc/dscloud/astra/dscloudUsingDrivers.html
        */
        if (!astraSecureConnectBundle.equals("none")) {
            return builder -> builder
                    .withCloudSecureConnectBundle(Paths.get(this.astraSecureConnectBundle))
                    .withAuthCredentials(this.username, this.password);
        }
        else {
            return builder -> builder
                    .addContactPoint(new InetSocketAddress(this.contactPoints, this.port))
                    .withLocalDatacenter(this.localDataCenter)
                    .withAuthCredentials(this.username, this.password);
        }
    }

    @Bean
    public DriverConfigLoaderBuilderCustomizer driverConfigLoaderBuilderCustomizer() {
        /* When using DataStax Astra, we do not have to pass contact points like we normally would because
           this metadata is contained in the secure connect bundle.
         */
        if (!astraSecureConnectBundle.equals("none")) {
            return builder -> builder.without(DefaultDriverOption.CONTACT_POINTS);
        }
        return builder -> builder
                .withString(DefaultDriverOption.SESSION_NAME, "spring-boot-service");
    }

    @Bean (initMethod = "maybeInsertTestData")
    @DependsOn("tracer")
    public CustomerDao customerDao(CqlSession session) {
    	System.out.println("CustomerApplicationConfigration.customerDao");
    	CustomerDao  customerDao = new CustomerDao(session, keyspace, localDataCenter, astraSecureConnectBundle);
    	return customerDao;
    }
    
    
    @Bean (name = "tracer" )
    public io.opentracing.Tracer tracer() {
    	logger.info("io.opentracing.Tracer tracer()");
    	
        /*return new io.jaegertracing.Configuration("spring-boot", new io.jaegertracing.Configuration.SamplerConfiguration(ProbabilisticSampler.TYPE, 1),
            new io.jaegertracing.Configuration.ReporterConfiguration())
            .getTracer();
        */
        
		  io.jaegertracing.Configuration.SenderConfiguration senderConfig =  io.jaegertracing.Configuration.SenderConfiguration.fromEnv()
        //          .withAgentHost("127.0.0.1")
		          .withAgentHost(jaegertracingSenderconfigHost)
                  .withAgentPort(Integer.valueOf(jaegertracingSenderconfigPort).intValue());

		  SamplerConfiguration samplerConfig = io.jaegertracing.Configuration.SamplerConfiguration.fromEnv().withType("const").withParam(1).withManagerHostPort(jaegertracingSenderconfigPort);
		  io.jaegertracing.Configuration.ReporterConfiguration reporterConfig = io.jaegertracing.Configuration.ReporterConfiguration.fromEnv().withLogSpans(true).withSender(senderConfig);
		  io.jaegertracing.Configuration config = new io.jaegertracing.Configuration("customer").withSampler(samplerConfig).withReporter(reporterConfig);
		  return config.getTracer();

    }
        
    @Bean
    public ServletRegistrationBean servletRegistrationBean() {
        DefaultExports.initialize();
        return new ServletRegistrationBean(new MetricsServlet(), "/custom_metrics");
    }

    /*
    @Bean
    public SpringBootMetricsCollector springBootMetricsCollector(Collection<PublicMetrics> publicMetrics) {
        SpringBootMetricsCollector springBootMetricsCollector = new SpringBootMetricsCollector(
            publicMetrics);
        springBootMetricsCollector.register();
        return springBootMetricsCollector;
    }
    */
}