package com.neutrinoproject.testrunner.ui;

import com.neutrinoproject.testrunner.TestRunState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by btv on 07.12.15.
 */
class TestState {
    private final Collection<String> outLines = Collections.synchronizedList(new ArrayList<>());
    private TestRunState state;

    @Nullable
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

    public void clear() {
        state = null;
        outLines.clear();
    }
}
