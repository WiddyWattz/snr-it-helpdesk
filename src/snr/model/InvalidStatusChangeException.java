package snr.model;

public class InvalidStatusChangeException extends HelpdeskException {
    public InvalidStatusChangeException(Status from, Status to) {
        super("Cannot change status from " + from + " to " + to + ".");
    }
}
