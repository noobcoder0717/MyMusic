package event;

public class NeedPayEvent {
    private String message;
    public NeedPayEvent(String messsage){
        this.message=messsage;
    }

    public String getMessage() {
        return message;
    }
}
