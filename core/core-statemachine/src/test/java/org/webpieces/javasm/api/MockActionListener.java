package org.webpieces.javasm.api;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;
import org.webpieces.mock.ParametersPassedIn;

public class MockActionListener extends MockSuperclass implements ActionListener {

	enum Method implements MethodEnum {
		ACTION_PERFORMED
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		super.calledMethod(Method.ACTION_PERFORMED, e);
	}

	public List<ActionEvent> getMethodParameters() {
		Stream<ParametersPassedIn> calledMethods2 = super.getCalledMethods(Method.ACTION_PERFORMED);
		return calledMethods2
				.map(p -> (ActionEvent)p.getArgs()[0])
				.collect(Collectors.toList());
	}
	
    public void expectNoMethodCalls() {
        Assert.assertEquals(0, getMethodParameters().size());
        clear();
    }

	public void expectOneMethodCall() {
        Assert.assertEquals(1, getMethodParameters().size());
        clear();
	}

	public void addThrowException(IllegalMonitorStateException illegalMonitorStateException) {
		super.addExceptionToThrow(Method.ACTION_PERFORMED, illegalMonitorStateException);
	}
}
