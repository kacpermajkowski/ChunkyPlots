package pl.kacpermajkowski.ChunkyPlots.utils;

import lombok.Getter;
import lombok.Setter;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;

@Getter
public class ActionResult {
    private final Message resultMessage;
    private final boolean success;

    public ActionResult(boolean success){
        this.success = success;
        this.resultMessage = null;
    }

    public ActionResult(boolean success, Message resultMessage) {
        this.resultMessage = resultMessage;
        this.success = success;
    }

}
