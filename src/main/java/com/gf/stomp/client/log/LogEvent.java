package com.gf.stomp.client.log;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;

import com.gf.util.string.MacroCompiler;

public final class LogEvent {
	public static final String DEFAULT_TAG = "DEFAULT";
	public final Date date;
	public final Type type;
	public final String tag;
	public final String msg;
	public final String tr;
	
	public LogEvent(final Type type, final String tag, final String msg, final Throwable tr) {
		this.date = new Date();
		this.type = type == null?Type.na:type;
		this.tag = tag == null?DEFAULT_TAG:tag;
		this.msg = msg == null?"":msg;
		if (tr == null) 
			this.tr = null;
		else 
			this.tr = trowableToStr(tr);
	}
	@Override
	public final int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((msg == null) ? 0 : msg.hashCode());
		result = prime * result + ((tag == null) ? 0 : tag.hashCode());
		result = prime * result + ((tr == null) ? 0 : tr.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		final LogEvent other = (LogEvent) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (msg == null) {
			if (other.msg != null)
				return false;
		} else if (!msg.equals(other.msg))
			return false;
		if (tag == null) {
			if (other.tag != null)
				return false;
		} else if (!tag.equals(other.tag))
			return false;
		if (tr == null) {
			if (other.tr != null)
				return false;
		} else if (!tr.equals(other.tr))
			return false;
		if (type != other.type)
			return false;
		return true;
	}
	@Override
	public final String toString() {
		if (tr == null)
			return MacroCompiler.compileInline("[${1}][${0}][${2}][] - [${3}]", date.toString(), typeToVerbose(type), tag, msg);
		else
			return MacroCompiler.compileInline("[${1}][${0}][${2}][${4}] - [${3}]", date.toString(), typeToVerbose(type), tag, msg, tr);
	}
	
	public static final String typeToVerbose(final Type type) {
		if (type == null)
			return "NOT_SPECIFIED";
		switch (type) {
		case na:
			return "NOT_SPECIFIED";
		case d:
			return "DEBUG";
		case e:
			return "ERROR";
		case i:
			return "INFO";
		case v:
			return "VERBOSE";
		case w:
			return "WARNING";
		case wtf:
			return "CRITICAL_ERROR";
		default:
			return "WTF";
		}
	}
	
	private static final String trowableToStr(final Throwable t) {
		final ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
		final PrintStream stream = new PrintStream(out);
		t.printStackTrace(stream);
		stream.flush();
		stream.close();
		return new String(out.toByteArray());
	}
	
	public static enum Type{
		na, d, e, i, v, w, wtf
	}
}
