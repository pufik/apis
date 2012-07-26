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
package org.surfnet.oaaas.example.api;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;

import org.surfnet.oaaas.model.VerifyTokenResponse;
import org.surfnet.oaaas.resource.VerifyResource;

import com.google.common.base.Optional;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.core.util.Base64;
import com.yammer.dropwizard.auth.AuthenticationException;
import com.yammer.dropwizard.auth.Authenticator;

/**
 * {@link Authenticator} that ask the Authorization Server to check
 * 
 */
public class OAuthAuthenticator implements Authenticator<String, Principal> {

  private String authorizationServerUrl;
  private String authorizationValue;

  private Client client = Client.create();

  /**
   * @param configuration
   */
  public OAuthAuthenticator(UniversityFooConfiguration configuration) {
    AuthConfiguration auth = configuration.getAuth();
    authorizationServerUrl = auth.getAuthorizationServerUrl();
    authorizationValue = new String(Base64.encode(auth.getName().concat(":").concat(auth.getAccessToken())));
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.yammer.dropwizard.auth.Authenticator#authenticate(java.lang.Object)
   */
  @Override
  public Optional<Principal> authenticate(String accessToken) throws AuthenticationException {
    final VerifyTokenResponse response = client
        .resource(String.format(authorizationServerUrl.concat("?access_token=%s"), accessToken))
        .header(HttpHeaders.AUTHORIZATION, authorizationValue).accept("application/json")
        .get(VerifyTokenResponse.class);
    Principal principal = null;
    if (response.getUser_id() != null) {
      principal = new Principal() {
        @Override
        public String getName() {
          return response.getUser_id();
        }
      };
    }
    return Optional.fromNullable(principal);
  }
}
