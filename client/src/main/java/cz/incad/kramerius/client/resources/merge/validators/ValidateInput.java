package cz.incad.kramerius.client.resources.merge.validators;

public interface ValidateInput<T> {

    public boolean validate(T rawOutput);
}
