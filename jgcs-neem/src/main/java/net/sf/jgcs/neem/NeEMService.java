/*
 * NeEM implementation of JGCS - Group Communication Service.
 * Copyright (C) 2006,2013 José Orlando Pereira
 *
 * http://github.com/jopereira/jgcs
 *
 * See COPYING for licensing details.
 */

package net.sf.jgcs.neem;

import net.sf.jgcs.Service;

/**
 * NeEM doesn't accept service configuration. 
 */
public class NeEMService implements Service {
	
	private static final long serialVersionUID = 2L;
	
	@Override
	public boolean satisfies(Service service) {
		return service instanceof NeEMService;
	}
}
