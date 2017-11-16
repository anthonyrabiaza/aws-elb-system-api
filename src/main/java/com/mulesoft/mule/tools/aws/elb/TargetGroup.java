package com.mulesoft.mule.tools.aws.elb;

/**
 * TargetGroup class
 * @author anthony.rabiaza@mulesoft.com
 *
 */
public class TargetGroup {
	private String arn;
	
	public TargetGroup(String arn){
		this.arn = arn;
	}

	public String getArn() {
		return arn;
	}
}
