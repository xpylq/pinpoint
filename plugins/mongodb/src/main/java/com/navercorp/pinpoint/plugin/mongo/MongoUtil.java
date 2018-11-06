/*
 * Copyright 2018 NAVER Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.WriteConcern;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.common.util.StringStringValue;
import org.bson.BsonDocument;
import org.bson.BsonDocumentWriter;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Map;

/**
 * @author Roy Kim
 */
public class MongoUtil {

    public static final char SEPARATOR = ',';
    private static MongoWriteConcernMapper mongoWriteConcernMapper = new MongoWriteConcernMapper();

    private MongoUtil() {
    }

    public static void recordMongoCollection(SpanEventRecorder recorder, String collectionName, String readPreferenceOrWriteConcern) {
        recorder.recordAttribute(MongoConstants.MONGO_COLLECTION_INFO, collectionName);
        recorder.recordAttribute(MongoConstants.MONGO_COLLECTION_OPTION, readPreferenceOrWriteConcern);
    }

    public static String getWriteConcern0(WriteConcern writeConcern) {

        return mongoWriteConcernMapper.getName(writeConcern);
    }

    public static void recordParsedBson(SpanEventRecorder recorder, StringStringValue stringStringValue) {
        if (stringStringValue != null) {
            recorder.recordAttribute(MongoConstants.MONGO_JSON_DATA, stringStringValue);
        }
    }

    private static Map<String, ?> getBsonKeyValueMap(Bson bson) {
        if (bson instanceof BasicDBObject) {
            return (BasicDBObject) bson;
        } else if (bson instanceof BsonDocument) {
            return (BsonDocument) bson;
        } else if (bson instanceof Document) {
            return (Document) bson;
        } else {
            return null;
        }
        //TODO leave comments for further use
//        if(arg instanceof BsonDocumentWrapper) {
//            bson.append(arg.toString());
//        }
//        if(arg instanceof CommandResult) {
//            bson.append(arg.toString());
//        }
//        if(arg instanceof RawBsonDocument) {
//            bson.append(arg.toString());
//        }
    }

    public static StringStringValue parseBson(Object[] args, boolean traceBsonBindValue) {

        if (args == null) {
            return null;
        }
        BsonDocument toSend = new BsonDocument();


        StringBuilder parsedBson = new StringBuilder();
        StringBuilder parameter = new StringBuilder(32);

        for (Object arg : args) {
            if (arg instanceof Bson) {

                final Map<String, ?> map = getBsonKeyValueMap((Bson) arg);

                if (map == null) {
                    continue;
                }
                BsonDocumentWriter writer = new BsonDocumentWriter(toSend);
                writer.writeStartDocument();

                for (Map.Entry<String, ?> entry : map.entrySet()) {

                    writer.writeName(entry.getKey());
                    writer.writeString("?");

                    if (traceBsonBindValue) {

                        appendOutputSeparator(parameter);
                        if (entry.getValue() instanceof String) {
                            parameter.append("\"");
                            parameter.append(entry.getValue());
                            parameter.append("\"");
                        } else {
                            parameter.append(entry.getValue());
                        }
                    }
                }
                writer.writeEndDocument();

                if (parsedBson.length() != 0) {
                    parsedBson.append(",");
                }
                parsedBson.append(writer.getDocument().toString());
            }
        }

        return new StringStringValue(parsedBson.toString(), parameter.toString());
    }

    private static void appendOutputSeparator(StringBuilder output) {
        if (output.length() == 0) {
            // first parameter
            return;
        }
        output.append(SEPARATOR);
    }
}

