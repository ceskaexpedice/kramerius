package cz.incad.kramerius.statistics.accesslogs;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class LogRecordDetail {
    

    private String pid;
    private String model;
    private String title;
    

    public LogRecordDetail(String pid, String model) {
        super();
        this.pid = pid;
        this.model = model;
    }

    public String getPid() {
        return pid;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getTitle() {
        return title;
    }
    
    
    
    @Override
    public int hashCode() {
        return Objects.hash(model, pid, title);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LogRecordDetail other = (LogRecordDetail) obj;
        return Objects.equals(model, other.model) && Objects.equals(pid, other.pid)
                && Objects.equals(title, other.title);
    }

    public static LogRecordDetail buildDetail(String pid, String model) {
        return new LogRecordDetail(pid, model);
    }

}
