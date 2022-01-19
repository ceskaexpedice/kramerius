package cz.incad.kramerius.audio;

/**
 * @deprecated use AudioStreamForwardingHelper instead
 */
public interface ResponseHeaderForwarder {

    public String forwardHeaderIfPresent(String headerName);

}
