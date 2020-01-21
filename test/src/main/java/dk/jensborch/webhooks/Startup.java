package dk.jensborch.webhooks;

import javax.enterprise.context.ApplicationScoped;

/**
 *
 */
@ApplicationScoped
public class Startup {

    /*private static final Logger LOG = LoggerFactory.getLogger(Startup.class);

    @Inject
    WebhookRegistry registry;

    public void init(@Observes final StartupEvent ev) {
        try {
            registry.registre(new Webhook(new URI("http://localhost:8080/webhooks"), "test_topics"));
        } catch (URISyntaxException ex) {
            LOG.error("Error registreing webhook");
        }
    }*/
}
