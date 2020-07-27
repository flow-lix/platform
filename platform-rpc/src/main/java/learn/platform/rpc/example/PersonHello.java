package learn.platform.rpc.example;

public class PersonHello implements Hello {

    @Override
    public String say(String person) {
        return "Hello " + person;
    }
}
