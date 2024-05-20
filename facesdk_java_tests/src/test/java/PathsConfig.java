import com.regula.facesdk.webclient.FaceSdk;

import java.io.Console;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.lang.System;

public class PathsConfig {
    public static final String API_BASE_PATH = System.getProperty("apiBasePath", "http://localhost:41101/");
    static final FaceSdk faceSdk = new FaceSdk(PathsConfig.API_BASE_PATH);
    private static final String PROJECT_DIR = System.getProperty("user.dir");
    private static final Path FILES_PATH = Paths.get(PROJECT_DIR, "misc", "files");
    public static final Path FACE1_PATH = FILES_PATH.resolve("face1.jpg");
    public static final Path FACE2_PATH = FILES_PATH.resolve("face2.jpg");
    public static final Path FACE3_PATH = FILES_PATH.resolve("face3.jpg");
    public static final Path LIVE_PHOTO_PATH = FILES_PATH.resolve("me.png");
    public static final Path PRINTED_DOCUMENT_PATH = FILES_PATH.resolve("printedDoc.png");
    public static final Path DOCUMENT_WITH_LIVE_PATH = FILES_PATH.resolve("me_and_id.png");
    public static final Path SEVERAL_FACES_IMAGE_PATH = FILES_PATH.resolve("severalFaces.jpg");

    public static byte[] readImageBytes(Path path) throws IOException {
        return Files.readAllBytes(path);
    }
}
