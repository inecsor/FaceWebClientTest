namespace facesdk_cs_tests;

[TestFixture]
public class SearchTests
{
    private Guid _groupId;
    private Guid _personId;
    private const string NameA = "Person A";

    private readonly Dictionary<string, object> _baseMetadata = new Dictionary<string, object>
    {
        { "description", "This is a test group" }
    };

    private static void StandardAssertions(SearchResult response, Guid personId)
    {
        Assert.That(response, Is.Not.Null, "No response received");
        Assert.That(response.Persons, Is.Not.Null, "Person list is null");
        var person = response.Persons.FirstOrDefault(p => p.Id == personId);
        Debug.Assert(person != null, nameof(person) + " != null");
        Assert.That(person.Images[0].Path, Is.Not.Null, $"The image path for person ID {personId} is empty");
    }

    [SetUp]
    public void SetUp()
    {
        var groupToCreate = new GroupToCreate
        {
            Name = "test",
            Metadata = _baseMetadata
        };
        var createdGroup = PathsConfig.FaceSdk.GroupApi.CreateGroup(groupToCreate);
        _groupId = createdGroup.Id;

        var personFields = new PersonFields
        {
            Name = NameA,
            Groups = new List<Guid> { _groupId },
            Metadata = _baseMetadata
        };
        var createdPerson = PathsConfig.FaceSdk.PersonApi.CreatePerson(personFields);
        _personId = createdPerson.Id;

        var imageContent = PathsConfig.ReadImageBytes(PathsConfig.Face3Path);
        var image = new ImageFieldsImage
        {
            ContentType = "image/jpeg",
            Content = imageContent
        };
        var imageFields = new ImageFields { Image = image };
        PathsConfig.FaceSdk.PersonApi.AddImageToPerson(_personId, imageFields);
    }

    [TearDown]
    public void TearDown()
    {
        if (_personId != Guid.Empty)
            PathsConfig.FaceSdk.PersonApi.DeletePerson(_personId);
        if (_groupId != Guid.Empty)
            PathsConfig.FaceSdk.GroupApi.DeleteGroup(_groupId);
    }

    [Test]
    public void ShouldSearchWithLimitAndThreshold()
    {
        try
        {
            var searchRequest = new SearchRequest
            {
                GroupIds = new List<Guid> { _groupId },
                Image = new ImageFieldsImage
                {
                    ContentType = "image/jpeg",
                    Content = PathsConfig.ReadImageBytes(PathsConfig.Face1Path)
                },
                Limit = 10,
                Threshold = 0.8f
            };

            var response = PathsConfig.FaceSdk.SearchApi.Search(searchRequest);
            StandardAssertions(response, _personId);
        }
        catch (ApiException e)
        {
            Console.Error.WriteLine($"ApiException occurred: {e.ErrorCode + e.Message}");
            throw;
        }
        catch (IOException e)
        {
            throw new InvalidOperationException(e.Message, e);
        }
    }

    [Test]
    public void ShouldSearchWithBackgroundAndCrop()
    {
        try
        {
            var searchRequest = new SearchRequest
            {
                GroupIds = new List<Guid> { _groupId },
                Image = new ImageFieldsImage
                {
                    ContentType = "image/jpeg",
                    Content = PathsConfig.ReadImageBytes(PathsConfig.Face1Path)
                },
                OutputImageParams = new OutputImageParams
                {
                    BackgroundColor = new List<int> { 128, 128, 128 },
                    Crop = new Crop
                    {
                        Type = FaceImageQualityAlignType.ALIGN_1x1, // Adjust as needed
                        PadColor = new List<int> { 0, 0, 0 },
                        Size = new List<int> { 300, 400 }
                    }
                }
            };

            var response = PathsConfig.FaceSdk.SearchApi.Search(searchRequest);
            StandardAssertions(response, _personId);
            var person = response.Persons.FirstOrDefault(p => p.Id == _personId);
            Assert.IsTrue(person is { Detection.Crop: not null },
                "Crop data not found in person's detection");
        }
        catch (ApiException e)
        {
            Console.Error.WriteLine($"ApiException occurred: {e.ErrorCode + e.Message}");
            throw;
        }
        catch (IOException e)
        {
            throw new InvalidOperationException(e.Message, e);
        }
    }

    [Test]
    public void ShouldSearchWithResize()
    {
        try
        {
            var searchRequest = new SearchRequest
            {
                GroupIds = new List<Guid> { _groupId },
                Image = new ImageFieldsImage
                {
                    ContentType = "image/jpeg",
                    Content = PathsConfig.ReadImageBytes(PathsConfig.Face1Path),
                    ResizeOptions = new ResizeOptions
                    {
                        Height = 867,
                        Width = 1300,
                        Quality = 99
                    }
                }
            };

            var response = PathsConfig.FaceSdk.SearchApi.Search(searchRequest);
            List<Guid> personIds = response.Persons.Select(person => person.Id).ToList();
            foreach (var id in personIds)
            {
                TestContext.Out.WriteLine(id);
            }

            TestContext.Out.WriteLine(personIds.Count);
            StandardAssertions(response, _personId);
        }
        catch (ApiException e)
        {
            Console.Error.WriteLine($"ApiException occurred: {e.ErrorCode + e.Message}");
            throw;
        }
        catch (IOException e)
        {
            throw new InvalidOperationException(e.Message, e);
        }
    }

    [Test]
    public void ShouldSearchWithUrl()
    {
        try
        {
            var searchRequest = new SearchRequest
            {
                GroupIds = new List<Guid> { _groupId },
                Image = new ImageFieldsImage
                {
                    ImageUrl = "https://img.freepik.com/free-photo/portrait-beautiful-blond-woman-with-trendy-hairstyle_23-2149430891.jpg?t=st=1712577121~exp=1712577721~hmac=e4fb2fa9517e9bf0953bcc7eda15059d131fec68541bee602b8885f6e99bbc9b"
                }
            };

            var response = PathsConfig.FaceSdk.SearchApi.Search(searchRequest);

            Assert.That(response, Is.Not.Null, "No response received");
            List<Guid> personIds = response.Persons.Select(person => person.Id).ToList();
            foreach (var id in personIds)
            {
                TestContext.Out.WriteLine(id);
            }

            TestContext.Out.WriteLine(personIds.Count);
        }
        catch (ApiException e)
        {
            Console.Error.WriteLine($"ApiException occurred: {e.ErrorCode + e.Message}");
            throw;
        }
    }
}