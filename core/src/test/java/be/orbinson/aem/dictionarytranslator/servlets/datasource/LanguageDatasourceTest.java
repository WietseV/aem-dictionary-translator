package be.orbinson.aem.dictionarytranslator.servlets.datasource;

import be.orbinson.aem.dictionarytranslator.services.DictionaryService;
import be.orbinson.aem.dictionarytranslator.services.impl.DictionaryServiceImpl;
import be.orbinson.aem.dictionarytranslator.servlets.datasource.BreadcrumbsDatasource;
import be.orbinson.aem.dictionarytranslator.servlets.datasource.LanguageDatasource;
import com.adobe.granite.translation.api.TranslationConfig;
import com.adobe.granite.ui.components.ds.DataSource;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.Replicator;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.distribution.Distributor;
import org.apache.sling.servlethelpers.MockRequestPathInfo;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class LanguageDatasourceTest {
    private final AemContext context = new AemContext();

    @NotNull
    private LanguageDatasource languageDatasource;
    @Mock
    TranslationConfig translationConfig;

    private DictionaryDatasource dictionaryDatasource;
    private DictionaryServiceImpl dictionaryService;

    @BeforeEach
    public void beforeEach() {
        translationConfig = context.registerService(TranslationConfig.class, translationConfig);
        dictionaryService = context.registerInjectActivateService(new DictionaryServiceImpl());
        languageDatasource = context.registerInjectActivateService(new LanguageDatasource());
    }

    @Test
    void testDoGet() {
        Resource test = context.create().resource("/content/dictionaries/i18n/en/appel");
        ResourceResolver resolver = spy(context.resourceResolver());

        MockSlingHttpServletRequest request = new MockSlingHttpServletRequest(resolver, context.bundleContext());
        request.setMethod("POST");
        request.setParameterMap(Map.of(
                "labels", new String[]{"/content/dictionaries/i18n/en/appel"}
        ));
        request.setResource(test);

        MockRequestPathInfo requestPathInfo = (MockRequestPathInfo)request.getRequestPathInfo();
        requestPathInfo.setSuffix("lekker");

        SlingHttpServletRequest slingHttpServletRequest= spy(request);

        when(slingHttpServletRequest.getRequestPathInfo()).thenReturn(requestPathInfo);

        languageDatasource.doGet(request, context.response());

    }
}