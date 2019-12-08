package es.upm.woa.group3.error;

public class ContentNotOfTypeActionException extends AgentException {
    public ContentNotOfTypeActionException() {
    }

    public ContentNotOfTypeActionException(String s) {
        super(s);
    }

    public ContentNotOfTypeActionException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ContentNotOfTypeActionException(Throwable throwable) {
        super(throwable);
    }

    public ContentNotOfTypeActionException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
