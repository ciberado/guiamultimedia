/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ciberado.botserver.util;

import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

/**
 *
 * @author ciberado
 */
//@Component("appProperties")
public class ApplicationProperties extends Properties {

    @Inject
    private WebApplicationContext context;

    public ApplicationProperties() {
        super(System.getProperties());
    }

    @PostConstruct
    public void initialize() {
        String appFolder = (context == null) ? "/" : context.getServletContext().getRealPath("/");
        super.setProperty("appFolder", appFolder);
        super.setProperty("java.io.tmpdir", System.getProperty("java.io.tmpdir"));
    }


}
