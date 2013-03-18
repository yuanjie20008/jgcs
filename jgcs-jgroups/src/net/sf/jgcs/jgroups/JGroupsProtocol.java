
/*
 *
 * JGroups implementation of JGCS - Group Communication Service
 * Copyright (C) 2006 Nuno Carvalho, Universidade de Lisboa
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 * 
 * Contact
 * 	Address:
 * 		LASIGE, Departamento de Informatica, Bloco C6
 * 		Faculdade de Ciencias, Universidade de Lisboa
 * 		Campo Grande, 1749-016 Lisboa
 * 		Portugal
 * 	Email:
 * 		jgcs@lasige.di.fc.ul.pt
 * 
 */
 
package net.sf.jgcs.jgroups;

import java.io.InputStream;
import java.io.OutputStream;

import net.sf.jgcs.AbstractProtocol;
import net.sf.jgcs.ControlSession;
import net.sf.jgcs.DataSession;
import net.sf.jgcs.GroupConfiguration;
import net.sf.jgcs.JGCSException;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.View;

public class JGroupsProtocol extends AbstractProtocol {

	public JGroupsProtocol() {
		super();
	}

	public DataSession openDataSession(GroupConfiguration group) throws JGCSException {
		DataSession data=lookupDataSession(group);
		if (data==null) {
			createSessions(group);
			data = lookupDataSession(group);
		}
		return data;
	}

	public ControlSession openControlSession(GroupConfiguration group)
			throws JGCSException {
		ControlSession control = lookupControlSession(group);
		if (control==null){
			createSessions(group);
			control = lookupControlSession(group);
		}
		return control;
	}
	
	private synchronized void createSessions(GroupConfiguration g) throws JGCSException{
		if( !(g instanceof JGroupsGroup))
			throw new JGCSException("Wrong type of the given Group: "+g.getClass().getName()+
					"should be of type "+JGroupsGroup.class.getName());
		
		JGroupsGroup group = (JGroupsGroup) g;

		try {
			JChannel channel = new JChannel(group.getConfigName());
			final JGroupsControlSession cs = new JGroupsControlSession(channel,group.getGroupName());
			final JGroupsDataSession ds= new JGroupsDataSession(this, channel, group);
			putSessions(group, cs,ds);
			
			Receiver recv = new Receiver() {

				@Override
				public void receive(Message msg) {
					JGroupsMessage message = new JGroupsMessage();
					byte[] jgroupsBuffer = ((org.jgroups.Message) msg).getBuffer();
					if(jgroupsBuffer == null)
						return;
					byte[] buffer = new byte[jgroupsBuffer.length];
					// FIXME? This makes the gap on the DOA results.
					System.arraycopy(jgroupsBuffer,0,buffer,0,buffer.length);
					message.setPayload(buffer);
					message.setSenderAddress(new JGroupsSocketAddress(((org.jgroups.Message) msg).getSrc()));
					Object cookie = ds.notifyMessageListeners(message);
					/* FIXME: These notifications are _so_ wrong. The application should get 
					 * only get one notification and compare the Service given with what's expected.
					 */
					if(cookie != null){
						ds.notifyServiceListeners(cookie,new JGroupsService("seto_total_order"));
						ds.notifyServiceListeners(cookie,new JGroupsService("regular_total_order"));
						ds.notifyServiceListeners(cookie,new JGroupsService("uniform_total_order"));
					}
				}

				@Override
				public void getState(OutputStream output) throws Exception {
					// TODO Auto-generated method stub
				}

				@Override
				public void setState(InputStream input) throws Exception {
					// TODO Auto-generated method stub					
				}

				@Override
				public void viewAccepted(View new_view) {
					cs.jgroupsViewAccepted(new_view);						
				}

				@Override
				public void suspect(Address suspected_mbr) {
					cs.jgroupsSuspect(suspected_mbr);
				}

				@Override
				public void block() {
					// TODO Auto-generated method stub
				}

				@Override
				public void unblock() {
					// TODO Auto-generated method stub					
				}				
			};
			
			channel.setReceiver(recv);

		} catch (Exception e) {
			throw new JGCSException("Could not create JGroups channel. ",e);
		}
	}
	
	protected synchronized void removeSessions(GroupConfiguration group) {
		super.removeSessions(group);
	}
}
