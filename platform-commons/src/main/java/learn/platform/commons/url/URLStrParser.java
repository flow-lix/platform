/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package learn.platform.commons.url;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class URLStrParser {

    private static final char SPACE = 0x20;

    private static final ThreadLocal<TempBuf> DECODE_TEMP_BUF = ThreadLocal.withInitial(() -> new TempBuf(1024));

    private URLStrParser() {
        //empty
    }

    /**
     * @param decodedURLStr : protocol://username:password@host:port/path?k1=v1&k2=v2
     */
    public static UrlResource parseDecodedStr(String decodedURLStr) {
        Map<String, Object> parameters = null;
        int pathEndIdx = decodedURLStr.indexOf('?');
        if (pathEndIdx >= 0) {
            parameters = parseDecodedParams(decodedURLStr, pathEndIdx + 1);
        } else {
            pathEndIdx = decodedURLStr.length();
        }

        String decodedBody = decodedURLStr.substring(0, pathEndIdx);
        return parseURLBody(decodedURLStr, decodedBody, parameters);
    }

    private static Map<String, Object> parseDecodedParams(String str, int from) {
        int len = str.length();
        if (from >= len) {
            return Collections.emptyMap();
        }

        TempBuf tempBuf = DECODE_TEMP_BUF.get();
        Map<String, Object> params = new HashMap<>();
        int nameStart = from;
        int valueStart = -1;
        int i;
        for (i = from; i < len; i++) {
            char ch = str.charAt(i);
            switch (ch) {
                case '=':
                    if (nameStart == i) {
                        nameStart = i + 1;
                    } else if (valueStart < nameStart) {
                        valueStart = i + 1;
                    }
                    break;
                case ';':
                case '&':
                    addParam(str, false, nameStart, valueStart, i, params, tempBuf);
                    nameStart = i + 1;
                    break;
                default:
                    // continue
            }
        }
        addParam(str, false, nameStart, valueStart, i, params, tempBuf);
        return params;
    }

    /**
     * @param fullUrlStr  : fullURLString
     * @param decodedBody : format: [protocol://][username:password@][host:port]/[path]
     * @param parameters  :
     * @return URL
     */
    private static UrlResource parseURLBody(String fullUrlStr, String decodedBody, Map<String, Object> parameters) {
        int starIdx = 0, endIdx = decodedBody.length();
        String protocol = null;
        int protoEndIdx = decodedBody.indexOf("://");
        if (protoEndIdx >= 0) {
            if (protoEndIdx == 0) {
                throw new IllegalStateException("url missing protocol: \"" + fullUrlStr + "\"");
            }
            protocol = decodedBody.substring(0, protoEndIdx);
            starIdx = protoEndIdx + 3;
        } else {
            // case: file:/path/to/file.txt
            protoEndIdx = decodedBody.indexOf(":/");
            if (protoEndIdx >= 0) {
                if (protoEndIdx == 0) {
                    throw new IllegalStateException("url missing protocol: \"" + fullUrlStr + "\"");
                }
                protocol = decodedBody.substring(0, protoEndIdx);
                starIdx = protoEndIdx + 1;
            }
        }

        String path = null;
        int pathStartIdx = indexOf(decodedBody, '/', starIdx, endIdx);
        if (pathStartIdx >= 0) {
            path = decodedBody.substring(pathStartIdx + 1);
            endIdx = pathStartIdx;
        }

        String username = null;
        String password = null;
        int pwdEndIdx = lastIndexOf(decodedBody, '@', starIdx, endIdx);
        if (pwdEndIdx > 0) {
            int userNameEndIdx = indexOf(decodedBody, ':', starIdx, pwdEndIdx);
            username = decodedBody.substring(starIdx, userNameEndIdx);
            password = decodedBody.substring(userNameEndIdx + 1, pwdEndIdx);
            starIdx = pwdEndIdx + 1;
        }

        String host = null;
        int port = 0;
        int hostEndIdx = lastIndexOf(decodedBody, ':', starIdx, endIdx);
        if (hostEndIdx > 0 && hostEndIdx < decodedBody.length() - 1) {
            if (lastIndexOf(decodedBody, '%', starIdx, endIdx) > hostEndIdx) {
                // ipv6 address with scope id
                // e.g. fe80:0:0:0:894:aeec:f37d:23e1%en0
                // see https://howdoesinternetwork.com/2013/ipv6-zone-id
                // ignore
            } else {
                port = Integer.parseInt(decodedBody.substring(hostEndIdx + 1, endIdx));
                endIdx = hostEndIdx;
            }
        }

        if (endIdx > starIdx) {
            host = decodedBody.substring(starIdx, endIdx);
        }
        UrlResource resource = new UrlResource(protocol, username, password, host, port, path);
        resource.putParameters(parameters);
        return resource;
    }

    private static boolean addParam(String str, boolean isEncoded, int nameStart, int valueStart, int valueEnd, Map<String, Object> params,
                                    TempBuf tempBuf) {
        if (nameStart >= valueEnd) {
            return false;
        }

        if (valueStart <= nameStart) {
            valueStart = valueEnd + 1;
        }
        String name = str.substring(nameStart, valueStart -1);
        String value = str.substring(valueStart, valueEnd);
        params.put(name, value);
        return true;
    }

    private static int indexOf(String str, char ch, int from, int toExclude) {
        from = Math.max(from, 0);
        toExclude = Math.min(toExclude, str.length());
        if (from > toExclude) {
            return -1;
        }

        for (int i = from; i < toExclude; i++) {
            if (str.charAt(i) == ch) {
                return i;
            }
        }
        return -1;
    }

    private static int lastIndexOf(String str, char ch, int from, int toExclude) {
        from = Math.max(from, 0);
        toExclude = Math.min(toExclude, str.length() - 1);
        if (from > toExclude) {
            return -1;
        }

        for (int i = toExclude; i >= from; i--) {
            if (str.charAt(i) == ch) {
                return i;
            }
        }
        return -1;
    }

    private static final class TempBuf {

        private final char[] chars;

        private final byte[] bytes;

        TempBuf(int bufSize) {
            this.chars = new char[bufSize];
            this.bytes = new byte[bufSize];
        }

        public char[] charBuf(int size) {
            char[] chars = this.chars;
            if (size <= chars.length) {
                return chars;
            }
            return new char[size];
        }

        public byte[] byteBuf(int size) {
            byte[] bytes = this.bytes;
            if (size <= bytes.length) {
                return bytes;
            }
            return new byte[size];
        }
    }
}
