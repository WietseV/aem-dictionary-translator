package be.orbinson.aem.dictionarytranslator.servlets.datasource;

import com.adobe.granite.translation.api.TranslationConfig;
import com.adobe.granite.ui.components.ds.DataSource;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class AvailableLanguageDatasourceTest {

    @Mock
    private TranslationConfig translationConfig;

    private final AemContext context = new AemContext();

    @BeforeEach
    public void setUp() {
        translationConfig = context.registerService(TranslationConfig.class, translationConfig);
    }

    @Test
    void testSortByValue() {
        Map<String, String> unsortedMap = Map.of("b", "Beta", "a", "Alpha", "c", "Charlie");
        Map<String, String> sortedMap = AvailableLanguageDatasource.sortByValue(unsortedMap);

        assertEquals("Alpha", sortedMap.values().toArray()[0]);
        assertEquals("Beta", sortedMap.values().toArray()[1]);
        assertEquals("Charlie", sortedMap.values().toArray()[2]);
    }

    @Test
    void testFilterExistingLanguages() {
        Resource dictionaryResource = context.create().resource("/content/dictionaries/i18n");
        context.create().resource(dictionaryResource, "en",
                "sling:primaryType", "sling:Folder",
                "jcr:mixinTypes", "mix:language",
                "jcr:language", "en"
        );
        context.create().resource(dictionaryResource, "fr",
                "sling:primaryType", "sling:Folder",
                "jcr:mixinTypes", "mix:language",
                "jcr:language", "fr"
        );
        Map<String, String> languageMap = new HashMap<>(Map.of("en", "English", "es", "Spanish", "fr", "French"));
        ResourceResolver resourceResolver = context.resourceResolver();
        String languagePath = "/content/dictionaries/i18n";


        AvailableLanguageDatasource.filterExistingLanguages(languageMap, resourceResolver, languagePath);

        assertEquals(1, languageMap.size());
        assertTrue(languageMap.containsKey("es"));
        assertFalse(languageMap.containsKey("en"));
        assertFalse(languageMap.containsKey("fr"));
    }

    @Test
    void testDoGet() {
        AvailableLanguageDatasource availableLanguageDatasource = new AvailableLanguageDatasource();
        availableLanguageDatasource.translationConfig = translationConfig;
        when(translationConfig.getLanguages(context.resourceResolver())).thenReturn(Map.of("en", "English", "es", "Spanish", "fr", "French"));

        availableLanguageDatasource.doGet(context.request(), context.response());

        DataSource dataSource = (DataSource) context.request().getAttribute(DataSource.class.getName());
        assertNotNull(dataSource);
        Iterator<Resource> resourceIterator = dataSource.iterator();
        List<String> values = new ArrayList<>();
        while (resourceIterator.hasNext()) {
            Resource resource = resourceIterator.next();
            ValueMap valueMap = resource.getValueMap();
            values.add(valueMap.get("value", String.class));
        }
        assertEquals(3, values.size());
        assertTrue(values.contains("en"));
        assertTrue(values.contains("es"));
        assertTrue(values.contains("fr"));
    }
}
