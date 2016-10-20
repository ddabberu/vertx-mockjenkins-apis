package io.vertx.mockapis.jenkins;

import io.vertx.core.AbstractVerticle;
//import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mockapis.jenkins.StartupVerticle;

import java.util.Objects;
//import java.util.Random;

/**
 * A verticle simulating the evaluation of a company evaluation in a very
 * unrealistic and irrational way. It emits the new data on the `jenkins`
 * address on the event bus.
 * @author Durgaprasad Dabberu
 */

public class JenkinsDataVerticle extends AbstractVerticle {

	String name;
	long period;
	int buildNumber;
	String status;
	String building;

	/**
	 * Method called when the verticle is deployed.
	 */
	@Override
	public void start() {
		JsonObject config = config();
		init(config);

		// Every `period` ms, the given Handler is called.
		vertx.setTimer(period, l -> {
			send();
		});
	}

	void init(JsonObject config) {
		
		//System.out.println( ""+config.getString("name")+","+config.getString("delay")+","+config.getString("buildNumber"));	
		name = config.getString("name");
		period= Long.valueOf(config.getString("delay"));
		//System.out.println("#3");
		buildNumber=Integer.valueOf(config.getString("buildNumber"));
		//System.out.println("#4");
		status="inprogress";
		System.out.println( "job:"+name+",buildNum:"+buildNumber+",buildTime:"+period);
		Objects.requireNonNull(name);
	}

	/**
	 * Sends the market data on the event bus.
	 */
	private void send() {
		System.out.println("Waking up, Job completed");
		vertx.eventBus().publish(StartupVerticle.ADDRESS, toJson());
	}

	private JsonObject toJson() {
		return new JsonObject().put("name", name).put("buildNumber", Integer.toString(buildNumber))
				.put("status", "SUCCESS").put("building", "False");
	}
}
