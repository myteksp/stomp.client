package com.gf.stomp.client.connection;

public final class StompHeader {
	public static final String VERSION = "version";
	public static final String HEART_BEAT = "heart-beat";
	public static final String DESTINATION = "destination";
	public static final String CONTENT_TYPE = "content-type";
	public static final String MESSAGE_ID = "message-id";
	public static final String ID = "id";
	public static final String ACK = "ack";

	private final String mKey;
	private final String mValue;

	public StompHeader(final String key, final String value) {
		mKey = key;
		mValue = value;
	}

	public final String getKey() {
		return mKey;
	}

	public final String getValue() {
		return mValue;
	}

	@Override
	public final int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mKey == null) ? 0 : mKey.hashCode());
		result = prime * result + ((mValue == null) ? 0 : mValue.hashCode());
		return result;
	}

	@Override
	public final boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final StompHeader other = (StompHeader) obj;
		if (mKey == null) {
			if (other.mKey != null)
				return false;
		} else if (!mKey.equals(other.mKey))
			return false;
		if (mValue == null) {
			if (other.mValue != null)
				return false;
		} else if (!mValue.equals(other.mValue))
			return false;
		return true;
	}

	@Override
	public final String toString() {
		return "StompHeader [mKey=" + mKey + ", mValue=" + mValue + "]";
	}
}
