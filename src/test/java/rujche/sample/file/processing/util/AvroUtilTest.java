package rujche.sample.file.processing.util;

import rujche.sample.file.processing.avro.generated.User;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AvroUtilTest {

    private static final String USER_1_TXT_STRING = "User1,blue,1";
    private static final User USER_1 = User.newBuilder()
            .setName("User1")
            .setFavoriteColor("blue")
            .setFavoriteNumber(1)
            .build();

    @Test
    public void userWriteToFileThenReadTest() throws IOException {
        File file = File.createTempFile("AvroUtilTest-", ".avro");
        AvroUtil.writeUserToAvroFile(USER_1, file);
        User user = AvroUtil.readUserFromAvroFile(file);
        assertEquals(USER_1, user);
    }

    @Test
    public void toUserTest() {
        assertEquals(USER_1, AvroUtil.toUser("testFile", 0, USER_1_TXT_STRING));
    }

    @Test
    public void userToAvroBytesAndFromAvroBytesTest() throws IOException {
        assertEquals(USER_1, AvroUtil.fromAvroBytes(AvroUtil.userToAvroBytes("testFile", 0, USER_1)));
    }
}
