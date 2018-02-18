package com.gf.stomp.client.log;

public final class Log {
	private static LogHandler handler = new LogHandler() {
		@Override
		public final int onLogEvent(final LogEvent event) {
			System.out.println(event);
			return 0;
		}
	};

	public static final LogHandler setHandler(final LogHandler newHandler) {
		if (newHandler == null)
			throw new RuntimeException("Handler can not be null");
		final LogHandler res = handler;
		handler = newHandler;
		return res;
	}


	public final static int d(final String tag, final String msg, final Throwable tr){
		return handler.onLogEvent(new LogEvent(LogEvent.Type.d, tag, msg, tr));
	}

	public final static int d(final String tag, final String msg){
		return handler.onLogEvent(new LogEvent(LogEvent.Type.d, tag, msg, null));
	}

	public final static int e(final String tag, final String msg){
		return handler.onLogEvent(new LogEvent(LogEvent.Type.e, tag, msg, null));
	}

	public final static int e(final String tag, final String msg, final Throwable tr){
		return handler.onLogEvent(new LogEvent(LogEvent.Type.e, tag, msg, tr));
	}


	public final static int i(final String tag, final String msg, Throwable tr){
		return handler.onLogEvent(new LogEvent(LogEvent.Type.i, tag, msg, tr));
	}

	public final static int i(final String tag, final String msg){
		return handler.onLogEvent(new LogEvent(LogEvent.Type.i, tag, msg, null));
	}

	public final static int v(final String tag, final String msg){
		return handler.onLogEvent(new LogEvent(LogEvent.Type.v, tag, msg, null));
	}

	public final static int v(final String tag, final String msg, final Throwable tr){
		return handler.onLogEvent(new LogEvent(LogEvent.Type.v, tag, msg, tr));
	}

	public final static int w(final String tag, final Throwable tr) {
		return handler.onLogEvent(new LogEvent(LogEvent.Type.w, tag, null, tr));
	}
	public final static int w(final String tag, final String msg, final Throwable tr){
		return handler.onLogEvent(new LogEvent(LogEvent.Type.w, tag, msg, tr));
	}

	public final static int w(final String tag, final String msg){
		return handler.onLogEvent(new LogEvent(LogEvent.Type.w, tag, msg, null));
	}

	public final static int wtf(final String tag, final String msg){
		return handler.onLogEvent(new LogEvent(LogEvent.Type.wtf, tag, msg, null));
	}

	public final static int wtf(final String tag, final Throwable tr){
		return handler.onLogEvent(new LogEvent(LogEvent.Type.wtf, tag, null, tr));
	}

	public final static int wtf(final String tag, final String msg, final Throwable tr){
		return handler.onLogEvent(new LogEvent(LogEvent.Type.wtf, tag, msg, tr));
	}
}
