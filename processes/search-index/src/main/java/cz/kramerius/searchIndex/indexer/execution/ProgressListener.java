package cz.kramerius.searchIndex.indexer.execution;

public interface ProgressListener {

    public void onProgress(int processed);

    public void onFinished(int processed);

}
