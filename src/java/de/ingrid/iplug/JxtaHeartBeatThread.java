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

import java.io.FileInputStream;

import net.weta.components.communication.ICommunication;
import net.weta.components.peer.StartJxtaConfig;

/**
 * {@link de.ingrid.iplug.HeartBeatThread} that uses an jxta implementaion of
 * {@link net.weta.components.communication.ICommunication}.
 * 
 * <p/>created on 03.04.2006
 * 
 * @version $Revision: $
 * @author jz
 * @author $Author: ${lastedit}
 * 
 */
public class JxtaHeartBeatThread extends HeartBeatThread {

    private String fJxtaConf;

    private String fIBusUrl;

    private String fPlugUrl;

    /**
     * Constructor for heart beat using jxta configuration.
     * 
     * @param jxtaConf
     * @param busUrl
     * @throws Throwable
     */
    public JxtaHeartBeatThread(String jxtaConf, String busUrl) throws Throwable {
        this.fJxtaConf = jxtaConf;
        this.fIBusUrl = busUrl;
    }

    protected String getIBusUrl() {
        return this.fIBusUrl;
    }

    protected ICommunication initCommunication() throws Exception {
        this.fPlugUrl = this.fPlugDescripion.getProxyServiceURL();
        FileInputStream confIS = new FileInputStream(this.fJxtaConf);
        ICommunication communication = StartJxtaConfig.start(confIS);
        communication.subscribeGroup(this.fPlugUrl);
        communication.subscribeGroup(this.fIBusUrl);

        return communication;
    }

}
