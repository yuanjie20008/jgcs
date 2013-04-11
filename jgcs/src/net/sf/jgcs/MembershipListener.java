/*
 * JGCS - Group Communication Service.
 * Copyright (C) 2006 Nuno Carvalho, Universidade de Lisboa
 * Copyright (C) 2013 Jose' Orlando Pereira
 *
 * http://github.com/jopereira/jgcs
 *
 * See COPYING for licensing details.
 */

package net.sf.jgcs;

/**
 * 
 * This class defines a MembershipListener.
 * 
 * This listener must be used to receive membership, when the control session used
 * implements the MembershipSession or BlockSession interfaces.
 * 
 * @author <a href="mailto:nunomrc@di.fc.ul.pt">Nuno Carvalho</a>
 * @version 1.0
 */
public interface MembershipListener {
	
	/**
	 * Notification of a MembershipChange. This should happen due to joining, leaving or
	 * failure of group members, but also because of merging or partitioning of 
	 * memberships. The new membership can be retrieved from the MembershipSession.
	 */
	public void onMembershipChange();

	/**
	 * Notification from the membership to indicate that the registered member
	 * does not belong to the group any more. This should happen when the member
	 * lost intermediate views (for instance, when using primary views) and lost some messages.
	 * After receiving this notification, the member may try to rejoin again.
	 */
	public void onExcluded();

}
