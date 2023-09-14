package webmvc.org.springframework.web.servlet.mvc.tobe;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import webmvc.org.springframework.web.servlet.ModelAndView;

public class HandlerExecution {

    private final Method method;

    public HandlerExecution(final Method method) {
        this.method = method;
    }

    final ModelAndView handle(final HttpServletRequest request, final HttpServletResponse response)
        throws Exception {
        final Object object = method.getDeclaringClass().getConstructor().newInstance();
        return (ModelAndView) method.invoke(object, request, response);
    }
}
