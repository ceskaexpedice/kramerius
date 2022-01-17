package cz.kramerius.searchIndex.indexerProcess;

public interface ProgressListener {

    public void onProgress(int processed);

    public void onFinished(int processed);

}
