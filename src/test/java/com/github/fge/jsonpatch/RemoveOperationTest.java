/*
 * Copyright (c) 2014, Francis Galiegue (fgaliegue@gmail.com)
 *
 * This software is dual-licensed under:
 *
 * - the Lesser General Public License (LGPL) version 3.0 or, at your option, any
 *   later version;
 * - the Apache Software License (ASL) version 2.0.
 *
 * The text of this file and of both licenses is available at the root of this
 * project or, if you have the jar distribution, in directory META-INF/, under
 * the names LGPL-3.0.txt and ASL-2.0.txt respectively.
 *
 * Direct link to the sources:
 *
 * - LGPL 3.0: https://www.gnu.org/licenses/lgpl-3.0.txt
 * - ASL 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * NOTE: This file has been modified by collaboration Factory AG.
 */

package com.github.fge.jsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.github.fge.jackson.JacksonUtils;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.*;

public final class RemoveOperationTest
    extends JsonPatchOperationTest
{
    public RemoveOperationTest()
        throws IOException
    {
        super("remove");
    }

    @Test
    public void removingRootReturnsMissingNode()
        throws JsonPatchException
    {
        final JsonNode node = JacksonUtils.nodeFactory().nullNode();
        final JsonPatchOperation op = new RemoveOperation(JsonPointer.empty(), NullNode.getInstance());
        final JsonNode ret = op.apply(node);
        assertTrue(ret.isMissingNode());
    }
}
