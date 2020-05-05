package cz.incad.kramerius.security;

import java.io.Serializable;
import java.util.Objects;

public class RightsReturnObject implements Serializable  {

    private static final long serialVersionUID = 1L;


    private Right right;
    private EvaluatingResultState state;

    public RightsReturnObject(Right right, EvaluatingResultState state) {
        this.right = right;
        this.state = state;
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
