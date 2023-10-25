package be.orbinson.aem.dictionarytranslator.servlets.datasource;

import com.adobe.granite.translation.api.TranslationConfig;
import com.adobe.granite.ui.components.ds.DataSource;

import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.servlet.MockRequestPathInfo;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import be.orbinson.aem.dictionarytranslator.services.LabelResourceProvider;
import be.orbinson.aem.dictionarytranslator.services.impl.DictionaryServiceImpl;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class LabelDatasourceTest {

    @Mock
    TranslationConfig translationConfig;

    @Mock
    RequestPathInfo requestPathInfo;

    private final AemContext context = new AemContext();

    private LabelDatasource labelDatasource;
    private DictionaryServiceImpl dictionaryService;

    private LabelResourceProvider labelResourceProvider;

    @BeforeEach
    public void setUp() {
        translationConfig = context.registerService(TranslationConfig.class, translationConfig);
        dictionaryService = context.registerInjectActivateService(new DictionaryServiceImpl());
        labelDatasource = context.registerInjectActivateService(new LabelDatasource());
        labelResourceProvider = context.registerInjectActivateService(new LabelResourceProvider());
        requestPathInfo = context.registerService(RequestPathInfo.class, requestPathInfo);
    }

    @Test
    void testDoGet() {
        String[] languages = {"en", "fr", "es", "nl", "de"};
        Resource dictionaryResource = context.create().resource("/content/dictionaries/site-a/i18n/apple", Map.of("languages", languages, "key", "apple", "en", "apple", "fr", "pomme", "es", "manzana", "nl", "appel", "de", "apfel"));
        context.create().resource("/content/dictionaries/site-a/i18n/en", Map.of("jcr:language", "en"));
        context.create().resource("/content/dictionaries/site-a/i18n/fr", Map.of("jcr:language", "fr"));
        context.create().resource("/content/dictionaries/site-a/i18n/es", Map.of("jcr:language", "es"));
        context.create().resource("/content/dictionaries/site-a/i18n/nl", Map.of("jcr:language", "nl"));
        context.create().resource("/content/dictionaries/site-a/i18n/de", Map.of("jcr:language", "de"));
        context.create().resource("/content/dictionaries/site-a/i18n/fr/apple", Map.of("jcr:primaryType", "sling:MessageEntry", "key", "apple", "fr", "pomme"));

        MockSlingHttpServletRequest request = new MockSlingHttpServletRequest(context.resourceResolver(), context.bundleContext());
        request.setMethod("GET");
        request.setParameterMap(Map.of("label", "/content/dictionaries/site-a/i18n/apple"));
        request.setResource(dictionaryResource);
        MockRequestPathInfo pathInfo = (MockRequestPathInfo) request.getRequestPathInfo();
        pathInfo.setSuffix("/content/dictionaries/site-a/i18n");

        labelDatasource.doGet(request, context.response());
        DataSource dataSource = (DataSource) request.getAttribute(DataSource.class.getName());
        assertNotNull(dataSource);
        Iterator<Resource> resourceIterator = dataSource.iterator();
        List<String> values = new ArrayList<>();
        while (resourceIterator.hasNext()) {
            Resource resource = resourceIterator.next();
            Map<String, Object> dictionaryData = resource.getValueMap();
            if (dictionaryData.get("path") != null) {
                values.add(dictionaryData.get("path").toString());
            } else {
                values.add(Arrays.toString(new Object[]{dictionaryData.get("name"), dictionaryData.get("value")}));
            }
        }
        assertEquals(7, values.size());
        assertTrue(values.contains("/mnt/dictionary/content/dictionaries/site-a/i18n/apple"));
        assertTrue(values.contains("[en, apple]"));
        assertTrue(values.contains("[fr, pomme]"));
        assertTrue(values.contains("[es, manzana]"));
        assertTrue(values.contains("[nl, appel]"));
        assertTrue(values.contains("[Label, apple]"));
        assertTrue(values.contains("[de, apfel]"));
    }
}