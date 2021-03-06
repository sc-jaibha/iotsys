/*******************************************************************************
 * Copyright (c) 2013
 * Institute of Computer Aided Automation, Automation Systems Group, TU Wien.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the Institute nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * 
 * This file is part of the IoTSyS project.
 ******************************************************************************/

package at.ac.tuwien.auto.iotsys.gateway.obix.server;

import java.net.URI;
import java.util.logging.Logger;

import at.ac.tuwien.auto.iotsys.commons.ObjectBroker;
import at.ac.tuwien.auto.iotsys.commons.persistent.UIDb;
import at.ac.tuwien.auto.iotsys.commons.persistent.UIDbImpl;
import at.ac.tuwien.auto.iotsys.commons.persistent.WriteableObjectDb;
import at.ac.tuwien.auto.iotsys.commons.persistent.WriteableObjectDbImpl;
import obix.Err;
import obix.Obj;
import obix.Uri;
import obix.io.ObixDecoder;
import obix.xml.XException;

public class ObixServerImpl implements ObixServer {
	private static final Logger log = Logger.getLogger(ObixServerImpl.class
			.getName());
	
	private ObjectBroker objectBroker;
	private UIDb uidb;
	
	@Override
	public UIDb getUidb() {
		return UIDbImpl.getInstance();
	}

	@Override
	public ObjectBroker getObjectBroker() {
		return objectBroker;
	}

	public ObixServerImpl(ObjectBroker objectBroker){
		this.objectBroker = objectBroker;
	}

	public String getIPv6LinkedHref(String ipv6Address) {
		return objectBroker.getIPv6LinkedHref(ipv6Address);
	}

	public boolean containsIPv6(String ipv6Address) {
		return objectBroker.containsIPv6(ipv6Address);

	}

	public Obj readObj(URI href, boolean refreshObject) {
		Obj o = objectBroker.pullObj(new Uri(href.toASCIIString()), refreshObject);
		return o;
	}

	public String getCoRELinks() {
		return objectBroker.getCoRELinks();
	}
	
	public Obj writeObj(URI href, String xmlStream) {
		log.info("Writing on object " + href);
		log.finer("Writing on object: " + href + " xmlStream: " + xmlStream);
		// persisting the write
		WriteableObjectDbImpl.getInstance().persistWritingObject(href.toASCIIString(), xmlStream);
		
		return applyObj(href, xmlStream);
	}
	
	public Obj applyObj(URI href, String xmlStream){
		try {
			Obj input = ObixDecoder.fromString(xmlStream);
			objectBroker.pushObj(new Uri(href.toASCIIString()), input, false);
		} catch (XException ex) {
			return new Err("Invalid payload");
		} catch (Exception ex) {
			ex.printStackTrace();
			Err e = new Err("Error writing object to network: " + ex.getMessage());
			return e;
		}

		return objectBroker.pullObj(new Uri(href.toASCIIString()), false);
	}

	public Obj invokeOp(URI href, String xmlStream) {
		Obj input = null;
		try {
			if (xmlStream != null && xmlStream.trim().length() > 0) {
				input = ObixDecoder.fromString(xmlStream);
			}
			
			Obj o = objectBroker.invokeOp(new Uri(href.toASCIIString()), input);
			return o;
		} catch (XException ex) {
			return new Err("Invalid payload");
		} catch (Exception ex) {
			Err e = new Err("Error invoking operation: " + ex.getMessage());
			ex.printStackTrace();
			return e;
		}
	}
	
	@Override
	public String getNormalizedPath(String href) {
		Obj o = objectBroker.pullObj(new Uri(href), false);
		if (o == null) return null;
		
		return o.getFullContextPath();
	}

}
