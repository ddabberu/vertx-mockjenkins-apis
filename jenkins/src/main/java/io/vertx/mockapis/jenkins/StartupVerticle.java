package io.vertx.mockapis.jenkins;

import io.vertx.core.DeploymentOptions;
//import io.vertx.core.json.JsonArray;
//import io.vertx.core.json.JsonObject;
import io.vertx.mockapis.common.MicroServiceVerticle;
//import io.vertx.ext.web.*;

/**
 * a verticle generating "fake" responses based on the configuration.
* @author Durgaprasad Dabberu
 */
public class StartupVerticle extends MicroServiceVerticle {

  /**
   * The address on which the data are sent.
   */
  public static final String ADDRESS = "jenkins";

  /**
   * This method is called when the verticle is deployed.
   */
  @Override
  public void start() {
    super.start();

    //Deploy a verticle to publish AIM data read from the file
    //vertx.deployVerticle(JenkinsDataVerticle.class.getName(), new DeploymentOptions().setConfig(config()));

    // Deploy another verticle without configuration.
    //vertx.deployVerticle(RestJenkinsAPIVerticle.class.getName());
    
    vertx.deployVerticle(RestJenkinsAPIVerticle.class.getName(), new DeploymentOptions().setConfig(config()));

    // Publish the services in the discovery infrastructure.
    publishMessageSource("jenkins-service", ADDRESS, rec -> {
      if (!rec.succeeded()) {
        rec.cause().printStackTrace();
      }
      System.out.println("jenkins-service service published : " + rec.succeeded());
    });

    System.out.println("Http port:"+config().getInteger("http.port"));
    //publishHttpEndpoint("jenkinsmockup", "localhost", config().getInteger("http.port", 8080), ar -> {
    publishHttpEndpoint("jenkinsmockup", "localhost", 35002, ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
      } else {
        System.out.println("jenkinsmockup (Rest endpoint) service published : " + ar.succeeded());
      }
    });
  }
}
