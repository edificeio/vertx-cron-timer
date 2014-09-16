/*
 * Copyright © WebServices pour l'Éducation, 2014
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
