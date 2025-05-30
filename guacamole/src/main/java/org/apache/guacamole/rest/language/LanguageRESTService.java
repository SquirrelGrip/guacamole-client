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

package org.apache.guacamole.rest.language;

import java.util.Map;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.apache.guacamole.extension.LanguageResourceService;


/**
 * A REST Service for handling the listing of languages.
 */
@Path("/languages")
@Produces(MediaType.APPLICATION_JSON)
public class LanguageRESTService {

    /**
     * Service for retrieving information regarding available language
     * resources.
     */
    @Inject
    private LanguageResourceService languageResourceService;

    /**
     * Returns a map of all available language keys to their corresponding
     * human-readable names.
     * 
     * @return
     *     A map of languages defined in the system, of language key to 
     *     display name.
     */
    @GET
    public Map<String, String> getLanguages() {
        return languageResourceService.getLanguageNames();
    }

}
