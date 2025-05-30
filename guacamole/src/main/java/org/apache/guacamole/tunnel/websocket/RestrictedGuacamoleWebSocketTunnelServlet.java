/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.guacamole.tunnel.websocket;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.tunnel.TunnelRequestService;
import org.apache.guacamole.tunnel.TunnelRequest;

/**
 * Tunnel servlet implementation which uses WebSocket as a tunnel backend,
 * rather than HTTP, properly parsing connection IDs included in the connection
 * request.
 */
@Singleton
public class RestrictedGuacamoleWebSocketTunnelServlet extends GuacamoleWebSocketTunnelServlet {

    @Inject
    private TunnelRequestService tunnelRequestService;

    @Override
    protected GuacamoleTunnel doConnect(TunnelRequest request) throws GuacamoleException {
        return tunnelRequestService.createTunnel(request);
    }
}
