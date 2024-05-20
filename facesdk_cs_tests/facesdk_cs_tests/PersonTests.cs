namespace facesdk_cs_tests;

[TestFixture]
public class PersonTests
{
    private Guid? _groupId;
    private Guid? _personId;
    private const string TestGroupName = "test";
    private const string NameA = "Person A";
    private const string NameB = "Person B";
    private Person? _createdPerson;

    private readonly Dictionary<string, object> _baseMetadata = new Dictionary<string, object>
    {
        { "description", "This is a test group" }
    };

    [SetUp]
    public void SetUp()
    {
        try
        {
            // Create a test group
            var groupToCreate = new GroupToCreate
            {
                Name = TestGroupName,
                Metadata = _baseMetadata
            };
            var createdGroup = PathsConfig.FaceSdk.GroupApi.CreateGroup(groupToCreate);
            _groupId = createdGroup.Id;
            Assert.That(_groupId, Is.Not.Null, "No id field in the returned group");

            var personFields = new PersonFields
            {
                Name = NameA,
                Groups = new List<Guid> { (Guid)_groupId },
                Metadata = _baseMetadata
            };

            _createdPerson = PathsConfig.FaceSdk.PersonApi.CreatePerson(personFields);
            _personId = _createdPerson.Id;
            Assert.That(_personId, Is.Not.Null, "No id field in the returned person");
        }
        catch (ApiException e)
        {
            Console.Error.WriteLine("ApiException occurred during setup: " + e.ErrorCode + e.Message);
            throw;
        }
    }

    [TearDown]
    public void TearDown()
    {
        // Delete the test group
        if (_groupId == Guid.Empty) return;
        {
            try
            {
                Debug.Assert(_groupId != null, nameof(_groupId) + " != null");
                PathsConfig.FaceSdk.GroupApi.DeleteGroup((Guid)_groupId);
            }
            catch (ApiException e)
            {
                Console.WriteLine("Error during group deletion: " + e.Message);
            }
        }

        // Delete the test person
        if (!_personId.HasValue) return;
        try
        {
            Debug.Assert(_personId != null, nameof(_personId) + " != null");
            PathsConfig.FaceSdk.PersonApi.DeletePerson((Guid)_personId);
        }
        catch (ApiException e)
        {
            Console.WriteLine("Error during person deletion: " + e.Message);
        }
    }

    [Test]
    public void ShouldCreateAPerson()
    {
        try
        {
            Assert.Multiple(() =>
            {
                Debug.Assert(_createdPerson != null, nameof(_createdPerson) + " != null");
                Assert.That(_createdPerson.Name, Is.EqualTo(NameA), "Expected person name to match");
                Assert.That(_createdPerson.Groups[0], Is.EqualTo(_groupId), "Expected person name to match");
                Assert.That(_createdPerson.Metadata?["description"], Is.EqualTo(_baseMetadata["description"]),
                    "The description of person is incorrect");
            });
        }
        catch (ApiException e)
        {
            Console.Error.WriteLine("ApiException occurred: " + e.ErrorCode + e.Message);
            throw;
        }
    }

    [Test]
    public void ShouldGetAPerson()
    {
        try
        {
            // Get the test person
            Debug.Assert(_personId != null, nameof(_personId) + " != null");
            var retrievedPerson = PathsConfig.FaceSdk.PersonApi.GetPerson((Guid)_personId);

            // Assertions
            Assert.That(retrievedPerson, Is.Not.Null, "No response received");
            Assert.That(retrievedPerson.Id, Is.EqualTo(_personId), "Expected person ID to match");
        }
        catch (ApiException e)
        {
            Console.Error.WriteLine("ApiException occurred: " + e.ErrorCode + e.Message);
            throw;
        }
    }

    [Test]
    public void ShouldUpdateAPerson()
    {
        try
        {
            // Update the test person
            const string newDescription = "upd";
            var updatedMetadata = new Dictionary<string, object>
            {
                { "description", newDescription }
            };

            var updatedPersonFields = new PersonFields
            {
                Name = NameB,
                Metadata = updatedMetadata
            };

            Debug.Assert(_personId != null, nameof(_personId) + " != null");
            PathsConfig.FaceSdk.PersonApi.UpdatePerson((Guid)_personId, updatedPersonFields);
            var updatedPerson = PathsConfig.FaceSdk.PersonApi.GetPerson((Guid)_personId);

            // Assertions
            Assert.That(updatedPerson.Name, Is.EqualTo(NameB), "Person name not updated");
            Assert.That(updatedPerson.Metadata?["description"], Is.EqualTo(newDescription), "Description not updated");
        }
        catch (ApiException e)
        {
            Console.Error.WriteLine("ApiException occurred: " + e.ErrorCode + e.Message);
            throw;
        }
    }

    [Test]
    public void ShouldDeleteAPerson()
    {
        try
        {
            // Delete the test person
            Debug.Assert(_personId != null, nameof(_personId) + " != null");
            PathsConfig.FaceSdk.PersonApi.DeletePerson((Guid)_personId);

            // Get persons in the group and ensure the test person is deleted
            Debug.Assert(_groupId != null, nameof(_groupId) + " != null");
            var personsResponse = PathsConfig.FaceSdk.GroupApi.GetAllPersonsByGroupId(1, 1, (Guid)_groupId);

            // Assertions
            Assert.NotNull(personsResponse, "No response received");
            Assert.IsTrue(personsResponse.Items?.Count == 0, "Expected no persons in the group");
            _personId = null;
        }
        catch (ApiException e)
        {
            Console.Error.WriteLine("ApiException occurred: " + e.ErrorCode + e.Message);
            throw;
        }
    }

    [Test]
    public void ShouldGetPersonImages()
    {
        try
        {
            var imageContent = PathsConfig.ReadImageBytes(PathsConfig.Face1Path);
            var image = new ImageFieldsImage
            {
                Content = imageContent
            };
            var imageFields = new ImageFields
            {
                Image = image
            };
            Debug.Assert(_personId != null, nameof(_personId) + " != null");
            PathsConfig.FaceSdk.PersonApi.AddImageToPerson((Guid)_personId, imageFields);
            var response =
                PathsConfig.FaceSdk.PersonApi.GetAllImagesByPersonId(page: 1, size: 1, personId: (Guid)_personId);

            // Assertions for the returned response
            Assert.That(response, Is.Not.Null, "No response received");
            Assert.That(response.Items[0].Id, Is.Not.Null, "id of photo not found");
            Assert.That(response.Items[0].Path, Is.Not.Null, "path of photo not found");
        }
        catch (Exception e)
        {
            Console.Error.WriteLine("Exception occurred: " + e.Message);
            throw;
        }
    }

    [Test]
    public void ShouldGetAllGroupsAssociatedWithAPerson()
    {
        try
        {
            // Create a second test group
            var secondGroupToCreate = new GroupToCreate
            {
                Name = "secondTestGroup",
                Metadata = _baseMetadata
            };
            var secondGroup = PathsConfig.FaceSdk.GroupApi.CreateGroup(secondGroupToCreate);
            var secondGroupId = secondGroup.Id;

            // Create a new person associated with both groups
            Debug.Assert(_groupId != null, nameof(_groupId) + " != null");
            var newPersonFields = new PersonFields
            {
                Name = NameA,
                Groups = new List<Guid> { (Guid)_groupId, secondGroupId },
                Metadata = _baseMetadata
            };
            var newPerson = PathsConfig.FaceSdk.PersonApi.CreatePerson(newPersonFields);
            var newPersonId = newPerson.Id;

            var groupsResponse = PathsConfig.FaceSdk.PersonApi.GetAllGroupsByPersonId(1, 2, newPersonId);

            // Assertions
            Assert.That(groupsResponse, Is.Not.Null, "No response received");
            Assert.That(groupsResponse.Items, Is.Not.Null, "No items in the response");
            Assert.That(groupsResponse.Items.Count, Is.EqualTo(2), "Expected 2 groups, but got a different number");
            Assert.That(groupsResponse.Items.Any(group => group.Id.Equals(_groupId)), Is.True,
                "The first group not found in group IDs");
            Assert.That(groupsResponse.Items.Any(group => group.Id.Equals(secondGroupId)), Is.True,
                "The second group not found in group IDs");

            // Clean up - delete the second group
            PathsConfig.FaceSdk.GroupApi.DeleteGroup(secondGroupId);
        }
        catch (ApiException e)
        {
            Console.Error.WriteLine("ApiException occurred: " + e.ErrorCode + e.Message);
            throw;
        }
    }

    [Test]
    public void ShouldAddImageToPerson()
    {
        try
        {
            var imageContent = PathsConfig.ReadImageBytes(PathsConfig.Face1Path);
            var image = new ImageFieldsImage
            {
                Content = imageContent
            };
            var imageFields = new ImageFields
            {
                Image = image
            };
            Debug.Assert(_personId != null, nameof(_personId) + " != null");
            var addedImage = PathsConfig.FaceSdk.PersonApi.AddImageToPerson((Guid)_personId, imageFields);

            // Assertions for the returned response

            Assert.Multiple(() =>
            {
                Assert.That(addedImage, Is.Not.Null, "No response received");
                Assert.That(addedImage.ContentType, Is.EqualTo("image/jpeg"),
                    "Expected contentType to be 'image/jpeg'");
                Assert.That(addedImage.Id, Is.Not.Null, "Image ID should not be null");
                Assert.That(addedImage.Path, Is.Not.Null, "path of photo not found");
            });

            // Get the updated person
            var updatedPerson = PathsConfig.FaceSdk.PersonApi.GetPerson((Guid)_personId);

            // Assertions
            Assert.That(updatedPerson, Is.Not.Null, "No response received");
            Assert.That(updatedPerson.Id, Is.EqualTo(_personId), "Expected personId to match");
        }
        catch (Exception e)
        {
            Console.Error.WriteLine("Exception occurred: " + e.Message);
            throw;
        }
    }

    [Test]
    public void CreatePersonWithoutMetadata()
    {
        try
        {
            // Create a test group
            var groupToCreate = new GroupToCreate
            {
                Name = TestGroupName
            };
            var createdGroup = PathsConfig.FaceSdk.GroupApi.CreateGroup(groupToCreate);
            var newGroupId = createdGroup.Id;
            Assert.NotNull(newGroupId, "No id field in the returned group");

            var personFields = new PersonFields
            {
                Name = NameA,
                Groups = new List<Guid> { newGroupId }
            };
            var createdPersonWoMeta = PathsConfig.FaceSdk.PersonApi.CreatePerson(personFields);
            var newPersonId = createdPersonWoMeta.Id;

            PathsConfig.FaceSdk.PersonApi.DeletePerson(newPersonId);
            PathsConfig.FaceSdk.GroupApi.DeleteGroup(newGroupId);
        }
        catch (ApiException e)
        {
            Console.Error.WriteLine("ApiException occurred: " + e.ErrorCode + e.Message);
            throw;
        }
    }
}