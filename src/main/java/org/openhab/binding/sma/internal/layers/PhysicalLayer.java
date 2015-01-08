/**
 * Copyright (c) 2010-2014, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sma.internal.layers;

import java.io.IOException;

public interface PhysicalLayer {

	public static final short ANYSUSYID = (short) 0xFFFF;
	public static final int ANYSERIAL = 0xFFFFFFFF;
	
	public void open() throws IOException;
	public void close();
	public void send() throws IOException;
	public byte[] receive(int i) throws IOException;
	
	public void writePacketHeader(int control);
	public void writePacketTrailer();

	public void writePacket(byte longwords, byte ctrl, short ctrl2,
			short dstSUSyID, int dstSerial);

	public void write(final byte[] bytes, int loopcount);
	public void write(byte v);
	public void write(short v);
	public void write(int v);
	
	void writePacketLength();
	boolean isCrcValid();
}
