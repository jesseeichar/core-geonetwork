package org.fao.geonet.services.metadata.format;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import net.sf.json.xml.JSONTypes;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Params;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.services.metadata.format.function.FormatterFunction;
import org.fao.geonet.services.metadata.format.function.FormatterFunctionRepository;
import org.jdom.CDATA;
import org.jdom.Element;

import java.io.IOException;
import java.util.Arrays;

/**
 * Add a formatter widget library.  A widget library is an xslt file containing only functions.  The libraries are not permitted to
 * have anything other function declarations.
 * 
 * Created by Jesse on 3/15/14.
 */
public class FormatterFunctionManagerService implements Service {
    public static final String PARAM_NAMESPACE = "namespace";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_FUNCTION = "functionBody";
    private FormatterFunctionServiceAction _action;

    @Override
    public void init(String appPath, ServiceConfig params) throws Exception {

        this._action = FormatterFunctionServiceAction.lookup(params);

    }

    public static enum FormatterFunctionServiceAction {
        SET, DELETE, LIST;

        public static FormatterFunctionServiceAction lookup(ServiceConfig params) {
            String action = params.getMandatoryValue(Params.ACTION);
            for (FormatterFunctionServiceAction serviceAction : values()) {
                if (action.equalsIgnoreCase(serviceAction.name())) {
                    return serviceAction;
                }
            }
            throw new BadParameterEx(Params.ACTION, action);
        }
    }

    @Override
    public Element exec(Element params, ServiceContext context) throws Exception {
        switch (_action) {
            case SET:
                return set(params, context);
            case DELETE:
                return delete(params, context);
            case LIST:
                return list(params, context);
            default:
                throw new Error("Action needs to be handled: "+_action);
        }
    }

    private Element list(Element params, ServiceContext context) throws IOException {
        final Element root = new Element("formatterFunctions");
        final FormatterFunctionRepository repository = context.getBean(FormatterFunctionRepository.class);
        for (String namespace : repository.findAllNamespaces()) {
            Element singleNamespaceEl = new Element(namespace);
            singleNamespaceEl.addContent(new Element("name").setText(namespace));
            root.addContent(singleNamespaceEl);
            Element functions = new Element("functions");
            singleNamespaceEl.addContent(functions);
            for (FormatterFunction function : repository.findAllByNamespace(namespace)) {
                Element singleFunctionEl = new Element("formatterFunction");
                functions.addContent(singleFunctionEl);
                singleFunctionEl.addContent(Arrays.asList(
                        new Element("namespace").setText(function.getNamespace()),
                        new Element("name").setText(function.getName()),
                        new Element("functionBody").addContent(new CDATA(function.getFunction()))
                ));
            }
        }
        return root;
    }

    private Element delete(Element params, ServiceContext context) throws IOException {
        String namespace = Util.getParam(params, PARAM_NAMESPACE);
        String name = Util.getParam(params, PARAM_NAME);

        context.getBean(FormatterFunctionRepository.class).delete(namespace, name);

        return new Element("ok");
    }

    private Element set(Element params, ServiceContext context) throws IOException {
        String namespace = Util.getParam(params, PARAM_NAMESPACE);
        String name = Util.getParam(params, PARAM_NAME);
        String function = Util.getParam(params, PARAM_FUNCTION);

        FormatterFunction functionObj = new FormatterFunction(namespace, name, function);
        context.getBean(FormatterFunctionRepository.class).save(functionObj);

        return new Element("ok");
    }
}
