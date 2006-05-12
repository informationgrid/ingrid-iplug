/*
 * Copyright 2004-2005 weta group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 *  $Source:  $
 */

package de.ingrid.iplug;

import net.weta.components.communication.ICommunication;
import net.weta.components.communication_sockets.SocketCommunication;
import net.weta.components.communication_sockets.util.AddressUtil;
import de.ingrid.iplug.util.PlugShutdownHook;
import de.ingrid.utils.IPlug;
import de.ingrid.utils.PlugDescription;

/**
 * {@link de.ingrid.iplug.HeartBeatThread} that uses an socket implementaion of
 * {@link net.weta.components.communication.ICommunication}.
 * 
 * <p/>created on 03.04.2006
 * 
 * @version $Revision: $
 * @author jz
 * @author $Author: ${lastedit}
 * 
 */
public class SocketHeartBeatThread extends HeartBeatThread {

    protected String fIBusHost;

    protected int fIBusPort;

    protected int fMulticastPort;

    protected int fUnicastPort;

    /**
     * Constructor for heart beat using socket configuration.
     * 
     * @param mPort
     * @param uPort
     * @param iBustHost
     * @param iBusPort
     * @param plug
     * @param shutdownHook
     */
    public SocketHeartBeatThread(int mPort, int uPort, String iBustHost, int iBusPort, IPlug plug,
            PlugShutdownHook shutdownHook) {
        super(plug, shutdownHook);
        this.fMulticastPort = mPort;
        this.fUnicastPort = uPort;
        this.fIBusHost = iBustHost;
        this.fIBusPort = iBusPort;
    }

    protected ICommunication initCommunication(PlugDescription description) throws Exception {
        SocketCommunication communication = new SocketCommunication();
        communication.setMulticastPort(this.fMulticastPort);
        communication.setUnicastPort(this.fUnicastPort);
        communication.startup();

        return communication;
    }

    protected String getIBusUrl() {
        return AddressUtil.getWetagURL(this.fIBusHost, this.fIBusPort);
    }
}
