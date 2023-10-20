package be.orbinson.aem.dictionarytranslator.services;

import com.adobe.granite.translation.api.TranslationConfig;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.spi.resource.provider.ResolveContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import be.orbinson.aem.dictionarytranslator.services.impl.DictionaryServiceImpl;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class LabelResourceProviderTest {

    @Mock
    TranslationConfig translationConfig;

    @Mock
    ResolveContext<Object> resolveContext;

    private final AemContext context = new AemContext();

    private LabelResourceProvider labelResourceProvider;

    @BeforeEach
    public void setUp() {
        translationConfig = context.registerService(TranslationConfig.class, translationConfig);
        context.registerInjectActivateService(new DictionaryServiceImpl());
        labelResourceProvider = context.registerInjectActivateService(new LabelResourceProvider());
    }

    @Test
    void testGetLabelResource() {
        Resource dictionaryResource = context.create().resource("/content/dictionaries/i18n");
        Resource languageResource = context.create().resource(dictionaryResource, "en",
                "sling:primaryType", "sling:Folder",
                "jcr:mixinTypes", "mix:language",
                "jcr:language", "en"
        );
        context.create().resource(languageResource, "apple",
                "jcr:primaryType", "sling:MessageEntry",
                "sling:key", "apple",
                "sling:message", "Apple"
        );

        Resource labelResource = context.resourceResolver().getResource("/mnt/dictionary/content/dictionaries/i18n/apple");
        assertFalse(ResourceUtil.isNonExistingResource(labelResource));
    }

    @Test
    void testDeleteLabel() throws PersistenceException {
        Resource dictionaryResource = context.create().resource("/content/dictionaries/i18n");
        Resource languageResource = context.create().resource(dictionaryResource, "en",
                "sling:primaryType", "sling:Folder",
                "jcr:mixinTypes", "mix:language",
                "jcr:language", "en"
        );
        context.create().resource(languageResource, "apple",
                "jcr:primaryType", "sling:MessageEntry",
                "sling:key", "apple",
                "sling:message", "Apple"
        );

        Resource labelResource = context.resourceResolver().getResource("/mnt/dictionary/content/dictionaries/i18n/apple");

        when(resolveContext.getResourceResolver()).thenReturn(context.resourceResolver());

        labelResourceProvider.delete(resolveContext, labelResource);
        assertNull(context.resourceResolver().getResource("/content/dictionaries/i18n/en/apple"));
    }
}