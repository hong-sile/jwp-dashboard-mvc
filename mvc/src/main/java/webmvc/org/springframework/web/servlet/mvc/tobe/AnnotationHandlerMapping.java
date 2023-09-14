package webmvc.org.springframework.web.servlet.mvc.tobe;

import static java.util.Map.entry;

import context.org.springframework.stereotype.Controller;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.org.springframework.web.bind.annotation.RequestMapping;
import web.org.springframework.web.bind.annotation.RequestMethod;

public class AnnotationHandlerMapping {

    private static final Logger log = LoggerFactory.getLogger(AnnotationHandlerMapping.class);

    private final Object[] basePackage;
    private final Map<HandlerKey, HandlerExecution> handlerExecutions;

    public AnnotationHandlerMapping(final Object... basePackage) {
        this.basePackage = basePackage;
        this.handlerExecutions = new HashMap<>();
    }

    public void initialize() {
        log.info("Initialized AnnotationHandlerMapping!");

        final Map<HandlerKey, HandlerExecution> handlerExecutions = new Reflections(basePackage)
            .getTypesAnnotatedWith(Controller.class)
            .stream()
            .map(Class::getMethods)
            .flatMap(Arrays::stream)
            .filter(method -> method.isAnnotationPresent(RequestMapping.class))
            .map(method -> entry(method, method.getAnnotation(RequestMapping.class)))
            .flatMap(this::mapExecutions)
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        this.handlerExecutions.putAll(handlerExecutions);
    }

    private Stream<Entry<HandlerKey, HandlerExecution>> mapExecutions(
        final Entry<Method, RequestMapping> entry
    ) {
        final Method handleTo = entry.getKey();
        final RequestMapping requestMapping = entry.getValue();

        final String path = requestMapping.value();
        final RequestMethod[] methods = requestMapping.method();

        return Arrays.stream(methods)
            .map(method -> new HandlerKey(path, method))
            .map(key -> entry(key, new HandlerExecution(handleTo)));
    }

    public Object getHandler(final HttpServletRequest request) {
        final String servletPath = request.getRequestURI();
        final RequestMethod requestMethod = RequestMethod.valueOf(request.getMethod());

        final HandlerKey handlerKey = new HandlerKey(servletPath, requestMethod);

        return handlerExecutions.get(handlerKey);
    }
}
