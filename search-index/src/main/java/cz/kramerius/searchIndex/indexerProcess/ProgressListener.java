package cz.kramerius.searchIndex.indexerProcess;

public interface ProgressListener {

    public void onProgress(int processed, int total);

    public void onFinished(int processed, int total);

}
