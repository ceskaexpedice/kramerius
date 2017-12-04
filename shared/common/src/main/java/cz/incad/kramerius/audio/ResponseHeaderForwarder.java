package cz.incad.kramerius.audio;

public interface ResponseHeaderForwarder {

    public String forwardHeaderIfPresent(String headerName);

}
