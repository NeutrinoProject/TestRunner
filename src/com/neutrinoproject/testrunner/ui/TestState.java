package com.neutrinoproject.testrunner.ui;

import com.neutrinoproject.testrunner.TestRunState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by btv on 07.12.15.
 */
class TestState {
    private final Collection<String> outLines = Collections.synchronizedList(new ArrayList<>());
    private TestRunState state;

    public TestRunState getState() {
        return state;
    }

    public Collection<String> getOutLines() {
        return outLines;
    }

    public void setState(final TestRunState state) {
        this.state = state;
    }

    public void appendOutLine(final String line) {
        outLines.add(line);
    }
}
