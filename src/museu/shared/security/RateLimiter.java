package museu.shared.security;

public interface RateLimiter {

    boolean tryAcquire(String clientKey);
}
