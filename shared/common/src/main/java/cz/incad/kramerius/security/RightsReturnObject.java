package cz.incad.kramerius.security;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RightsReturnObject implements Serializable  {

    private static final long serialVersionUID = 1L;

    private Right right;
    private EvaluatingResultState state;
    private Map<String,String> evaluateInfoMap = new HashMap<>();

    public RightsReturnObject(Right right, EvaluatingResultState state) {
        this.right = right;
        this.state = state;
    }

    public RightsReturnObject(Right right, EvaluatingResultState state, Map<String, String> evaluateInfoMap) {
        this.right = right;
        this.state = state;
        this.evaluateInfoMap = new HashMap<>(evaluateInfoMap);
    }

    public EvaluatingResultState getState() {
        return state;
    }

    public Right getRight() {
        return right;
    }

    public boolean flag() {
        return this.state == EvaluatingResultState.TRUE ? true: false;
    }


    public Map<String, String> getEvaluateInfoMap() {
        return evaluateInfoMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RightsReturnObject that = (RightsReturnObject) o;
        return Objects.equals(right, that.right) &&
                state == that.state;
    }

    @Override
    public int hashCode() {
        return Objects.hash(right, state);
    }
}