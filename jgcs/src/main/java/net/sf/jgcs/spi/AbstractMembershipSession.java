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

import net.sf.jgcs.GroupConfiguration;
import net.sf.jgcs.InvalidStateException;
import net.sf.jgcs.Membership;
import net.sf.jgcs.MembershipID;
import net.sf.jgcs.MembershipListener;
import net.sf.jgcs.MembershipSession;

public abstract class AbstractMembershipSession<
		P extends AbstractProtocol<P,DS,CS,G>,
		DS extends AbstractDataSession<P,DS,CS,G>,
		CS extends AbstractMembershipSession<P,DS,CS,G>,
		G extends GroupConfiguration>
	extends AbstractControlSession<P,DS,CS,G> implements MembershipSession {

	private MembershipListener membListener;	
	protected Membership membership;
	
	protected void setMembership(Membership m) {
		membership = m;
	}

	protected void notifyAndSetMembership(Membership m) {
		/* Avoid NPE but invoke callback outside the lock.
		 * This means that there can be callbacks after close(),
		 * but avoids deadlocks.
		 */
		MembershipListener listener = null;
		try {
			lock.lock();
			membership = m;
			listener = membListener;		
		} finally {
			lock.unlock();
		}
		if(listener != null)
			listener.onMembershipChange();
	}

	protected void notifyRemoved() {
		/* Avoid NPE but invoke callback outside the lock.
		 * This means that there can be callbacks after close(),
		 * but avoids deadlocks.
		 */
		MembershipListener listener = null;
		try {
			lock.lock();
			membership = null;
			listener = membListener;		
		} finally {
			lock.unlock();
		}
		if(listener != null)
			listener.onExcluded();
	}

	@Override
	public void setMembershipListener(MembershipListener listener) {
		try {
			lock.lock();
			membListener = listener;
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	public MembershipListener getMembershipListener() {
		try {
			lock.lock();
			return membListener;
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	protected boolean hasAllListeners(){
		return super.hasAllListeners() && membListener != null ;
	}

	@Override
	public Membership getMembership() throws InvalidStateException {
		try {
			lock.lock();
			if(membership == null)
				throw new InvalidStateException("Membership does not exist.");
			return membership;
		} finally {
			lock.unlock();
		}				
	}

	@Override
	public MembershipID getMembershipID() throws InvalidStateException {
		try {
			lock.lock();
			if(membership == null)
				throw new InvalidStateException("Membership does not exist.");
			return membership.getMembershipID();
		} finally {
			lock.unlock();
		}				
	}
	
	@Override
	protected void cleanup() {
		super.cleanup();
		membership = null;
		membListener = null;
	}
}
