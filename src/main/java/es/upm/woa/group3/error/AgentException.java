package es.upm.woa.group3.error;

public class AgentException extends RuntimeException {
    public AgentException() {
    }

    public AgentException(String s) {
        super(s);
    }

    public AgentException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public AgentException(Throwable throwable) {
        super(throwable);
    }

    public AgentException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
