package fr.wseduc.cron;

import org.quartz.CronExpression;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;

public class CronTrigger implements Handler<Long>, Serializable {

	private transient final Vertx vertx;
	private final CronExpression expression;
	private transient Handler<Long> handler;
	private long timerId;

	public CronTrigger(Vertx vertx, String cronExpression) throws ParseException {
		this.vertx = vertx;
		this.expression = new CronExpression(cronExpression);
	}

	public CronTrigger schedule(Handler<Long> handler) throws ParseException {
		if (handler == null) {
			throw new IllegalArgumentException("Handler is null.");
		}
		this.handler = handler;
		final long delay = getDelay(expression);
		timerId = vertx.setTimer(delay, this);
		return this;
	}

	private long getDelay(CronExpression expression) {
		final Date now = new Date();
		final Date next = expression.getNextValidTimeAfter(now);
		return next.getTime() - now.getTime();
	}

	@Override
	public void handle(Long timerId) {
		final long delay = getDelay(expression);
		timerId = vertx.setTimer(delay, this);
		handler.handle(timerId);
	}

	public void cancel() {
		vertx.cancelTimer(timerId);
	}

}
