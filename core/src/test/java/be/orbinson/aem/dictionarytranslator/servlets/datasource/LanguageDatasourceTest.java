package be.orbinson.aem.dictionarytranslator.servlets.datasource;

import be.orbinson.aem.dictionarytranslator.services.impl.DictionaryServiceImpl;
import com.adobe.granite.translation.api.TranslationConfig;
import com.adobe.granite.ui.components.ds.DataSource;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.servlethelpers.MockRequestPathInfo;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class LanguageDatasourceTest {
    private final AemContext context = new AemContext();

    @Mock
    private LanguageDatasource languageDatasource;
    @Mock
    TranslationConfig translationConfig;

    @Mock
    private DictionaryServiceImpl dictionaryService;

    @BeforeEach
    public void beforeEach() {
        translationConfig = context.registerService(TranslationConfig.class, translationConfig);
    }

    @Test
    void testDoGet() {
        ResourceResolver resolver = spy(context.resourceResolver());
        context.create().resource("/content/dictionaries/site-a/i18n", Map.of("jcr:primaryType", "sling:Folder"));
        context.create().resource("/content/dictionaries/site-a/i18n/en", Map.of("jcr:language", "en", "en", "apple"));
        Resource test = context.create().resource("/content/dictionaries/site-a/i18n/en/test", Map.of("sling:resourceType", "granite/ui/components/coral/foundation/container"));
        context.create().resource("/content/dictionaries/site-a/i18n/fr", Map.of("jcr:language", "fr", "fr", "pomme"));
        context.create().resource("/content/dictionaries/site-b/i18n/de", Map.of("jcr:language", "de", "de", "apfel"));


        MockSlingHttpServletRequest request = new MockSlingHttpServletRequest(resolver, context.bundleContext());
        request.setMethod("POST");
        request.setParameterMap(Map.of(
                "labels", new String[]{"/content/dictionaries/i18n/en/appel"}
        ));
        request.setResource(test);

        //Voodoo magic - No idea how this works without a when()
        MockRequestPathInfo requestPathInfo = (MockRequestPathInfo) request.getRequestPathInfo();
        requestPathInfo.setSuffix("/content/dictionaries/site-a/i18n");

        Map<String, String> mockList = new HashMap<>();
        mockList.put("en", "apple");
        mockList.put("fr", "pomme");
        mockList.put("de", "apfel");

        when(dictionaryService.getLanguagesForPath(any(), anyString())).thenReturn(mockList);
        context.registerInjectActivateService(dictionaryService);
        languageDatasource = context.registerInjectActivateService(new LanguageDatasource());

        languageDatasource.doGet(request, context.response());
        DataSource dataSource = (DataSource) request.getAttribute("com.adobe.granite.ui.components.ds.DataSource");
        Iterator<Resource> iterator = dataSource.iterator();
        assertValueMap(iterator.next(), "de", "apfel");
        assertValueMap(iterator.next(), "en", "apple");
        assertValueMap(iterator.next(), "fr", "pomme");
    }

    private void assertValueMap(Resource resource, String expectedName, String expectedFieldLabel) {
        ValueMap vm = resource.getValueMap();
        assertEquals(expectedName, vm.get("name"));
        assertEquals(expectedFieldLabel + " (" + expectedName + ")", vm.get("fieldLabel"));
    }

}