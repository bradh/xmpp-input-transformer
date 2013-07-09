/**
 * Copyright (c) Codice Foundation
 *
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 *
 **/
package org.codice.opendx.xmpp;


import java.util.Map;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.ConfigurationPolicy;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;
import org.apache.log4j.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

@Component(name = "org.codice.opendx.xmpp.xmppclient",
designate = XmppClient.Config.class,
configurationPolicy = ConfigurationPolicy.require)
public class XmppClient implements IXmppClient {
	
	interface Config {
		@Meta.AD(required = true, deflt = "test2")
		 String login();
		@Meta.AD(required = true, deflt = "$123qwe!")
		 String password();
		@Meta.AD(required = true, deflt = "TestUser2")
		 String nickname();
		@Meta.AD(required = true, deflt = "localhost.localdomain")
		 String server();
		@Meta.AD(required = true, deflt = "5222")
		 String port();
		@Meta.AD(required = true, deflt = "Test@conference.localhost.localdomain")
		 String room();
		@Meta.AD(required = true, deflt = "false")
		 String sASLAuthenticationEnabled();
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 8441276665745228719L;
	

	static final Logger log = Logger.getLogger(XmppClient.class);
	
		 private Config config;
	 
	    @Activate
	    protected void activate(Map<String, Object> configProps) throws InvalidSyntaxException {
	    	config = Configurable.createConfigurable(Config.class, configProps);
	    	
		 	
	        
	    }
	    @Deactivate
	    protected void deactivate(Map<String, Object> configProps) {
	        config = null;
	        
	    }

	   
	   
	    public String login() {
			return config.login();
		}
		public String password() {
			return config.password();
		}
		public String nickname() {
			return config.nickname();
		}
		public String server() {
			return config.server();
		}
		public String port() {
			return config.port();
		}
		public String room() {
			return config.room();
		}
		public String sASLAuthenticationEnabled() {
			return config.sASLAuthenticationEnabled();
		}
		
	
	}
	 
	
		
		



