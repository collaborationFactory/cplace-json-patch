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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;

import java.io.IOException;

/**
 * JSON Path {@code remove} operation
 *
 * <p>This operation only takes one pointer ({@code path}) as an argument. It
 * is an error condition if no JSON value exists at that pointer.</p>
 */
public final class RemoveOperation
    extends JsonPatchOperation
{
    private final JsonNode value;

    @JsonCreator
    public RemoveOperation(@JsonProperty("path") final JsonPointer path, @JsonProperty(value = "value") final JsonNode value)
    {
        super("remove", path);
        this.value = value != null ? value : NullNode.getInstance();
    }

    @Override
    public JsonNode apply(final JsonNode node)
        throws JsonPatchException
    {
        if (path.isEmpty())
            return MissingNode.getInstance();
        if (path.path(node).isMissingNode())
            throw new JsonPatchException(BUNDLE.getMessage(
                    "jsonPatch.noSuchPath"));
        final JsonNode ret = node.deepCopy();
        final JsonNode parentNode = path.parent().get(ret);
        final String fieldName = Iterables.getLast(path).getToken().getRaw();

        if (parentNode.isObject())
            removeFromObject(parentNode, fieldName);
        else
            ((ArrayNode) parentNode).remove(Integer.parseInt(fieldName));
        return ret;
    }

    private void removeFromObject(JsonNode parentNode, String fieldName) {
        final JsonNode targetObject = parentNode.get(fieldName);
        if (targetObject.isArray()) {
            removeFromArray((ObjectNode) parentNode, fieldName, (ArrayNode) targetObject);
        } else {
            ((ObjectNode) parentNode).remove(fieldName);
        }
    }

    private void removeFromArray(ObjectNode parentNode, String fieldName, ArrayNode targetObject) {
        if (value.isNull()) {
            parentNode.remove(fieldName);
        }
        else if (value.isArray()) {
            final ArrayNode arr = remove(targetObject, (ArrayNode) value);
            parentNode.replace(fieldName, arr);
        } else {
            final ArrayNode arr = remove(targetObject, value);
            parentNode.replace(fieldName, arr);
        }
    }

    private ArrayNode remove(ArrayNode arrayObject, JsonNode toRemove) {
        ArrayNode reduced = arrayObject.deepCopy();
        int i = 0;
        for (JsonNode e : arrayObject) {
            if (e.equals(toRemove)) {
                reduced.remove(i);
            }
            i++;
        }
        return reduced;
    }

    private ArrayNode remove(ArrayNode arrayObject, ArrayNode toRemove) {
        ArrayNode reduced = arrayObject.deepCopy();
        for (JsonNode rem : toRemove) {
            reduced = remove(reduced, rem);
        }
        return reduced;
    }

    @Override
    public void serialize(final JsonGenerator jgen,
        final SerializerProvider provider)
        throws IOException, JsonProcessingException
    {
        jgen.writeStartObject();
        jgen.writeStringField("op", "remove");
        jgen.writeStringField("path", path.toString());
        if (value != null && !value.equals(NullNode.getInstance())) {
            jgen.writeFieldName("value");
            jgen.writeTree(value);
        }
        jgen.writeEndObject();
    }

    @Override
    public void serializeWithType(final JsonGenerator jgen,
        final SerializerProvider provider, final TypeSerializer typeSer)
        throws IOException, JsonProcessingException
    {
        serialize(jgen, provider);
    }

    @Override
    public String toString()
    {
        return "op: " + op + "; path: \"" + path + '"';
    }
}
