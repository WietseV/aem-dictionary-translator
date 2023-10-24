package be.orbinson.aem.dictionarytranslator.servlets.action;

import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.Replicator;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.distribution.Distributor;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class ReplicateDictionaryServletTest {
    private final AemContext context = new AemContext();

    @Mock
    private Distributor distributor;

    @Mock
    private Replicator replicator;

    @NotNull ReplicateDictionaryServlet servlet;

    @BeforeEach
    public void beforeEach() {
        context.registerService(Distributor.class, distributor);
        context.registerService(Replicator.class, replicator);
        servlet = context.registerInjectActivateService(new ReplicateDictionaryServlet());
    }

    @Test
    void testReplicateNonExistingDictionary() throws ServletException, IOException, ReplicationException {
        context.create().resource("/content/dictionaries1/i18n/en/apple");

        ResourceResolver resolver = spy(context.resourceResolver());

        MockSlingHttpServletRequest request = new MockSlingHttpServletRequest(resolver, context.bundleContext());
        request.setMethod("POST");
        request.setParameterMap(Map.of(
                "path", new String[]{"/content/dictionaries/i18n"}
        ));

        servlet.doPost(request, context.response());

        verify(replicator, times(0)).replicate(any(), eq(ReplicationActionType.ACTIVATE), anyString());
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, context.response().getStatus());
    }

    @Test
    void testReplicateDictionary() throws ServletException, IOException, ReplicationException {
        context.create().resource("/content/dictionaries/i18n/en/apple");
        context.create().resource("/content/dictionaries/i18n/fr/apple");
        context.create().resource("/content/dictionaries/i18n/fr/pear");
        context.create().resource("/content/dictionaries/i18n/de/pear");
        context.create().resource("/content/dictionaries2/i18n/de/apple");

        ResourceResolver resolver = spy(context.resourceResolver());

        MockSlingHttpServletRequest request = new MockSlingHttpServletRequest(resolver, context.bundleContext());
        request.setMethod("POST");
        request.setParameterMap(Map.of(
                "path", new String[]{"/content/dictionaries"}
        ));

        servlet.doPost(request, context.response());

        verify(replicator, times(9)).replicate(any(), eq(ReplicationActionType.ACTIVATE), anyString());
        assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());
    }
}