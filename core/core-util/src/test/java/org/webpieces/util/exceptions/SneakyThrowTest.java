package org.webpieces.util.exceptions;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

public class SneakyThrowTest {

    @Test
    public void testSneakCompletionException() {

        Assert.assertThrows(RuntimeException.class, () -> {

            RuntimeException expected = new RuntimeException("expected");
            CompletionException ex1 = new CompletionException("ex1", expected);

            throw SneakyThrow.sneak(ex1);

        });

    }

    @Test
    public void testSneakExecutionException() {

        Assert.assertThrows(RuntimeException.class, () -> {

            RuntimeException expected = new RuntimeException("expected");
            ExecutionException ex1 = new ExecutionException("ex1", expected);

            throw SneakyThrow.sneak(ex1);

        });

    }

    @Test
    public void testSneakInvocationTargetException() {

        Assert.assertThrows(RuntimeException.class, () -> {

            RuntimeException expected = new RuntimeException("expected");
            InvocationTargetException ex1 = new InvocationTargetException(expected, "ex1");

            throw SneakyThrow.sneak(ex1);

        });

    }

    @Test
    public void testSneakNestedExceptions() {

        Assert.assertThrows(RuntimeException.class, () -> {

            RuntimeException expected = new RuntimeException("expected");
            InvocationTargetException ex2 = new InvocationTargetException(expected, "ex2");
            CompletionException ex1 = new CompletionException("ex1", ex2);

            throw SneakyThrow.sneak(ex1);

        });

    }

}