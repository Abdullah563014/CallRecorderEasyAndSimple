package md.habibure.dhaka.callrecorder_easyandsimple.model;

public class CallListModelClass {
    private String callIndicator;
    private String duration;
    private String name;
    private String time;
    private String file;

    public CallListModelClass(String callIndicator, String duration, String name, String time, String file) {
        this.callIndicator = callIndicator;
        this.duration = duration;
        this.name = name;
        this.time = time;
        this.file = file;
    }

    public String getCallIndicator() {
        return callIndicator;
    }

    public void setCallIndicator(String callIndicator) {
        this.callIndicator = callIndicator;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}
