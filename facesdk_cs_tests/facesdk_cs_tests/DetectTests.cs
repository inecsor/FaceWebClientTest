namespace facesdk_cs_tests;

public class DetectTests
{
    private byte[] face1B64;
    private byte[] sevFacesB64;

    private void BasicAssertions(DetectResponse response)
    {
        Assert.Multiple(() =>
        {
            Assert.That(response.Code, Is.EqualTo(FaceSDKResultCode.FACER_OK), "Unexpected response code");
            Assert.That(response.Results, Is.Not.Null, "'results' field not found in response");
            Assert.That(response.Results.Detections, Is.Not.Null, "'detections' field not found in 'results'");
            Assert.That(!response.Results.Detections.Any(), Is.False, "'detections' array is empty");
        });
    }

    [SetUp]
    public void SetUp()
    {
        face1B64 = PathsConfig.ReadImageBytes(PathsConfig.Face1Path);
        sevFacesB64 = PathsConfig.ReadImageBytes(PathsConfig.SeveralFacesImagePath);
    }

    [Test]
    public void TestDetectApi()
    {
        var request = new DetectRequest
        {
            Image = face1B64
        };
        PathsConfig.FaceSdk.MatchingApi.Detect(request);
    }

    [Test]
    public void TestQualityFullScenario()
    {
        var request = new DetectRequest
        {
            Image = face1B64
        };
        var scenario = FaceQualityScenarios.QUALITY_FULL;
        var processParam = new ProcessParam
        {
            Scenario = scenario
        };
        request.ProcessParam = processParam;
        var response = PathsConfig.FaceSdk.MatchingApi.Detect(request);
        Assert.That(response.Results?.Scenario, Is.EqualTo(scenario),
            $"Expected scenario to be {scenario} but got {response.Results?.Scenario}");
    }

    [Test]
    public void TestCropAllFacesScenario()
    {
        var request = new DetectRequest
        {
            Image = sevFacesB64
        };
        var processParam = new ProcessParam
        {
            Scenario = FaceQualityScenarios.CROP_ALL_FACES,
            OnlyCentralFace = false
        };
        request.ProcessParam = processParam;
        var response = PathsConfig.FaceSdk.MatchingApi.Detect(request);
        BasicAssertions(response);
        Assert.That(response.Results?.Detections?.Count, Is.EqualTo(5), "Expected 5 detections");
    }

    [Test]
    public void TestOnlyCentralFace()
    {
        var request = new DetectRequest
        {
            Image = sevFacesB64
        };
        var processParam = new ProcessParam
        {
            OnlyCentralFace = true
        };
        request.ProcessParam = processParam;
        var response = PathsConfig.FaceSdk.MatchingApi.Detect(request);
        BasicAssertions(response);
        Assert.That(response.Results?.Detections?.Count, Is.EqualTo(1), "Expected 1 detection");
    }

    [Test]
    public void TestOutputImageParams()
    {
        var outputImageParams = new OutputImageParams
        {
            BackgroundColor = new List<int> { 128, 128, 128 }
        };
        var crop = new Crop
        {
            Type = FaceImageQualityAlignType.ALIGN_1x1,
            PadColor = new List<int> { 0, 0, 0 },
            Size = new List<int> { 300, 400 }
        };
        outputImageParams.Crop = crop;
        var processParam = new ProcessParam
        {
            OutputImageParams = outputImageParams
        };
        var request = new DetectRequest
        {
            Image = sevFacesB64,
            ProcessParam = processParam
        };
        var response = PathsConfig.FaceSdk.MatchingApi.Detect(request);
        BasicAssertions(response);
        Assert.That(response.Results?.Detections?.FirstOrDefault()?.Crop, Is.Not.Null,
            "'crop' field is empty in detection");
    }

    [Test]
    public void TestQuality()
    {
        var qualityRequest = new QualityRequest(new List<int> { 128, 128, 128 });
        var qualityConfig = new QualityConfig
        {
            Name = FaceQualityConfigName.ROLL,
            Range = new List<float> { 0.0f, 5.0f }
        };
        qualityRequest.Config = new List<QualityConfig> { qualityConfig };
        var processParam = new ProcessParam
        {
            Quality = qualityRequest
        };
        var request = new DetectRequest
        {
            Image = sevFacesB64,
            ProcessParam = processParam
        };
        var response = PathsConfig.FaceSdk.MatchingApi.Detect(request);
        BasicAssertions(response);
    }


    [Test]
    public void TestAttributesWithAge()
    {
        var ageAttributeConfig = new AttributeConfig(FaceAttribute.AGE);
        ProcessParamAttributes attributes = new ProcessParamAttributes(new List<AttributeConfig> { ageAttributeConfig });
        var processParam = new ProcessParam
        {
            Attributes = attributes
        };
        var request = new DetectRequest
        {
            Image = sevFacesB64,
            ProcessParam = processParam
        };
        var response = PathsConfig.FaceSdk.MatchingApi.Detect(request);
        BasicAssertions(response);
    }
}
