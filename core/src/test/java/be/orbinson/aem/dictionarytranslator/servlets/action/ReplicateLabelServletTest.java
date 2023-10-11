package be.orbinson.aem.dictionarytranslator.servlets.action;

import be.orbinson.aem.dictionarytranslator.services.DictionaryService;
import be.orbinson.aem.dictionarytranslator.services.impl.DictionaryServiceImpl;
import com.adobe.granite.translation.api.TranslationConfig;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.Replicator;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class ReplicateLabelServletTest {

    private final AemContext context = new AemContext(ResourceResolverType.JCR_MOCK);

    @NotNull ReplicateLabelServlet servlet;

    @NotNull CreateLabelServlet create;

    DictionaryService dictionaryService;

    @Mock
    TranslationConfig translationConfig;

    @Mock
    Replicator replicator;

    @Mock
    ResourceResolver resourceResolver;



    @BeforeEach
    void beforeEach() {
        translationConfig = context.registerService(TranslationConfig.class, translationConfig);
        dictionaryService = context.registerInjectActivateService(new DictionaryServiceImpl());
        replicator = context.registerService(Replicator.class, replicator);
        resourceResolver = context.registerService(ResourceResolver.class, resourceResolver);
        servlet = context.registerInjectActivateService(new ReplicateLabelServlet());
        create = context.registerInjectActivateService(new CreateLabelServlet());
    }

    @Test
    void doPostWithoutParams() throws ServletException, IOException {
        context.request().setMethod("POST");
        context.request().setParameterMap(Map.of());

        servlet.service(context.request(), context.response());

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, context.response().getStatus());
    }

    @Test
    void publishLabelWithNonExistingKey() throws ServletException, IOException {
        context.request().setMethod("POST");
        context.request().setParameterMap(Map.of(
                "labels", "/content/dictionaries/i18n/appel"
        ));

        servlet.service(context.request(), context.response());

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, context.response().getStatus());
    }

    @Test
    @Disabled
    void publishExistingLabel() throws ServletException, IOException, ReplicationException {
        Resource test = context.create().resource("/content/dictionaries/i18n/en/appel");
//        context.create().resource("/content/dictionaries/i18n/peer");
        context.request().setMethod("POST");
        context.request().setParameterMap(Map.of(
                "labels", new String[]{"/content/dictionaries/i18n/en/appel"}
        ));

        //create mock iterator of query result
//        Resource test = context.create().resource("/content/dictionaries/i18n/appel");
        List<Resource> resources = new ArrayList<>();
        resources.add(test);
        Iterator<Resource> iterator = resources.iterator();

        SlingHttpServletRequest request = Mockito.spy(context.request());
        when(servlet.getResourceResolver(request)).thenReturn(resourceResolver);

//        when(resourceResolver.findResources(anyString(),anyString())).thenReturn(iterator);
        when(servlet.getResources(resourceResolver, anyString(), anyString())).thenReturn(iterator);

        servlet.service(context.request(), context.response());

        // Verify that the labels were published
        verify(replicator, times(1)).replicate(any(), eq(ReplicationActionType.ACTIVATE), anyString());
        assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());
    }

    @Test
    @Disabled
    void publishMultipleLabels() throws ServletException, IOException, ReplicationException {
        // Creating resources
        context.create().resource("/content/dictionaries/i18n/en/appel");
        context.create().resource("/content/dictionaries/i18n/en/peer");
        context.create().resource("/content/dictionaries/i18n/en/framboos");

        // Setting up the request
        context.request().setMethod("POST");
        context.request().setParameterMap(Map.of(
                "labels", new String[]{"/content/dictionaries/i18n/appel,/content/dictionaries/i18n/peer"}
        ));

        // Invoking the servlet
        servlet.service(context.request(), context.response());

        // Verify that the labels were published twice
        verify(replicator, times(2)).replicate(any(), eq(ReplicationActionType.ACTIVATE), anyString());

        // Asserting the HTTP response status code
        assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());
    }

    @Test
    void publishNonExistingLabel() throws ServletException, IOException, ReplicationException {
        context.create().resource("/content/dictionaries/i18n/en/appel");
        context.request().setMethod("POST");
        context.request().setParameterMap(Map.of(
                "labels", new String[]{"/content/dictionaries/i18n/peer"}
        ));

        servlet.service(context.request(), context.response());

        // Verify that the labels were published
        verify(replicator, times(0)).replicate(any(), any(), anyString());

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, context.response().getStatus());
    }

}