/* 
 * $Header: $ 
 * 
 * Copyright (C) 2008 Escenic. 
 * All Rights Reserved.  No use, copying or distribution of this 
 * work may be made except in accordance with a valid license 
 * agreement from Escenic.  This notice must be 
 * included on all copies, modifications and derivatives of this 
 * work. 
 */
package org.httpcache4j;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @author last modified by $Author: $
 * @version $Id: $
 */
public class Challenge {
    private String identifier;
    private char[] password;
    private ChallengeMethod method;

    public Challenge(String identifier, char[] password, ChallengeMethod method) {
        this.identifier = identifier;
        this.password = password != null ? password.clone() : null;
        this.method = method;
    }

    public String getIdentifier() {
        return identifier;
    }

    public char[] getPassword() {
        return password;
    }

    public ChallengeMethod getMethod() {
        return method;
    }
}