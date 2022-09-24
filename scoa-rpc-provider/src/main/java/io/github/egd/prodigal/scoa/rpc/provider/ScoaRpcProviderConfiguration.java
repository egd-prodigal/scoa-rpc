package io.github.egd.prodigal.scoa.rpc.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

public class ScoaRpcProviderConfiguration {

    @Bean
    public ScoaRpcDispatchServlet scoaRpcDispatchServlet() {
        return new ScoaRpcDispatchServlet();
    }

    @Bean
    public ServletRegistrationBean<ScoaRpcDispatchServlet> scoaRpcDispatchServletServletRegistrationBean(
            @Autowired ScoaRpcDispatchServlet scoaRpcDispatchServlet) {
        ServletRegistrationBean<ScoaRpcDispatchServlet> registrationBean = new ServletRegistrationBean<>();
        registrationBean.setServlet(scoaRpcDispatchServlet);
        registrationBean.addUrlMappings("/scoa-rpc/provider");
        return registrationBean;
    }

}
