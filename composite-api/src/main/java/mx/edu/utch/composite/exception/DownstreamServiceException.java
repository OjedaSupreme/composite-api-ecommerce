package mx.edu.utch.composite.exception;

/**
 * Un microservicio o API externa no respondio (caido, timeout o error 5xx).
 */
public class DownstreamServiceException extends RuntimeException {

    private final String serviceName;

    public DownstreamServiceException(String serviceName, String message, Throwable cause) {
        super(message, cause);
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
