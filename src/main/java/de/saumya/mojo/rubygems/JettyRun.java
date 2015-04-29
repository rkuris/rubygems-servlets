package de.saumya.mojo.rubygems;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class JettyRun
{
    private static final Logger LOGGER = LoggerFactory.getLogger(JettyRun.class);

    private JettyRun() {
    }

    public static void main(final String[] args) throws Exception {
        final String gemsPropertyFilename;
        switch (args.length) {
            case 0:
                gemsPropertyFilename = "rubygems.properties";
                break;
            case 1:
                gemsPropertyFilename = args[0];
                break;
            default:
                throw new IllegalArgumentException("Only the rubygems property filename is allowed");
        }

        final Properties props = new Properties();
        try
        {
            props.load(new FileReader(gemsPropertyFilename));
            LOGGER.info("Loaded properties from {}", gemsPropertyFilename);
        } catch (FileNotFoundException e) {
            LOGGER.warn("Could not load properties from {}; using defaults", gemsPropertyFilename);
        } catch (final IOException e)
        {
            throw new IOException("Error reading from " + gemsPropertyFilename, e);
    	}

        final String basedir = props.getProperty( "gem.storage.base", "rubygems" );

        promoteToSystemProperty(props, "gem.caching.proxy.storage", basedir + "/caching");
        promoteToSystemProperty(props, "gem.proxy.storage", basedir + "/proxy");
        promoteToSystemProperty(props, "gem.hosted.storage", basedir + "/hosted");
        promoteToSystemProperty(props, "gem.caching.proxy.url", "https://rubygems.org");
        promoteToSystemProperty(props, "gem.proxy.url", "https://rubygems.org");

        final Server server = new Server();

        /* TODO: https support */

        /* connectors */
        final ServerConnector connector = new ServerConnector(server);
        final int port = Integer.parseInt(props.getProperty("port", "8989"));
        connector.setPort(port);
        final String host = props.getProperty("host", "localhost");
        connector.setHost(host);
        connector.setIdleTimeout(TimeUnit.HOURS.toMillis(1));
        connector.setSoLingerTime(-1);
        server.setConnectors(new Connector[]{connector});

        final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SECURITY);
        context.setContextPath("/");
        context.addServlet(new ServletHolder("caching", new RubygemsServlet()), "/caching/*");
        context.addServlet(new ServletHolder("hosted", new RubygemsServlet()), "/hosted/*");
        context.addServlet(new ServletHolder("proxy", new RubygemsServlet()), "/proxy/*");
        context.addServlet(new ServletHolder("merged", new RubygemsServlet()), "/merged/*");
        context.addServlet(new ServletHolder(new DefaultServlet()), "/*");
        context.setWelcomeFiles(new String[]{"index.html"});
        context.addEventListener(new RubygemsServletContextListener());
        server.setHandler(context);

        server.start();
        LOGGER.info("Listening on {}:{}", host, port);
        server.join();
    }
    
    private static void promoteToSystemProperty(final Properties props, final String key, final String defaultValue) {
        final String result = props.getProperty( key, defaultValue );
        System.setProperty( key, result );
    }
}
