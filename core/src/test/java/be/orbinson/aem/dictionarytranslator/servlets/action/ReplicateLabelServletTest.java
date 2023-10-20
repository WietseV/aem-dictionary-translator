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

import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
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
import javax.servlet.ServletRequest;
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

    private final AemContext context = new AemContext();

    @NotNull ReplicateLabelServlet servlet;

    @NotNull CreateLabelServlet create;

    @Mock
    TranslationConfig translationConfig;

    @Mock
    Replicator replicator;

    @BeforeEach
    void beforeEach() {
        translationConfig = context.registerService(TranslationConfig.class, translationConfig);
        context.registerInjectActivateService(new DictionaryServiceImpl());
        replicator = context.registerService(Replicator.class, replicator);
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
    void publishExistingLabel() throws ServletException, IOException, ReplicationException {
        Resource test = context.create().resource("/content/dictionaries/i18n/en/appel");

        List<Resource> resources = new ArrayList<>();
        resources.add(test);
        Iterator<Resource> iterator = resources.iterator();

        ResourceResolver resolver = spy(context.resourceResolver());
        when(resolver.findResources(anyString(), anyString())).thenReturn(iterator);

        MockSlingHttpServletRequest request = new MockSlingHttpServletRequest(resolver, context.bundleContext());
        request.setMethod("POST");
        request.setParameterMap(Map.of(
                "labels", new String[]{"/content/dictionaries/i18n/en/appel"}
        ));

        servlet.service(request, context.response());
        verify(replicator, times(1)).replicate(any(), eq(ReplicationActionType.ACTIVATE), anyString());
        assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());
    }

    @Test
    void publishMultipleLabels() throws ServletException, IOException, ReplicationException {
        Resource test = context.create().resource("/content/dictionaries/i18n/en/appel");
        Resource test2 = context.create().resource("/content/dictionaries/i18n/en/peer");
        Resource test3 = context.create().resource("/content/dictionaries/i18n/en/framboos");

        List<Resource> resources = new ArrayList<>();
        resources.add(test);
        resources.add(test2);
        resources.add(test3);
        Iterator<Resource> iterator = resources.iterator();

        ResourceResolver resolver = spy(context.resourceResolver());
        when(resolver.findResources(anyString(), anyString())).thenReturn(iterator);

        MockSlingHttpServletRequest request = new MockSlingHttpServletRequest(resolver, context.bundleContext());
        request.setMethod("POST");
        request.setParameterMap(Map.of(
                "labels", new String[]{"/content/dictionaries/i18n/en/appel", "/content/dictionaries/i18n/en/peer", "/content/dictionaries/i18n/en/framboos"}
        ));

        servlet.service(request, context.response());
        verify(replicator, times(3)).replicate(any(), eq(ReplicationActionType.ACTIVATE), anyString());
        assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());
    }

    @Test
    void publishNonExistingLabel() throws ServletException, IOException, ReplicationException {
        context.create().resource("/content/dictionaries/i18n/en/appel");

        MockSlingHttpServletRequest request = new MockSlingHttpServletRequest(context.resourceResolver(), context.bundleContext());
        request.setMethod("POST");
        request.setParameterMap(Map.of(
                "labels", new String[]{"/content/dictionaries/i18n/en/peer"}
        ));

        servlet.service(request, context.response());
        verify(replicator, times(0)).replicate(any(), any(), anyString());
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, context.response().getStatus());
    }

    @Test
    void publishLabelWithWrongPath() throws ServletException, IOException, ReplicationException {
        context.create().resource("/content/dictionaries/i18n/en/appel");

        MockSlingHttpServletRequest request = new MockSlingHttpServletRequest(context.resourceResolver(), context.bundleContext());
        request.setMethod("POST");
        request.setParameterMap(Map.of(
                "labels", new String[]{"peer"}
        ));

        servlet.service(request, context.response());
        verify(replicator, times(0)).replicate(any(), any(), anyString());
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, context.response().getStatus());
    }

}