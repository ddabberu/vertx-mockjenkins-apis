package io.vertx.mockapis.jenkins;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
//import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
//import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
//import io.vertx.ext.web.*;

import java.util.HashMap;
import java.util.Map;
import java.util.*;
import java.util.Random;
//import java.io.File;
//import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This verticle exposes a HTTP endpoint to retrieve the static jenkins data.
 *
 *
 * @author Durgaprasad Dabberu
 */
public class RestJenkinsAPIVerticle extends AbstractVerticle {

	private Map<String, JsonObject> configJobs = new HashMap<>();
	// map2 to store objects by name
	//private Map<String, JsonObject> builds = new HashMap<>();
	private List<JsonObject> builds = new ArrayList<JsonObject>();
	private Map<String,String> jobLastBuild= new HashMap<>();
	private String jboss="";
	private String node="";
	
	
	private final Random random = new Random();

	@Override
	public void start() throws Exception {
		
		//read from config for the delay
		//JsonObject config = config();
		System.out.println("#1");
		JsonArray jobs =config().getJsonArray("jobs");
		// 
		// System.out.println("#2"+ jobs.toString());

		for (Object q : jobs) {
			JsonObject job = (JsonObject) q;
			configJobs.put(job.getString("name"), job);
		}
		
		//Read jboss output
		jboss= new String(Files.readAllBytes(Paths.get("src/conf/jboss.txt")));
		//Read node output
		node=new String(Files.readAllBytes(Paths.get("src/conf/node.txt")));
		
		// System.out.println("#2"+ job.toString());
		
		vertx.eventBus().<JsonObject> consumer(StartupVerticle.ADDRESS).handler(message -> {
			System.out.println("RestAPI handler#1");
			JsonObject quote = message.body();
			System.out.println("#2" + quote.getString("name"));
			//look up in the list and update the status of the build
			for( JsonObject j: builds)
			{
				if( j.getString("name").equals(quote.getString("name")))
				if( j.getString("buildNumber").equals(quote.getString("buildNumber")))
				{
					j.put( "status", quote.getString("status"));
					j.put( "building", quote.getString("building"));
				}
			}
		});
		///

		vertx.createHttpServer().requestHandler(request -> {
			
			//HttpServerResponse response = request.response().putHeader("content-type", "application/json");
			HttpServerResponse response = request.response();
			System.out.println("HTTP Method:" + request.method());
			// DD
			if (request.method().toString().equalsIgnoreCase("POST")) {
				
				//Get the verb
				String path= request.path();
				path=path.toLowerCase();
				System.out.println("URI#"+ path);
				//parse the path for action.
				String parts[]= path.split("/");
				//System.out.println( parts.toString());
				int plen= parts.length;
				String action= parts[plen-1];
				String jobName= "job1";
				//expecting the first part is the job name, otherwise add some code
				//xl-r pads this weird sequence: http://10.20.159.8:35113/build/jenkins/v1/job/job1/lastBuild/buildNumber
				jobName= parts[5];
				System.out.println("jobname:"+jobName+",action:"+action);
				//build/jenkins/v1/job
				if ( action.equals("build"))
				{
					//start a build job and respond with build number, associate the delay
					JsonObject job= configJobs.get(jobName);
					
					//Read the build time from config, other option is below random
					int delay=job.getInteger("delay");
					//String lastbuild= jobLastBuild.get(jobName);
					int buildNum;
					if( jobLastBuild.containsKey(jobName))
					{
						buildNum= Integer.parseInt(jobLastBuild.get(jobName));
						buildNum++;
						
					}
					else
					{
						buildNum= random.nextInt(256);
					}
					
					//int buildNum= random.nextInt(256);
					//int delay= random.nextInt(5);
					//delay=delay*1000;
					//String json="{\"name\":\""+jobName+"\",\"delay\":\""+Integer.toOctalString(delay)+"\",\"buildNumber\":\""+ Integer.toString(buildNum)+"\"}";
					String json="{\"name\":\""+jobName+"\",\"delay\":\""+delay+"\",\"buildNumber\":\""+ Integer.toString(buildNum)+"\"}";
					System.out.println("#Build API, creating vertix with json"+json);
					JsonObject jconfig= new JsonObject(json);
				    vertx.deployVerticle(JenkinsDataVerticle.class.getName(), new DeploymentOptions().setConfig(jconfig));
				    
				    //add this 2 builds map
				    builds.add( new JsonObject().put("name", jobName).put("buildNumber", Integer.toString(buildNum))
							.put("status", "inprogress").put("building", "true"));
				    
				    //add/update the lastbuild 
				    jobLastBuild.put(jobName,Integer.toString(buildNum) );
				    
				    //return 201 with no body
				    response.setStatusCode(201).end();
				    
				}
				else if (action.equals("buildnumber"))
				{
					System.out.println("#Serving buildNumber");
					//System.out.println( )
					//return 
					
					//if( jobLastBuild.containsKey(jobName)) 
					response.putHeader("content-type","text/plain");
					response.setStatusCode(200);
					if(jobLastBuild.containsKey(jobName)){
						response.end( jobLastBuild.get(jobName));
					}
					else{
						response.end( "0"   );
					}
					
				}
				else if(action.equals("json"))
				{
					//send some sort of json
					String rawjson="{\"actions\":[{\"causes\":[{\"shortDescription\":\"Started by user ddabberu\",\"userId\":\"ddabberu\",\"userName\":\"ddabberu\"}]},{},{},{\"buildsByBranchName\":{\"origin/master\":{\"buildNumber\":33,\"buildResult\":null,\"marked\":{\"SHA1\":\"ac49e874d528176a2dde45f2c09bea01b112f517\",\"branch\":[{\"SHA1\":\"ac49e874d528176a2dde45f2c09bea01b112f517\",\"name\":\"origin/master\"}]},\"revision\":{\"SHA1\":\"ac49e874d528176a2dde45f2c09bea01b112f517\",\"branch\":[{\"SHA1\":\"ac49e874d528176a2dde45f2c09bea01b112f517\",\"name\":\"origin/master\"}]}}},\"lastBuiltRevision\":{\"SHA1\":\"ac49e874d528176a2dde45f2c09bea01b112f517\",\"branch\":[{\"SHA1\":\"ac49e874d528176a2dde45f2c09bea01b112f517\",\"name\":\"origin/master\"}]},\"remoteUrls\":[\"ssh://stash.aexp.com/dd/ddrepo1.git\"],\"scmName\":\"\"},{},{},{}],\"artifacts\":[],\"building\":false,\"description\":null,\"duration\":621659,\"estimatedDuration\":624966,\"executor\":null,\"fullDisplayName\":\"cdserver » ddswag1 #33\",\"id\":\"2016-10-15_13-35-56\",\"keepLog\":false,\"number\":33,\"result\":\"SUCCESS\",\"timestamp\":1476563756890,\"url\":\"https://build.aedc.extra.aexp.com/job/cdserver/job/ddswag1/33/\",\"builtOn\":\"cu172_nodejs\",\"changeSet\":{\"items\":[],\"kind\":\"git\"},\"culprits\":[]}";
					JsonObject jsonResp= new JsonObject(rawjson);
					String lastbuild= jobLastBuild.get(jobName);
					String bstatus="inprogress";
					String building="True";
					for( JsonObject j: builds)
					{
						if( j.getString("name").equals(jobName))
						if( j.getString("buildNumber").equals(lastbuild))
						{
							//j.put( "status", quote.getString("status"));
							bstatus=j.getString("status");
							building=j.getString("building");
							break;
						}
					}
					jsonResp.put("number", lastbuild);
					jsonResp.put("result", bstatus);
					jsonResp.put("building", building);
					response.putHeader("content-type","application/json");
					response.setStatusCode(200).end(jsonResp.encodePrettily());
					
				}
				else if (action.contains("progressive"))
				{
					//send console output here
					JsonObject job= configJobs.get(jobName);
					response.putHeader("content-type","text/plain");
					if( job.getString("type").equals("java"))
					{
						response.setStatusCode(200);
						response.end(jboss);
					}
					else if( job.getString("type").equals("node"))
					{
						response.setStatusCode(200);
						response.end(node);
					}
					else response.setStatusCode(401).end();
				}
				else
				{
					response.setStatusCode(401).end();
				}

		}
			else {
				response.setStatusCode(401).end();}
			}
			
			).listen(35113, ar -> {
			if (ar.succeeded()) {
				System.out.println("Server started");
			} else {
				System.out.println("Cannot start the server: " + ar.cause());
			}
		});
	}
}
