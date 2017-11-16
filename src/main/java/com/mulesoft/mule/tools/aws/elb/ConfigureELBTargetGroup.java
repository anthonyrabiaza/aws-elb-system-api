package com.mulesoft.mule.tools.aws.elb;

import java.util.Map;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.api.registry.MuleRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.elasticloadbalancingv2.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancingv2.AmazonElasticLoadBalancingClientBuilder;
import com.amazonaws.services.elasticloadbalancingv2.model.DeregisterTargetsRequest;
import com.amazonaws.services.elasticloadbalancingv2.model.DeregisterTargetsResult;
import com.amazonaws.services.elasticloadbalancingv2.model.RegisterTargetsRequest;
import com.amazonaws.services.elasticloadbalancingv2.model.RegisterTargetsResult;
import com.amazonaws.services.elasticloadbalancingv2.model.TargetDescription;

/**
 * ConfigureELBTargetGroup class <br/>
 * This class is responsible of talking to AWS for the Target Group configuration
 * 
 * @author anthony.rabiaza@mulesoft.com
 *
 */
public class ConfigureELBTargetGroup implements Callable {
	
	private static final String AVAILABILITY_ZONE_ALL 	= "all";
	private static final String CIDR_PREFIX 			= "10.";
	
	private static Logger log = LoggerFactory.getLogger(ConfigureELBTargetGroup.class);

	private void configure(AWSCredentials credentials, String region, TargetGroup targetGroup, Target target, State state) throws Exception{

		try {
			AmazonElasticLoadBalancing client = AmazonElasticLoadBalancingClientBuilder.standard()
					.withCredentials(new AWSStaticCredentialsProvider(credentials))
					.withRegion(region)
					.build();

			switch(state) {
			case start:
				RegisterTargetsRequest registerRequest = new RegisterTargetsRequest()
				.withTargetGroupArn(targetGroup.getArn())
				.withTargets(
						new TargetDescription().
						withId(target.getIp())
						.withPort(target.getPort())
						.withAvailabilityZone(AVAILABILITY_ZONE_ALL)
						);
				RegisterTargetsResult registerResponse = client.registerTargets(registerRequest);
				System.out.println("AWS Response:"+registerResponse.toString());

				break;
			case stop:
				DeregisterTargetsRequest deregisterRequest = new DeregisterTargetsRequest()
				.withTargetGroupArn(targetGroup.getArn())
				.withTargets(
						new TargetDescription().
						withId(target.getIp())
						.withPort(target.getPort())
						.withAvailabilityZone(AVAILABILITY_ZONE_ALL)
						);
				DeregisterTargetsResult removeResponse = client.deregisterTargets(deregisterRequest);
				System.out.println("AWS Response: "+removeResponse.toString());
				break;
			default:
				break;
			}


		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}

	private String getIP(String ips) throws Exception{
		/*
		 * Allowed by AWS
		 * 10.0.0.0/8
		 * 172.16.0.0/12
		 * 192.168.0.0/16
		 * 100.64.0.0/10
		 */
		String[] ipList = ips.split(",");
		for (int i = 0; i < ipList.length; i++) {
			if(ipList[i].startsWith(CIDR_PREFIX)){
				return ipList[i];
			}
		}
		log.error("No valid ip found in list ("+ips+"), using first one");
		
		return ipList[0];
	}


	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		MuleRegistry registry = eventContext.getMuleContext().getRegistry();
		@SuppressWarnings("unchecked")
		Map<String,String> query = (Map<String,String>)eventContext.getMessage().getInboundProperty("http.query.params");
		String ips = query.get("ipAddress");
		State state = State.valueOf(query.get("state"));
		AWSCredentials credentials = new BasicAWSCredentials(registry.get("aws_access_key_id"),registry.get("aws_secret_access_key"));
		String region = registry.get("aws.elb.region");
		TargetGroup targetGroup = new TargetGroup((String)registry.get("aws.elb.targetgroup.arn"));
		Target target = new Target(getIP(ips), Integer.parseInt((String)registry.get("aws.elb.targetgroup.port")));
		ConfigureELBTargetGroup configureELBTargetGroup = new ConfigureELBTargetGroup();
		configureELBTargetGroup.configure(credentials, region, targetGroup, target, state);
		return null;
	}
}
