
package dk.jensborch.webhooks;

import javax.ws.rs.core.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

/**
 * Representation of an error message returned by the API.
 */
@Value
public class WebhookError {

    Code code;
    String msg;
    
    @AllArgsConstructor
    public enum Code {
        UNKNOWN_ERROR(Response.Status.INTERNAL_SERVER_ERROR), 
        REGISTRE_ERROR(Response.Status.BAD_REQUEST);
        
        private final @Getter Response.Status status;                
    }
}
