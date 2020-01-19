package dk.jensborch.webhooks;

import javax.enterprise.context.ApplicationScoped;

/**
 *
 */
@ApplicationScoped
public class Startup {

    /*
    private static final Logger LOG = LoggerFactory.getLogger(Startup.class);

    @Inject
    private WebhookRegistry registry;

    public void init(@Observes @Initialized(ApplicationScoped.class) final Object init) {
        try {
            registry.registre(new Webhook(new URI("http://localhost/webhooks"), "test_topics"));
        } catch (URISyntaxException ex) {
            LOG.error("Error registreing webhook");
        }
    }*/
}
