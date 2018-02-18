package com.gf.stomp.client.connection.protocol;

import java.io.StringReader;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gf.collections.GfCollection;
import com.gf.collections.GfCollections;
import com.gf.stomp.client.connection.StompHeader;

public final class StompMessage {
	public static final String TERMINATE_MESSAGE_SYMBOL = "\u0000";
	private static final Pattern PATTERN_HEADER = Pattern.compile("([^:\\s]+)\\s*:\\s*([^:\\s]+)");

	private final String mStompCommand;
	private final GfCollection<StompHeader> mStompHeaders;
	private final String mPayload;

	public StompMessage(final String stompCommand, final List<StompHeader> stompHeaders, final String payload) {
		mStompCommand = stompCommand;
		mStompHeaders = GfCollections.wrapAsCollection(stompHeaders);
		mPayload = payload;
	}

	public final List<StompHeader> getStompHeaders() {
		return mStompHeaders;
	}

	public final String getPayload() {
		return mPayload;
	}

	public final String getStompCommand() {
		return mStompCommand;
	}

	public String findHeader(String key) {
		if (mStompHeaders == null) return null;
		for (final StompHeader header : mStompHeaders) 
			if (header.getKey().equals(key)) return header.getValue();

		return null;
	}

	public final String compile() {
		final StringBuilder builder = new StringBuilder(124);
		builder.append(mStompCommand).append('\n');
		for (StompHeader header : mStompHeaders) 
			builder
			.append(header.getKey())
			.append(':')
			.append(header.getValue())
			.append('\n');

		builder.append('\n');
		if (mPayload != null) {
			builder.append(mPayload).append("\n\n");
		}
		builder.append(TERMINATE_MESSAGE_SYMBOL);
		return builder.toString();
	}

	public static StompMessage from(final String data) {
		if (data == null || data.trim().isEmpty()) 
			return new StompMessage(StompCommand.UNKNOWN, null, data);
		final Scanner reader = new Scanner(new StringReader(data));
		reader.useDelimiter("\\n");
		final String command = reader.next();
		final GfCollection<StompHeader> headers = GfCollections.asLinkedCollection();
		while (reader.hasNext(PATTERN_HEADER)) {
			final Matcher matcher = PATTERN_HEADER.matcher(reader.next());
			matcher.find();
			headers.add(new StompHeader(matcher.group(1), matcher.group(2)));
		}
		reader.skip("\\s");
		reader.useDelimiter(TERMINATE_MESSAGE_SYMBOL);
		final String payload = reader.hasNext() ? reader.next().trim() : null;
		reader.close();
		return new StompMessage(command, headers, payload);
	}
}
