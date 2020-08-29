package com.example.client;

import com.example.client.requests.Request;
import com.example.client.requests.RequestClass;
import com.example.client.requests.RequestFactory;
import com.example.client.requests.RequestGetDeviceStatus;
import com.example.client.requests.RequestGetVersion;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

import java.lang.reflect.Method;
import java.util.Set;

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
    public void shouldTestingReflection() {
        Reflections reflections = new Reflections("com.example.client", new MethodAnnotationsScanner());
        Set<Method> annotated = reflections.getMethodsAnnotatedWith(Test.class);
        for (Method c : annotated) {
            System.out.println(c.getName());
        }
    }

    @Test
    public void shouldTestingReflection2() {
        Reflections reflections = new Reflections("com.example.client.requests");
        Set<Class<? extends Request>> classes = reflections.getSubTypesOf(Request.class);
        for (Class<? extends Request> c : classes)
            System.out.println(c.getSimpleName());
    }

    @Test
    public void shouldTestingReflection3() {
        Reflections reflections = new Reflections("com.example.client.requests");
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(RequestClass.class);
        System.out.println("There:");
        for (Class<?> c : classes)
            System.out.println(c.getSimpleName());
    }

    @Test
    public void shouldCreateCorrespondingRequest() throws JSONException {
        JSONObject request = new JSONObject("{ \"Request\": \"GetVersion\" }");

        assertTrue(RequestFactory.parseJSONRequest(request) instanceof RequestGetVersion);
        assertFalse(RequestFactory.parseJSONRequest(request) instanceof RequestGetDeviceStatus);
    }


}