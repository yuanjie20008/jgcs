/*
 * JGCS - Group Communication Service.
 * Copyright (C) 2006 Nuno Carvalho, Universidade de Lisboa
 * Copyright (C) 2013 Jose' Orlando Pereira
 *
 * http://github.com/jopereira/jgcs
 *
 * See COPYING for licensing details.
 */

package net.sf.jgcs.spi;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import net.sf.jgcs.GroupConfiguration;
import net.sf.jgcs.GroupException;

/**
 * Data session that directly polls I/O. This is often used for simple
 * protocols that do not have associated membership sessions. It probably should
 * not be used with AbstractPollingProtocol.
 * @author Jose Pereira
 */
public abstract class AbstractPollingDataSession<
		P extends AbstractProtocol<P,DS,CS,G>,
		DS extends AbstractPollingDataSession<P,DS,CS,G>,
		CS extends AbstractControlSession<P,DS,CS,G>,
		G extends GroupConfiguration>
		extends AbstractDataSession<P,DS,CS,G> {
	protected ExecutorService pool;
	protected Runnable task;

	protected AbstractPollingDataSession() {
		pool = Executors.newFixedThreadPool(1, new ThreadFactory() {
			public Thread newThread(Runnable r) {
				Thread t=new Thread(r);
				t.setDaemon(true);
				return t;
			}
		});
		task = new Runnable() {
			public void run() {
				poll();
			}
		};		
	}
	
	/**
	 * Start polling for input. This should be executed after the object is
	 * fully initialized and before any invocations. Most likely, as the last
	 * operation of the constructor in a derived concrete class.
	 */
	@Override
	protected void boot() {
		pool.execute(task);		
	}
	
	private void poll() {
		if (isClosed()) {
			pool.shutdown();
			return;
		}
		try {
			read();
		} catch (InterruptedException e) {
			if (!isClosed()) {
				GroupException je=new GroupException("reader thread interrupted", e);
				notifyExceptionListeners(je);
			}
		} catch (IOException e) {
			if (!isClosed()) {
				GroupException je=new GroupException("I/O exception", e);
				notifyExceptionListeners(je);
			}
		}
		pool.execute(task);
	}

	/**
	 * This method normally blocks waiting for input. It should wakeup and never
	 * block again after cleanup has been called, either by returning or throwing
	 * some exception.
	 * @throws InterruptedException 
	 * @throws GroupException
	 */
	protected abstract void read() throws IOException, InterruptedException;
}
