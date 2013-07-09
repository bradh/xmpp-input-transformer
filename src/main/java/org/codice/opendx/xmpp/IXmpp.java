package org.codice.opendx.xmpp;

import java.io.IOException;

import org.apache.lucene.queryparser.classic.ParseException;
import org.jivesoftware.smack.XMPPException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import ddf.catalog.CatalogFramework;

public interface IXmpp {
	
	void setCatalog(CatalogFramework catalog);
	void init() throws IOException, XMPPException, ParseException, InterruptedException, InvalidSyntaxException;
	void destroy() throws IOException;
	void bind(ServiceReference service) throws XMPPException, InterruptedException;
	void unbind(ServiceReference service);
}
