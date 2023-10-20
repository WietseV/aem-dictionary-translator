package be.orbinson.aem.dictionarytranslator.servlets.datasource;

import com.adobe.granite.ui.components.ds.DataSource;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class BreadcrumbsDatasourceTest {

    private final AemContext context = new AemContext();

    @Test
    void testDoGet() {
        BreadcrumbsDatasource breadcrumbsDatasource = new BreadcrumbsDatasource();

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
        context.create().resource(dictionaryResource, "es",
                "sling:primaryType", "sling:Folder",
                "jcr:mixinTypes", "mix:language",
                "jcr:language", "es"
        );

        breadcrumbsDatasource.doGet(context.request(), context.response());

        DataSource dataSource = (DataSource) context.request().getAttribute(DataSource.class.getName());
        assertNotNull(dataSource);
        Iterator<Resource> resourceIterator = dataSource.iterator();
        List<String> values = new ArrayList<>();
        while (resourceIterator.hasNext()) {
            Resource resource = resourceIterator.next();
            ValueMap valueMap = resource.getValueMap();
            values.add(valueMap.get("value", String.class));
        }
        assertEquals(2, values.size());
    }
}
