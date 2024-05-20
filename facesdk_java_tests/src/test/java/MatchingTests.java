import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.epam.reportportal.junit5.ReportPortalExtension;
import com.regula.facesdk.webclient.gen.model.*;

@ExtendWith(ReportPortalExtension.class)
public class MatchingTests {

    private void validateResponse(MatchResponse response, int expectedDetectionsCount, ImageSource expectedFirst, ImageSource expectedSecond) {
        assertEquals(FaceSDKResultCode.FACER_OK, response.getCode(), "Unexpected response code");

        assertNotNull(response.getDetections(), "'detections' field should not be null");
        assertEquals(expectedDetectionsCount, response.getDetections().size(), "Incorrect number of detections");

        assertNotNull(response.getResults(), "'results' field should not be null");
        assertEquals(expectedFirst, response.getResults().get(0).getFirst(), "Unexpected 'first' value");
        if (expectedSecond != null) {
            assertEquals(expectedSecond, response.getResults().get(0).getSecond(), "Unexpected 'second' value");
        }
    }


    private MatchRequest createMatchRequest(int type1, Path path1, Integer type2, Path path2) throws IOException {
        List<MatchImage> images = new ArrayList<>();

        MatchImage image1 = new MatchImage();
        image1.setIndex(1);
        image1.data(PathsConfig.readImageBytes(path1));
        image1.setType(ImageSource.fromValue(type1));
        images.add(image1);


        if (type2 != null && path2 != null) {
            MatchImage image2 = new MatchImage();
            image2.setIndex(2);
            image2.data(PathsConfig.readImageBytes(path2));
            image2.setType(ImageSource.fromValue(type2));
            images.add(image2);
        }

        MatchRequest matchRequest = new MatchRequest();
        matchRequest.setImages(images);
        return matchRequest;
    }

    @Test
    void testMatchingAPI() throws Exception {
        MatchRequest matchRequest = createMatchRequest(3, PathsConfig.FACE1_PATH, 2, PathsConfig.FACE2_PATH);
        MatchResponse response = PathsConfig.faceSdk.matchingApi.match(matchRequest);
        validateResponse(response, 2, ImageSource.LIVE, ImageSource.DOCUMENT_RFID);
    }

    @Test
    void testLiveAndPrintedDocType() throws Exception {
        MatchRequest matchRequest = createMatchRequest(3, PathsConfig.LIVE_PHOTO_PATH, 1, PathsConfig.PRINTED_DOCUMENT_PATH);
        MatchResponse response = PathsConfig.faceSdk.matchingApi.match(matchRequest);
        validateResponse(response, 2, ImageSource.LIVE, ImageSource.DOCUMENT_PRINTED);
    }

    @Test
    void testDocWithLiveType() throws Exception {
        MatchRequest matchRequest = createMatchRequest(4, PathsConfig.DOCUMENT_WITH_LIVE_PATH, null, null);
        MatchResponse response = PathsConfig.faceSdk.matchingApi.match(matchRequest);
        validateResponse(response, 1, ImageSource.DOCUMENT_WITH_LIVE, null);
    }

    @Test
    void testExternalType() throws Exception {
        MatchRequest matchRequest = createMatchRequest(5, PathsConfig.LIVE_PHOTO_PATH, 3, PathsConfig.PRINTED_DOCUMENT_PATH);
        MatchResponse response = PathsConfig.faceSdk.matchingApi.match(matchRequest);
        validateResponse(response, 2, ImageSource.EXTERNAL, ImageSource.LIVE);
    }

    @Test
    void testDocumentRfidType() throws Exception {
        MatchRequest matchRequest = createMatchRequest(2, PathsConfig.LIVE_PHOTO_PATH, 1, PathsConfig.PRINTED_DOCUMENT_PATH);
        MatchResponse response = PathsConfig.faceSdk.matchingApi.match(matchRequest);
        validateResponse(response, 2, ImageSource.DOCUMENT_RFID, ImageSource.DOCUMENT_PRINTED);
    }

    @Test
    void testGhostType() throws Exception {
        MatchRequest matchRequest = createMatchRequest(6, PathsConfig.LIVE_PHOTO_PATH, 1, PathsConfig.PRINTED_DOCUMENT_PATH);
        MatchResponse response = PathsConfig.faceSdk.matchingApi.match(matchRequest);
        validateResponse(response, 2, ImageSource.GHOST, ImageSource.DOCUMENT_PRINTED);
    }

}




