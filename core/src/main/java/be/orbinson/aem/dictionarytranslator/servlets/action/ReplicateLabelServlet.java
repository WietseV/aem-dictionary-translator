package be.orbinson.aem.dictionarytranslator.servlets.action;

import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.Replicator;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;

import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServletResponse;

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
        ResourceResolver resolver = request.getResourceResolver();

        if (StringUtils.isEmpty(labels)) {
            LOG.warn("Labels parameters are required");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } else {
            for (String label : labels.split(",")) {
                //Splitting label into dictionary path and label key
                label = label.replace("/mnt/dictionary", "");
                int lastIndexOfBackslash = label.lastIndexOf('/');
                String parentPath = "";
                if (lastIndexOfBackslash != -1) {
                    parentPath = label.substring(0, lastIndexOfBackslash);
                    label = label.substring(lastIndexOfBackslash + 1);


                    Iterator<Resource> iterator = resolver.findResources("/jcr:root" + parentPath + "//element(*, mix:language)/" + label, "xpath");
                    if(!iterator.hasNext()){
                        LOG.warn("Unable to publish label '{}'", label);
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    }
                    iterator.forEachRemaining(
                            resource -> {
                                try {
                                    replicator.replicate(resolver.adaptTo(Session.class), ReplicationActionType.ACTIVATE, resource.getPath());
                                    LOG.debug("Published label on path '{}'", resource.getPath());
                                } catch (ReplicationException e) {
                                    LOG.warn("ReplicationException occurred when trying to replicate servlet in ReplicateDictionaryServlet");
                                }
                            }
                    );
                } else {
                    LOG.warn("Wrong label path '{}'", label);
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                }
            }
        }
    }
}
