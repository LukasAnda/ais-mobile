/*
 * Copyright 2020 Lukáš Anda. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lukasanda.aismobile.core

import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.net.UnknownHostException
import java.security.SecureRandom
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager


class TLSSocketFactoryCompat : SSLSocketFactory {
    private var delegate: SSLSocketFactory

    constructor() {
        val context: SSLContext = SSLContext.getInstance("TLS")
        context.init(null, null, null)
        delegate = context.socketFactory
    }

    constructor(tm: Array<TrustManager?>?) {
        val context: SSLContext = SSLContext.getInstance("TLS")
        context.init(null, tm, SecureRandom())
        delegate = context.socketFactory
    }

    @Throws(IOException::class)
    override fun createSocket(): Socket? {
        return enableTLSOnSocket(delegate.createSocket())
    }

    override fun getDefaultCipherSuites(): Array<String> = delegate.defaultCipherSuites

    @Throws(IOException::class)
    override fun createSocket(s: Socket?, host: String?, port: Int, autoClose: Boolean): Socket? {
        return enableTLSOnSocket(delegate.createSocket(s, host, port, autoClose))
    }

    @Throws(IOException::class, UnknownHostException::class)
    override fun createSocket(host: String?, port: Int): Socket? {
        return enableTLSOnSocket(delegate.createSocket(host, port))
    }

    @Throws(IOException::class, UnknownHostException::class)
    override fun createSocket(host: String?, port: Int, localHost: InetAddress?, localPort: Int): Socket? {
        return enableTLSOnSocket(delegate.createSocket(host, port, localHost, localPort))
    }

    @Throws(IOException::class)
    override fun createSocket(host: InetAddress?, port: Int): Socket? {
        return enableTLSOnSocket(delegate.createSocket(host, port))
    }

    @Throws(IOException::class)
    override fun createSocket(address: InetAddress?, port: Int, localAddress: InetAddress?, localPort: Int): Socket? {
        return enableTLSOnSocket(delegate.createSocket(address, port, localAddress, localPort))
    }

    override fun getSupportedCipherSuites(): Array<String> = delegate.supportedCipherSuites

    private fun enableTLSOnSocket(socket: Socket?): Socket? {
        if (socket != null && socket is SSLSocket) { //Create list of supported protocols
            val supportedProtocols: ArrayList<String> = ArrayList()
            for (protocol in socket.enabledProtocols) { //Log.d("TLSSocketFactory", "Supported protocol:" + protocol);
//Only add TLS protocols (don't want ot support older SSL versions)
                if (protocol.toUpperCase().contains("TLS")) {
                    supportedProtocols.add(protocol)
                }
            }
            //Force add TLSv1.1 and 1.2 if not already added
            if (!supportedProtocols.contains("TLSv1.1")) {
                supportedProtocols.add("TLSv1.1")
            }
            if (!supportedProtocols.contains("TLSv1.2")) {
                supportedProtocols.add("TLSv1.2")
            }
            val protocolArray: Array<String> = supportedProtocols.toArray(arrayOfNulls(supportedProtocols.size))
            /*for (int i = 0; i < protocolArray.length; i++) {
            Log.d("TLSSocketFactory", "protocolArray[" + i + "]" + protocolArray[i]);
        }*/
//enable protocols in our list
            (socket as SSLSocket?)?.enabledProtocols = protocolArray
        }
        return socket
    }
}