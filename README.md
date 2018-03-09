# STOMP SockJS-like client for Vivala projects


- Adding client to your project:

	>
	1. Add the repo to your build:
	```xml
	<repositories>
		<repository>
			<id>stomp.client-mvn-repo</id>
			<url>https://raw.github.com/myteksp/stomp.client/mvn-repo/</url>
			<snapshots>
				<enabled>true</enabled>
				<updatePolicy>always</updatePolicy>
			</snapshots>
		</repository>
	</repositories>
	```
	
	2. Add the dependency:
	```xml
	<dependency>
		<groupId>com.gf</groupId>
		<artifactId>stomp.client</artifactId>
		<version>0.0.9-SNAPSHOT</version>
	</dependency>
	```

	
- Connecting the client:

	> 
	```
	GenericClient client = Client.create("<https|http|wss|ws>://<your host>/<websocket STOMP end-point>", <JavaRX scheduler>);
	client.connect();
	```
	
- Sending data to server:

	> 
	```
	client.send("<path-to-handler>", "<payload>");
	```
	
- Subscribing to topics:

	> 
	```
	client.topic("<path-to-topic>", message->{
			System.out.println(message.getPayload());
	});
	```
	
- Closing the client:

	> 
	```
	client.disconnect();
	```
	
	
	