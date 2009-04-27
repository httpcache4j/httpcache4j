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

package org.codehaus.httpcache4j.cache;

import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.payload.Payload;
import org.codehaus.httpcache4j.resolver.AbstractPayloadCreator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * A default implementation of the cleanable payload.
 * Most users will want to use this when they create a new ResponseResolver.
 *  
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public class DefaultPayloadCreator extends AbstractPayloadCreator {
    private FileGenerationManager fileGenerationManager;

    public DefaultPayloadCreator(final File baseDirectory) {
        this(baseDirectory, 5, 1000);
    }

    public DefaultPayloadCreator(final File baseDirectory, int numberOfGenerations, int generationSize) {
        fileGenerationManager = new FileGenerationManager(baseDirectory, numberOfGenerations, generationSize);
    }

    protected Payload createCachedPayload(URI requestURI, InputStream stream, MIMEType type) throws IOException {
        return new CleanableFilePayload(fileGenerationManager, stream, type);
    }
}
