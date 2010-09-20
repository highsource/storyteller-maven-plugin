package org.highsource.storyteller.plexus.logging;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.logging.Logger;

public class LogToLoggerAdapter implements Logger, Log {

	private final String name;

	private final Log log;

	public LogToLoggerAdapter(final String name, final Log log) {
		super();
		this.name = name;
		this.log = log;
	}

	public String getName() {
		return name;
	}

	public Logger getChildLogger(String name) {
		return new LogToLoggerAdapter(name, log);
	}

	public int getThreshold() {
		if (log.isDebugEnabled()) {
			return LEVEL_DEBUG;
		} else if (log.isInfoEnabled()) {
			return LEVEL_INFO;
		} else if (log.isWarnEnabled()) {
			return LEVEL_WARN;
		} else if (log.isErrorEnabled()) {
			return LEVEL_ERROR;
		} else {
			return LEVEL_DISABLED;
		}
	}

	public boolean isDebugEnabled() {
		return log.isDebugEnabled();
	}

	public void debug(CharSequence content) {
		log.debug(content);
	}

	public void debug(CharSequence content, Throwable error) {
		log.debug(content, error);

	}

	public void debug(String message) {
		log.debug(message);

	}

	public void debug(String message, Throwable throwable) {
		log.debug(message, throwable);
	}

	public void debug(Throwable error) {
		log.debug(error);
	}

	public boolean isInfoEnabled() {
		return log.isInfoEnabled();
	}

	public void info(CharSequence content) {
		log.info(content);
	}

	public void info(CharSequence content, Throwable error) {
		log.info(content, error);

	}

	public void info(String message) {
		log.info(message);

	}

	public void info(String message, Throwable throwable) {
		log.info(message, throwable);
	}

	public void info(Throwable error) {
		log.info(error);
	}

	public boolean isWarnEnabled() {
		return log.isWarnEnabled();
	}

	public void warn(CharSequence content) {
		log.warn(content);
	}

	public void warn(CharSequence content, Throwable error) {
		log.warn(content, error);

	}

	public void warn(String message) {
		log.warn(message);

	}

	public void warn(String message, Throwable throwable) {
		log.warn(message, throwable);
	}

	public void warn(Throwable error) {
		log.warn(error);
	}

	public boolean isErrorEnabled() {
		return log.isErrorEnabled();
	}

	public void error(CharSequence content) {
		log.error(content);
	}

	public void error(CharSequence content, Throwable error) {
		log.error(content, error);

	}

	public void error(String message) {
		log.error(message);

	}

	public void error(String message, Throwable throwable) {
		log.error(message, throwable);
	}

	public void error(Throwable error) {
		log.error(error);
	}

	public boolean isFatalErrorEnabled() {
		return log.isErrorEnabled();
	}

	public void fatalError(String message) {
		log.error(message);
	}

	public void fatalError(String message, Throwable throwable) {
		log.error(message, throwable);
	}
}
