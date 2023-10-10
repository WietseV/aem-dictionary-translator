package be.orbinson.aem.dictionarytranslator.servlets.action;

import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.Replicator;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;

@Component(service = Servlet.class)
@SlingServletResourceTypes(
        resourceSuperType = "granite/ui/components/coral/foundation/form",
        resourceTypes = "aem-dictionary-translator/servlet/action/publish-label",
        methods = "POST"
)
public class ReplicateLabelServlet extends SlingAllMethodsServlet {
    private static final Logger LOG = LoggerFactory.getLogger(ReplicateLabelServlet.class);

    @Reference
    private Replicator replicator;

    @Override
    protected void doPost(SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) throws IOException {
        String labels = request.getParameter("labels");

        if (StringUtils.isEmpty(labels)) {
            LOG.warn("Labels parameters are required");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } else {
            for (String label : labels.split(",")) {
                LOG.info("label: " + label);

                //Splitting label into dictionary path and label key
                label = label.replace("/mnt/dictionary", "");
                int lastIndexOfBackslash = label.lastIndexOf('/');
                String parentPath = "";
                if (lastIndexOfBackslash != -1) {
                    parentPath = label.substring(0, lastIndexOfBackslash);
                    LOG.info("Parent path: " + parentPath);
                    label = label.substring(lastIndexOfBackslash + 1);
                    LOG.info("Label: " + label);
                }
                ResourceResolver resourceResolver = getResourceResolver(request);
                String query = "/jcr:root" + parentPath + "//element(*, mix:language)/" + label;
                LOG.info(query);
                Iterator<Resource> a = resourceResolver.findResources(query, "xpath");
                a.forEachRemaining(
                        resource -> {
                            try {
                                replicator.replicate(resourceResolver.adaptTo(Session.class), ReplicationActionType.ACTIVATE, resource.getPath());
                                LOG.debug("Published label on path '{}'", resource.getPath());
                            } catch (ReplicationException e) {
                                throw new RuntimeException(e);
                            }
                        }
                );
                LOG.warn("Unable to get label '{}'", labels);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }

    // Wrapper method to mock resourceResolver
    @NotNull
    ResourceResolver getResourceResolver(SlingHttpServletRequest request) {
        return request.getResourceResolver();
    }

}
