package com.mulesoft.mule.tools.aws.elb;

/**
 * Target class
 * @author anthony.rabiaza@mulesoft.com
 *
 */
public class Target {
	private String ip;
	private int port;
	
	public Target(String ip, int port){
		this.ip = ip;
		this.port = port;
	}
	
	public String getIp() {
		return ip;
	}
	public int getPort() {
		return port;
	}
}
