namespace facesdk_cs_tests;

[TestFixture]
public class MatchingTests
{
    private static void ValidateResponse(MatchResponse response, int expectedDetectionsCount, ImageSource expectedFirst,
        ImageSource expectedSecond = default)
    {
        Assert.That(response.Code, Is.EqualTo(FaceSDKResultCode.FACER_OK), "Unexpected response code");

        Assert.That(response.Detections, Is.Not.Null, "'detections' field should not be null");
        Assert.That(response.Detections.Count, Is.EqualTo(expectedDetectionsCount), "Incorrect number of detections");

        Assert.That(response.Results, Is.Not.Null, "'results' field should not be null");
        Assert.That(response.Results[0].First, Is.EqualTo(expectedFirst), "Unexpected 'first' value");
        if (expectedSecond != default)
        {
            Assert.That(response.Results[0].Second, Is.EqualTo(expectedSecond), "Unexpected 'second' value");
        }
    }

    private MatchRequest CreateMatchRequest(ImageSource type1, string path1, ImageSource? type2 = null,
        string? path2 = null)
    {
        var images = new List<MatchImage>();

        var imageData1 = PathsConfig.ReadImageBytes(path1);
        var image1 = new MatchImage(index: 1, type: type1, data: imageData1);
        images.Add(image1);

        if (type2 != null && path2 != null)
        {
            var imageData2 = PathsConfig.ReadImageBytes(path2);
            var image2 = new MatchImage(index: 2, type: type2, data: imageData2);
            images.Add(image2);
        }

        return new MatchRequest(tag: null, thumbnails: false, images: images);
    }


    [Test]
    public void TestMatchingApi()
    {
        var matchRequest = CreateMatchRequest(ImageSource.LIVE, PathsConfig.Face1Path, ImageSource.DOCUMENT_RFID,
            PathsConfig.Face2Path);
        var response = PathsConfig.FaceSdk.MatchingApi.Match(matchRequest);
        ValidateResponse(response, 2, ImageSource.LIVE, ImageSource.DOCUMENT_RFID);
    }

    [Test]
    public void TestLiveAndPrintedDocType()
    {
        var matchRequest = CreateMatchRequest(ImageSource.LIVE, PathsConfig.LivePhotoPath, ImageSource.DOCUMENT_PRINTED,
            PathsConfig.PrintedDocumentPath);
        var response = PathsConfig.FaceSdk.MatchingApi.Match(matchRequest);
        ValidateResponse(response, 2, ImageSource.LIVE, ImageSource.DOCUMENT_PRINTED);
    }

    [Test]
    public void TestDocWithLiveType()
    {
        var matchRequest = CreateMatchRequest(ImageSource.DOCUMENT_WITH_LIVE, PathsConfig.DocumentWithLivePath);
        var response = PathsConfig.FaceSdk.MatchingApi.Match(matchRequest);
        ValidateResponse(response, 1, ImageSource.DOCUMENT_WITH_LIVE);
    }

    [Test]
    public void TestExternalType()
    {
        var matchRequest = CreateMatchRequest(ImageSource.EXTERNAL, PathsConfig.LivePhotoPath,
            ImageSource.DOCUMENT_PRINTED, PathsConfig.PrintedDocumentPath);
        var response = PathsConfig.FaceSdk.MatchingApi.Match(matchRequest);
        ValidateResponse(response, 2, ImageSource.EXTERNAL, ImageSource.DOCUMENT_PRINTED);
    }

    [Test]
    public void TestDocumentRfidType()
    {
        var matchRequest = CreateMatchRequest(ImageSource.DOCUMENT_RFID, PathsConfig.LivePhotoPath,
            ImageSource.DOCUMENT_PRINTED, PathsConfig.PrintedDocumentPath);
        var response = PathsConfig.FaceSdk.MatchingApi.Match(matchRequest);
        ValidateResponse(response, 2, ImageSource.DOCUMENT_RFID, ImageSource.DOCUMENT_PRINTED);
    }

    [Test]
    public void TestGhostType()
    {
        var matchRequest = CreateMatchRequest(ImageSource.GHOST, PathsConfig.LivePhotoPath,
            ImageSource.DOCUMENT_PRINTED, PathsConfig.PrintedDocumentPath);
        var response = PathsConfig.FaceSdk.MatchingApi.Match(matchRequest);
        ValidateResponse(response, 2, ImageSource.GHOST, ImageSource.DOCUMENT_PRINTED);
    }
}