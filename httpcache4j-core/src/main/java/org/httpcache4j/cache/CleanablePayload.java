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
package org.httpcache4j.cache;

import org.httpcache4j.payload.Payload;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @author last modified by $Author: $
 * @version $Id: $
 */
public interface CleanablePayload extends Payload {
    void clean();
}