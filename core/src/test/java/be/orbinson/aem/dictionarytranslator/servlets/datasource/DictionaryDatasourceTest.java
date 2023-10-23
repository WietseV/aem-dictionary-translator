package be.orbinson.aem.dictionarytranslator.servlets.datasource;

import com.adobe.granite.translation.api.TranslationConfig;
import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.ValueMapResource;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.spi.resource.provider.ResolveContext;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import be.orbinson.aem.dictionarytranslator.services.DictionaryService;
import be.orbinson.aem.dictionarytranslator.services.LabelResourceProvider;
import be.orbinson.aem.dictionarytranslator.services.impl.DictionaryServiceImpl;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class DictionaryDatasourceTest {

    @Mock
    TranslationConfig translationConfig;

    private final AemContext context = new AemContext();
    private DictionaryDatasource dictionaryDatasource;
    private DictionaryServiceImpl dictionaryService;

    @BeforeEach
    public void setUp() {
        translationConfig = context.registerService(TranslationConfig.class, translationConfig);
        dictionaryService = context.registerInjectActivateService(new DictionaryServiceImpl());
        dictionaryDatasource = context.registerInjectActivateService(new DictionaryDatasource());
    }

    @Test
    void testDoGet() {
        context.create().resource("/content/dictionaries/site-a/i18n/en", Map.of("jcr:language", "en"));
        context.create().resource("/content/dictionaries/site-a/i18n/fr", Map.of("jcr:language", "fr"));
        context.create().resource("/content/dictionaries/site-b/i18n/de", Map.of("jcr:language", "de"));

        ResourceResolver resourceResolver = spy(context.resourceResolver());
        doReturn(
                List.of(
                        context.resourceResolver().getResource("/content/dictionaries/site-a/i18n/en"),
                        context.resourceResolver().getResource("/content/dictionaries/site-a/i18n/fr"),
                        context.resourceResolver().getResource("/content/dictionaries/site-b/i18n/de")
                ).iterator()
        ).when(resourceResolver).findResources(anyString(), anyString());

        MockSlingHttpServletRequest request = new MockSlingHttpServletRequest(resourceResolver, context.bundleContext());
        request.setMethod("POST");

        dictionaryDatasource.doGet(request, context.response());
        DataSource dataSource = (DataSource) request.getAttribute(DataSource.class.getName());
        assertNotNull(dataSource);
        Iterator<Resource> resourceIterator = dataSource.iterator();
        List<String> values = new ArrayList<>();
        while (resourceIterator.hasNext()) {
            Resource resource = resourceIterator.next();
            Map<String, Object> dictionaryData = resource.getValueMap();
            values.add(dictionaryData.values().toString());
        }
        assertEquals(2, values.size());
        assertTrue(values.contains("[de]"));
        assertTrue(values.contains("[fr]"));
    }

}