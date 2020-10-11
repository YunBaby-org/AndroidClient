package com.example.client;

import com.example.client.requests.RequestFactory;
import com.example.client.requests.RequestGetDeviceStatus;
import com.example.client.requests.RequestGetVersion;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void shouldCreateCorrespondingRequest() throws JSONException {
        JSONObject request = new JSONObject("{ \"Request\": \"GetVersion\", \"id\": \"123\" }");

        assertTrue(RequestFactory.parseJSONRequest(request) instanceof RequestGetVersion);
        assertFalse(RequestFactory.parseJSONRequest(request) instanceof RequestGetDeviceStatus);
    }

}