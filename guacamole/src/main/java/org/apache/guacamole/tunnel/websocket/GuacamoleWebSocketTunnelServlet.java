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

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.io.GuacamoleReader;
import org.apache.guacamole.io.GuacamoleWriter;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.protocol.GuacamoleStatus;
import org.apache.guacamole.tunnel.http.HTTPTunnelRequest;
import org.apache.guacamole.tunnel.TunnelRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerEndpoint(value = "/websocket-tunnel")
public abstract class GuacamoleWebSocketTunnelServlet {

    private static final Logger logger = LoggerFactory.getLogger(GuacamoleWebSocketTunnelServlet.class);
    private static final int BUFFER_SIZE = 8192;

    private Session session;
    private GuacamoleTunnel tunnel;

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;

        try {
            // Get HTTP request from endpoint config
            HttpServletRequest request = (HttpServletRequest) config.getUserProperties().get("HttpServletRequest");
            TunnelRequest tunnelRequest = new HTTPTunnelRequest(request);

            // Create tunnel
            tunnel = doConnect(tunnelRequest);
            if (tunnel == null) {
                closeConnection(session, GuacamoleStatus.RESOURCE_NOT_FOUND);
                return;
            }

            // Start reading from tunnel
            GuacamoleReader reader = tunnel.acquireReader();
            new ReadThread(reader, session).start();

        } catch (GuacamoleException e) {
            logger.error("Error connecting WebSocket tunnel: {}", e.getMessage());
            logger.debug("Error connecting WebSocket tunnel.", e);
            closeConnection(session, e.getStatus());
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            GuacamoleWriter writer = tunnel.acquireWriter();
            writer.write(message.toCharArray());
            tunnel.releaseWriter();
        } catch (GuacamoleException e) {
            logger.debug("WebSocket tunnel write failed.", e);
            closeConnection(session, e.getStatus());
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        try {
            if (tunnel != null) {
                tunnel.close();
            }
        } catch (GuacamoleException e) {
            logger.debug("Unable to close connection to guacd.", e);
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.error("Error on WebSocket connection: {}", throwable.getMessage());
        closeConnection(session, GuacamoleStatus.SERVER_ERROR);
    }

    private void closeConnection(Session session, GuacamoleStatus status) {
        try {
            session.close(new CloseReason(
                    CloseReason.CloseCodes.NORMAL_CLOSURE,
                    Integer.toString(status.getGuacamoleStatusCode())
            ));
        } catch (Exception e) {
            logger.debug("Unable to close WebSocket connection.", e);
        }
    }

    protected abstract GuacamoleTunnel doConnect(TunnelRequest request) throws GuacamoleException;

    private class ReadThread extends Thread {
        private final GuacamoleReader reader;
        private final Session session;

        public ReadThread(GuacamoleReader reader, Session session) {
            this.reader = reader;
            this.session = session;
        }

        @Override
        public void run() {
            try {
                char[] buffer;
                while ((buffer = reader.read()) != null) {
                    if (session.isOpen()) {
                        session.getBasicRemote().sendText(new String(buffer));
                    }
                }
            } catch (Exception e) {
                logger.debug("WebSocket tunnel read failed.", e);
                closeConnection(session, GuacamoleStatus.SERVER_ERROR);
            } finally {
                tunnel.releaseReader();
            }
        }
    }
}
