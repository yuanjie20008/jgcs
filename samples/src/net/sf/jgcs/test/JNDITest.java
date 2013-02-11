
/*
 * JGCS - Group Communication Service.
 * Copyright (C) 2006 Nuno Carvalho, Universidade de Lisboa
 *
 * jgcs@lasige.di.fc.ul.pt
 *
 * Departamento de Informatica, Universidade de Lisboa
 * Bloco C6, Faculdade de Ciências, Campo Grande, 1749-016 Lisboa, Portugal.
 *
 * See COPYING for licensing details.
 */

package net.sf.jgcs.test;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import net.sf.jgcs.GroupConfiguration;
import net.sf.jgcs.JGCSException;
import net.sf.jgcs.ProtocolFactory;
import net.sf.jgcs.Service;

/**
 * Test application using JNDI. Requires configuration of three objects
 * with names myProto, myGroup and myService.
 */
public class JNDITest extends Application {

	public JNDITest(Context ctx) throws JGCSException, NamingException {
		super((ProtocolFactory) ctx.lookup("myProto"),
				(GroupConfiguration) ctx.lookup("myGroup"),
				(Service)ctx.lookup("myService"));
	}

	public static void main(String[] args) {
		try {
			Context ctx = new InitialContext();
			Runnable test = new JNDITest(ctx);
			test.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}