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
			throw new NullPointerException("Handler can not be null");
		final LogHandler res = handler;
		handler = newHandler;
		return res;
	}

	private static LogEvent.Type logLevel = LogEvent.Type.d;

	public static final LogEvent.Type setLogLevel(final LogEvent.Type type){
		if (type == null)
			throw new NullPointerException("Log level can not be null");
		final LogEvent.Type res = logLevel;
		logLevel = type;
		return res;
	}

	public static final LogEvent.Type getLogLevel(){
		return logLevel;
	}


	private static final int onLog(final LogEvent event) {
		switch(logLevel) {
		case d:
			switch(event.type) {
			case d:
			case v:
			case i:
			case e:
			case w:
			case wtf:
			case na:
				return handler.onLogEvent(event);
			default:
				return -1;
			}
		case v:
			switch(event.type) {
			case v:
			case i:
			case e:
			case w:
			case wtf:
			case na:
				return handler.onLogEvent(event);
			default:
				return -1;
			}
		case i:
			switch(event.type) {
			case i:
			case e:
			case w:
			case wtf:
			case na:
				return handler.onLogEvent(event);
			default:
				return -1;
			}
		case e:
			switch(event.type) {
			case e:
			case w:
			case wtf:
			case na:
				return handler.onLogEvent(event);
			default:
				return -1;
			}
		case w:
			switch(event.type) {
			case w:
			case wtf:
			case na:
				return handler.onLogEvent(event);
			default:
				return -1;
			}
		case wtf:
			switch(event.type) {
			case wtf:
			case na:
				return handler.onLogEvent(event);
			default:
				return -1;
			}
		case na:
			switch(event.type) {
			case na:
				return handler.onLogEvent(event);
			default:
				return -1;
			}
		default:
			throw new RuntimeException("Unknown log level");
		}
	}


	public final static int d(final String tag, final String msg, final Throwable tr){
		return onLog(new LogEvent(LogEvent.Type.d, tag, msg, tr));
	}

	public final static int d(final String tag, final String msg){
		return onLog(new LogEvent(LogEvent.Type.d, tag, msg, null));
	}

	public final static int e(final String tag, final String msg){
		return onLog(new LogEvent(LogEvent.Type.e, tag, msg, null));
	}

	public final static int e(final String tag, final String msg, final Throwable tr){
		return onLog(new LogEvent(LogEvent.Type.e, tag, msg, tr));
	}


	public final static int i(final String tag, final String msg, Throwable tr){
		return onLog(new LogEvent(LogEvent.Type.i, tag, msg, tr));
	}

	public final static int i(final String tag, final String msg){
		return onLog(new LogEvent(LogEvent.Type.i, tag, msg, null));
	}

	public final static int v(final String tag, final String msg){
		return onLog(new LogEvent(LogEvent.Type.v, tag, msg, null));
	}

	public final static int v(final String tag, final String msg, final Throwable tr){
		return onLog(new LogEvent(LogEvent.Type.v, tag, msg, tr));
	}

	public final static int w(final String tag, final Throwable tr) {
		return onLog(new LogEvent(LogEvent.Type.w, tag, null, tr));
	}
	public final static int w(final String tag, final String msg, final Throwable tr){
		return onLog(new LogEvent(LogEvent.Type.w, tag, msg, tr));
	}

	public final static int w(final String tag, final String msg){
		return onLog(new LogEvent(LogEvent.Type.w, tag, msg, null));
	}

	public final static int wtf(final String tag, final String msg){
		return onLog(new LogEvent(LogEvent.Type.wtf, tag, msg, null));
	}

	public final static int wtf(final String tag, final Throwable tr){
		return onLog(new LogEvent(LogEvent.Type.wtf, tag, null, tr));
	}

	public final static int wtf(final String tag, final String msg, final Throwable tr){
		return onLog(new LogEvent(LogEvent.Type.wtf, tag, msg, tr));
	}
}
