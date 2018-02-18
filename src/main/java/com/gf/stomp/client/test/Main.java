package com.gf.stomp.client.test;


import java.util.Map;

import com.gf.stomp.client.Client;
import com.gf.stomp.client.connection.protocol.GenericClient;
import com.gf.stomp.client.log.Log;
import com.gf.stomp.client.log.LogEvent.Type;
import com.gf.util.string.JSON;
import com.gf.util.string.MacroCompiler;

public final class Main {
	private static long prevTime = System.currentTimeMillis();
	public static void main(final String[] args) {
		Log.setLogLevel(Type.i);
		GenericClient client = Client.create("https://athena-backend-api-rest.herokuapp.com/broadcast");
		client.connect();
		
	
		client.topic("/event/tick", m->{
			final Map<?,?> event = JSON.fromJson(m.getPayload(), Map.class);
			final long t = Long.parseLong(event.get("time").toString());
			System.out.println(MacroCompiler.compileInline("Time: ${0}. Previous: ${1}. Diff: ${2}.", t + "", prevTime + "", (t - prevTime) + ""));
			prevTime = t;
		});
		
		client.disconnect();
	}
}
