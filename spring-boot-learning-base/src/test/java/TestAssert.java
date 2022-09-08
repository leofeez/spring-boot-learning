import cn.hutool.core.util.StrUtil;
import constant.RCode;
import core.BizAssert;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.junit.Test;

/**
 * @author leofee
 */
public class TestAssert {

    @Test
    public void test() {
        User user = null;
//        RCode.USER_NOT_FOUND.notNull(user, "leofee");

        user = new User();

//        RCode.MUST_NOT_BE_BLANK.notBlank(user.getName(), "用户名称");

        RCode.MUST_NOT_BE_BLANK.state(StrUtil.isNotBlank(user.getName()), "用户名称");

        user.setStatus(UserStatus.INVALID.getStatus());
        RCode.USER_STATUS_INCORRECT.eq(user.getStatus(), UserStatus.ACTIVE.getStatus(), UserStatus.of(user.getStatus()).getName());
    }

    @Data
    static class User {
        String name;
        String status;
    }

    @Getter
    @AllArgsConstructor
    enum UserStatus {
        INVALID("00", "作废"),
        ACTIVE("20", "生效"),

        ;

        private String status;
        private String name;

        public static UserStatus of(String status) {
            for (UserStatus value : values()) {
                if (value.getStatus().equals(status)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("未知的用户状态" + status);
        }
    }
}
