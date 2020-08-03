package learn.platform.grpc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
public class GRpcApplication {

    public static void main(String[] args) {
        SpringApplication.run(GRpcApplication.class, args);
    }
}
