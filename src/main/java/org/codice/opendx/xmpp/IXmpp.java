package org.codice.opendx.xmpp;

import java.io.IOException;

import org.apache.lucene.queryparser.classic.ParseException;
import org.jivesoftware.smack.XMPPException;

import ddf.catalog.CatalogFramework;

public interface IXmpp {
	
	void setCatalog(CatalogFramework catalog);
	void init() throws IOException, XMPPException, ParseException, InterruptedException;
	void destroy() throws IOException;

}
