package learn.platform.grpc;

import learn.platform.grpc.service.HelloServiceClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GRpcApplicationTest {

    @Autowired
    private HelloServiceClient helloWorldClient;

    @Test
    public void testSayHello() {
        assertThat(helloWorldClient.sayHello("John", "Doe"))
                .isEqualTo("Hello John Doe!");
    }
}
