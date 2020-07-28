package learn.platform.grpc.service;

import io.grpc.stub.StreamObserver;
import learn.platform.grpc.model.Greeting;
import learn.platform.grpc.model.Person;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GRpcService
public class HelloServiceImpl extends HelloWorldServiceGrpc.HelloWorldServiceImplBase{

    private static final Logger LOGGER =
            LoggerFactory.getLogger(HelloServiceImpl.class);

    @Override
    public void sayHello(Person request,
                         StreamObserver<Greeting> responseObserver) {
        LOGGER.info("server received {}", request);

        String message = "Hello " + request.getFirstName() + " "
                + request.getLastName() + "!";
        Greeting greeting =
                Greeting.newBuilder().setMessage(message).build();
        LOGGER.info("server responded {}", greeting);

        responseObserver.onNext(greeting);
        responseObserver.onCompleted();
    }
}
