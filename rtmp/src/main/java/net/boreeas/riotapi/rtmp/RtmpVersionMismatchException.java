/*
 * Copyright 2014 The LolDevs team (https://github.com/loldevs)
 *
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

package net.boreeas.riotapi.rtmp;

import java.net.ProtocolException;

/**
 * Created on 5/24/2014.
 */
public class RtmpVersionMismatchException extends RtmpException {
    public RtmpVersionMismatchException(int serverVersion, int rtmpVersion) {
        super("Got server version " + serverVersion + " but expected " + rtmpVersion);
    }
}
