package dk.jensborch.webhooks;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

/**
 * Client request filter for adding basic auth headers.
 */
public class BasicAuthClientRequestFilter implements ClientRequestFilter {

    private final String user;
    private final String password;

    public BasicAuthClientRequestFilter(final String user, final String password) {
        this.user = user;
        this.password = password;
    }

    @Override
    public void filter(final ClientRequestContext crc) throws IOException {
        crc.getHeaders().add(
                "Authorization",
                encodeCredentials(user, password)
        );
    }

    public static String encodeCredentials(final String user, final String password) throws UnsupportedEncodingException {
        return "BASIC " + Base64.getEncoder().encodeToString((user + ":" + password).getBytes("UTF-8"));
    }

}
