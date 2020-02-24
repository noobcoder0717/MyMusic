package event;

public class LoadFinishedEvent {
    private String message;
    public LoadFinishedEvent(String msg){
        message=msg;
    }
    public String getMessage(){
        return message;
    }
}
