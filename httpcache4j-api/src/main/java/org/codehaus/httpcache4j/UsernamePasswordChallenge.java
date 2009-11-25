/*
 * Copyright (c) 2008, The Codehaus. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.codehaus.httpcache4j;

/**
 * Represents a Username and password challenge.
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @author last modified by $Author: $
 * @version $Id: $
 */
public class UsernamePasswordChallenge implements Challenge {
    private String identifier;
    private char[] password;

    public UsernamePasswordChallenge(String identifier, char[] password) {
        this.identifier = identifier;
        this.password = password != null ? password.clone() : null;
    }

    public UsernamePasswordChallenge(String identifier, String password) {
        this.identifier = identifier;
        this.password = password != null ? password.toCharArray() : null;
    }

    public String getIdentifier() {
        return identifier;
    }

    public char[] getPassword() {
        return password != null ? password.clone() : null;
    }
    
    @Override
    public String toString() {
        return String.format("Authenticating as %s ", identifier);
    }
}