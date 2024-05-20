import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.epam.reportportal.junit5.ReportPortalExtension;
import com.regula.facesdk.webclient.gen.model.*;

@ExtendWith(ReportPortalExtension.class)
public class DetectTests {
    private byte[] face1B64;
    private byte[] sevFacesB64;

    void basicAssertions(DetectResponse response) {
        assertEquals(FaceSDKResultCode.FACER_OK, response.getCode(), "Unexpected response code");
        assertNotNull(response.getResults(), "'results' field not found in response");
        assertNotNull(response.getResults().getDetections(), "'detections' field not found in 'results'");
        assertFalse(response.getResults().getDetections().isEmpty(), "'detections' array is empty");
    }

    @BeforeEach
    void setUp() throws IOException {
        face1B64 = PathsConfig.readImageBytes(PathsConfig.FACE1_PATH);
        sevFacesB64 = PathsConfig.readImageBytes(PathsConfig.SEVERAL_FACES_IMAGE_PATH);
    }

    @Test
    void testDetectAPI() {
        DetectRequest request = new DetectRequest();
        request.setImage(face1B64);
        PathsConfig.faceSdk.matchingApi.detect(request);

    }

    @Test
    void testQualityFullScenario() {
        DetectRequest request = new DetectRequest();
        request.setImage(face1B64);
        FaceQualityScenarios scenario = FaceQualityScenarios.QUALITY_FULL;
        ProcessParam processParam = new ProcessParam();
        processParam.setScenario(scenario);
        request.setProcessParam(processParam);
        DetectResponse response = PathsConfig.faceSdk.matchingApi.detect(request);
        assertEquals(Objects.requireNonNull(response.getResults()).getScenario(), FaceQualityScenarios.QUALITY_FULL,
                "Expected scenario to be " + scenario + " but got " +
                        response.getResults().getScenario());
    }

    @Test
    void testCropAllFacesScenario() {
        DetectRequest request = new DetectRequest();
        request.setImage(sevFacesB64);
        ProcessParam processParam = new ProcessParam();
        processParam.setScenario(FaceQualityScenarios.CROP_ALL_FACES);
        processParam.setOnlyCentralFace(false);
        request.setProcessParam(processParam);
        DetectResponse response = PathsConfig.faceSdk.matchingApi.detect(request);
        basicAssertions(response);
        assertEquals(5, Objects.requireNonNull(response.getResults()).getDetections().size(), "Expected 5 detections");
    }

    @Test
    void testOnlyCentralFace() {
        DetectRequest request = new DetectRequest();
        request.setImage(sevFacesB64);
        ProcessParam processParam = new ProcessParam();
        processParam.setOnlyCentralFace(true);
        request.setProcessParam(processParam);
        DetectResponse response = PathsConfig.faceSdk.matchingApi.detect(request);
        basicAssertions(response);
        assertEquals(1, Objects.requireNonNull(response.getResults()).getDetections().size(), "Expected 1 detection");
    }

    @Test
    void testOutputImageParams() {
        // Prepare the OutputImageParams
        OutputImageParams outputImageParams = new OutputImageParams();
        outputImageParams.setBackgroundColor(Arrays.asList(128, 128, 128));
        Crop crop = new Crop();
        crop.setType(FaceImageQualityAlignType.ALIGN_1x1);
        crop.setPadColor(Arrays.asList(0, 0, 0));
        crop.setSize(Arrays.asList(300, 400));
        outputImageParams.setCrop(crop);
        ProcessParam processParam = new ProcessParam();
        processParam.setOutputImageParams(outputImageParams);
        DetectRequest request = new DetectRequest();
        request.setImage(sevFacesB64);
        request.setProcessParam(processParam);
        DetectResponse response = PathsConfig.faceSdk.matchingApi.detect(request);
        basicAssertions(response);
        assertNotNull(Objects.requireNonNull(response.getResults()).getDetections().get(0).getCrop(), "'crop' field is empty in detection");
        // You can add more specific assertions here if needed
    }

    @Test
    void testQuality() {
        QualityRequest qualityRequest = new QualityRequest();
        qualityRequest.addBackgroundMatchColorItem(128)
                .addBackgroundMatchColorItem(128)
                .addBackgroundMatchColorItem(128);

        // Create and add QualityConfig
        QualityConfig qualityConfig = new QualityConfig();
        qualityConfig.setName(FaceQualityConfigName.ROLL);
        qualityConfig.setRange(Arrays.asList(0.0f, 5.0f));
        qualityRequest.addConfigItem(qualityConfig);
        ProcessParam processParam = new ProcessParam();
        processParam.setQuality(qualityRequest);
        // Create DetectRequest with quality config
        DetectRequest request = new DetectRequest();
        request.setImage(sevFacesB64);
        request.setProcessParam(processParam);
        DetectResponse response = PathsConfig.faceSdk.matchingApi.detect(request);
        basicAssertions(response);
    }

    @Test
    void testAttributes() {
        AttributeConfig ageAttributeConfig = new AttributeConfig();
        ageAttributeConfig.setName(FaceAttribute.AGE); // Assuming FaceAttribute.AGE corresponds to "Age"

        // Initialize process parameter attributes and add the age configuration
        ProcessParamAttributes processParamAttributes = new ProcessParamAttributes();
        List<AttributeConfig> attributeConfigs = new ArrayList<>();
        attributeConfigs.add(ageAttributeConfig);
        processParamAttributes.setConfig(attributeConfigs);

        // Set up the process parameters with the attributes
        ProcessParam processParam = new ProcessParam();
        processParam.setAttributes(processParamAttributes);

        DetectRequest request = new DetectRequest();
        request.setImage(sevFacesB64);
        request.setProcessParam(processParam);
        DetectResponse response = PathsConfig.faceSdk.matchingApi.detect(request);
        basicAssertions(response);
    }
}




