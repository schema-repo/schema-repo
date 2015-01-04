/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.schemarepo.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServlet;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.schemarepo.Repository;
import org.schemarepo.config.Config;
import org.schemarepo.config.ConfigModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.servlet.GuiceFilter;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

/**
 * A {@link RepositoryServer} is a stand-alone server for running a
 * {@link RESTRepository}. {@link #main(String...)} takes a single argument
 * containing a property file for configuration. <br/>
 * <br/>
 *
 */
public class RepositoryServer {
  private final Server server;

  /**
   * Constructs an instance of this class, overlaying the default properties
   * with any identically-named properties in the supplied {@link Properties}
   * instance.
   *
   * @param props
   *          Property values for overriding the defaults.
   *          <p>
   *          <b><i>Any overriding properties must be supplied as type </i>
   *          <code>String</code><i> or they will not work and the default
   *          values will be used.</i></b>
   *
   */
  public RepositoryServer(Properties props) {
    final Logger logger = LoggerFactory.getLogger(getClass());
    final String julToSlf4jDep = "jul-to-slf4j dependency";
    final String julPropName = Config.LOGGING_ROUTE_JUL_TO_SLF4J;
    if (Boolean.parseBoolean(props.getProperty(julPropName, Config.getDefault(julPropName)))) {
      final String slf4jBridgeHandlerName = "org.slf4j.bridge.SLF4JBridgeHandler";
      try {
        final Class<?> slf4jBridgeHandler = Class.forName(slf4jBridgeHandlerName, true,
            Thread.currentThread().getContextClassLoader());
        slf4jBridgeHandler.getMethod("removeHandlersForRootLogger").invoke(null);
        slf4jBridgeHandler.getMethod("install").invoke(null);
        logger.info("Routing java.util.logging traffic through SLF4J");
      } catch (Exception e) {
        logger.error(
            "Failed to install {}, java.util.logging is unaffected. Perhaps you need to add {}",
            slf4jBridgeHandlerName, julToSlf4jDep, e);
      }
    } else {
      logger.info(
          "java.util.logging is NOT routed through SLF4J. Set {} property to true and add {} if you want otherwise",
          julPropName, julToSlf4jDep);
    }

    Injector injector = Guice.createInjector(new ConfigModule(props), new ServerModule());
    this.server = injector.getInstance(Server.class);
  }

  public static void main(String... args) throws Exception {
    if (args.length != 1) {
      printHelp();
      System.exit(1);
    }
    File config = new File(args[0]);
    if (!config.canRead()) {
      System.err.println("Cannot read file: " + config);
      printHelp();
      System.exit(1);
    }
    Properties props = new Properties();
    props.load(new BufferedInputStream(new FileInputStream(config)));
    RepositoryServer server = new RepositoryServer(props);
    try {
      server.start();
      server.join();
    } finally {
      server.stop();
    }
  }

  public void start() throws Exception {
    server.start();
  }

  public void join() throws InterruptedException {
    server.join();
  }

  public void stop() throws Exception {
    server.stop();
  }

  private static void printHelp() {
    System.err.println("One argument expected containing a configuration "
        + "properties file.  Default properties are:");
    ConfigModule.printDefaults(System.err);
  }

  /**
   * Takes care of calling close() on the repo implementation.
   *
   * These hooks will not get called if stopAtShutdown is set to false, which can be set
   * via the Config.JETTY_STOP_AT_SHUTDOWN property.
   */
  private static class ShutDownListener extends AbstractLifeCycle.AbstractLifeCycleListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Repository repo;
    private final Integer gracefulShutdown;
    ShutDownListener(Repository repo, Integer gracefulShutdown) {
      this.repo = repo;
      this.gracefulShutdown = gracefulShutdown;
    }

    @Override
    public void lifeCycleStopped(LifeCycle event) {
      logger.info("Waited {} ms to drain requests before closing the repo and exiting. " +
              "This wait time can be adjusted with the {} config property.",
              gracefulShutdown, Config.JETTY_GRACEFUL_SHUTDOWN);

      try {
        repo.close();
        logger.info("Successfully closed the repo.");
      } catch (IOException e) {
        logger.warn("Failed to properly close repo", e);
      }
    }
  }

  private static class ServerModule extends JerseyServletModule {

    @Override
    protected void configureServlets() {
      bind(Connector.class).to(SelectChannelConnector.class);
      serve("/*").with(GuiceContainer.class);
      bind(RESTRepository.class);
    }

    @Provides
    @Singleton
    public Server provideServer(
        @Named(Config.JETTY_HOST) String host,
        @Named(Config.JETTY_PORT) Integer port,
        @Named(Config.JETTY_PATH) String path,
        @Named(Config.JETTY_HEADER_SIZE) Integer headerSize,
        @Named(Config.JETTY_BUFFER_SIZE) Integer bufferSize,
        @Named(Config.JETTY_STOP_AT_SHUTDOWN) Boolean stopAtShutdown,
        @Named(Config.JETTY_GRACEFUL_SHUTDOWN) Integer gracefulShutdown,
        Repository repo,
        Connector connector,
        GuiceFilter guiceFilter,
        ServletContextHandler handler) {

      Server server = new Server();
      if (null != host && !host.isEmpty()) {
        connector.setHost(host);
      }
      connector.setPort(port);
      connector.setRequestHeaderSize(headerSize);
      connector.setRequestBufferSize(bufferSize);
      server.setConnectors(new Connector[] { connector });

      // the guice filter intercepts all inbound requests and uses its bindings
      // for servlets
      FilterHolder holder = new FilterHolder(guiceFilter);
      handler.addFilter(holder, "/*", null);
      handler.addServlet(NoneServlet.class, "/");
      handler.setContextPath(path);
      handler.addLifeCycleListener(new ShutDownListener(repo, gracefulShutdown));
      server.setHandler(handler);
      server.dumpStdErr();
      server.setStopAtShutdown(stopAtShutdown);
      server.setGracefulShutdown(gracefulShutdown);
      return server;
    }

    private static final class NoneServlet extends HttpServlet {
      private static final long serialVersionUID = 4560115319373180139L;
    }
  }

}
