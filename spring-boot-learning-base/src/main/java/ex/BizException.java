package ex;

import constant.RCode;
import lombok.Getter;

/**
 * @author leofee
 */
@Getter
public class BizException extends RuntimeException {

    private String code;

    public BizException() {
        super();
    }

    public BizException(String msg) {
        super(msg);
    }

    public BizException(String code, String msg) {
        super(msg);
        this.code = code;
    }

    public BizException(Throwable cause, String code, String message) {
        super(message, cause);
        this.code = code;
    }
}
