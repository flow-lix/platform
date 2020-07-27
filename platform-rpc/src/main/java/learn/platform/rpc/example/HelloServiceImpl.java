package learn.platform.rpc.example;

import learn.platform.rpc.annotation.Provider;

@Provider(Hello.class)
public class HelloServiceImpl implements Hello {

    @Override
    public String say(String person) {
        return "Hello: " + person;
    }

}
