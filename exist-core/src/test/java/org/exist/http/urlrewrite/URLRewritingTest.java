/*
 * eXist-db Open Source Native XML Database
 * Copyright (C) 2001 The eXist-db Authors
 *
 * info@exist-db.org
 * http://www.exist-db.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.exist.http.urlrewrite;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.exist.TestUtils;
import org.exist.test.ExistWebServer;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.exist.xmldb.XmldbURI;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.exist.http.urlrewrite.XQueryURLRewrite.XQUERY_CONTROLLER_FILENAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class URLRewritingTest {

    private static final XmldbURI TEST_COLLECTION_NAME = XmldbURI.create("controller-test");
    private static final XmldbURI TEST_COLLECTION = XmldbURI.create("/db/apps").append(TEST_COLLECTION_NAME);

    private static final String TEST_CONTROLLER = "xquery version \"3.1\";\n<controller>{fn:current-dateTime()}</controller>";
    private static Executor executor = null;

    @ClassRule
    public static final ExistWebServer existWebServer = new ExistWebServer(true, false, true, true, false);

    @Test
    public void findsParentController() throws IOException {
        final XmldbURI nestedCollectionName = XmldbURI.create("nested");
        final XmldbURI docName = XmldbURI.create("test.xml");
        final String testDocument = "<hello>world</hello>";

        final String storeDocUri = getRestUri() + TEST_COLLECTION.append(nestedCollectionName).append(docName);
        HttpResponse response = executor.execute(Request
                .Put(storeDocUri)
                .bodyString(testDocument, ContentType.APPLICATION_XML)
        ).returnResponse();
        assertEquals(HttpStatus.SC_CREATED, response.getStatusLine().getStatusCode());

        final String retrieveDocUri = getAppsUri() + "/" + TEST_COLLECTION_NAME.append(nestedCollectionName).append(docName);
        response = executor.execute(Request
                .Get(retrieveDocUri)
        ).returnResponse();
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        final String responseBody;
        try (final UnsynchronizedByteArrayOutputStream baos = new UnsynchronizedByteArrayOutputStream((int)response.getEntity().getContentLength())) {
            response.getEntity().writeTo(baos);
            responseBody = baos.toString(UTF_8);
        }
        assertTrue(responseBody.matches("<controller>.+</controller>"));
    }

    @BeforeClass
    public static void setup() throws IOException {
        executor = Executor.newInstance()
                .auth(TestUtils.ADMIN_DB_USER, TestUtils.ADMIN_DB_PWD)
                .authPreemptive("localhost");

        final HttpResponse response = executor.execute(Request
                .Put(getRestUri() + TEST_COLLECTION + "/" + XQUERY_CONTROLLER_FILENAME)
                .bodyString(TEST_CONTROLLER, ContentType.create("application/xquery"))
        ).returnResponse();
        assertEquals(HttpStatus.SC_CREATED, response.getStatusLine().getStatusCode());
    }

    @AfterClass
    public static void cleanup() throws IOException {

        final HttpResponse response = executor.execute(Request
                .Delete(getRestUri() + TEST_COLLECTION)
        ).returnResponse();
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
    }

    private static String getServerUri() {
        return "http://localhost:" + existWebServer.getPort() + "/exist";
    }

    private static String getRestUri() {
        return getServerUri() + "/rest";
    }

    private static String getAppsUri() {
        return getServerUri() + "/apps";
    }
}
