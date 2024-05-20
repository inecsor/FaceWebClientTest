namespace facesdk_cs_tests;

[TestFixture]
public class GroupTests
{
    private Guid? _groupId;
    private Group? _groupResponse;
    private const string TestGroupName = "test";
    private const string NameA = "Person A";

    private readonly Dictionary<string, object> _baseMetadata = new Dictionary<string, object>
    {
        { "description", "This is a test group" }
    };

    [SetUp]
    public void SetUp()
    {
        var groupToCreate = new GroupToCreate
        {
            Name = TestGroupName,
            Metadata = _baseMetadata
        };
        _groupResponse = PathsConfig.FaceSdk.GroupApi.CreateGroup(groupToCreate);
        _groupId = _groupResponse.Id;
    }

    [TearDown]
    public void TearDown()
    {
        if (!_groupId.HasValue) return;
        try
        {
            PathsConfig.FaceSdk.GroupApi.DeleteGroup(_groupId.Value);
        }
        catch (ApiException e)
        {
            Console.WriteLine("Error during group deletion: " + e.Message);
        }
    }

    [Test]
    public void ShouldCreateAndDeleteGroup()
    {
        Assert.That(_groupResponse, Is.Not.Null, "No response received");
        if (_groupResponse == null) return;
        Assert.Multiple(() =>
        {
            Assert.That(_groupResponse.Name, Is.EqualTo(TestGroupName),
                $"Expected group name to be {TestGroupName}, but got {_groupResponse.Name} instead.");
            Assert.That(_groupResponse.Metadata.TryGetValue("description", out var value),
                Is.EqualTo(_baseMetadata.TryGetValue("description", out var value1)),
                $"Expected group name to be {value1}, but got {value} instead.");
        });
    }

    [Test]
    public void ShouldGetAllGroups()
    {
        Debug.Assert(_groupId != null, nameof(_groupId) + " != null");
        var createdGroupIds = new List<Guid> { _groupId.Value };

        try
        {
            // Creating additional groups
            for (var i = 1; i < 3; i++)
            {
                var additionalGroupToCreate = new GroupToCreate
                {
                    Name = TestGroupName,
                    Metadata = _baseMetadata
                };
                var additionalGroup = PathsConfig.FaceSdk.GroupApi.CreateGroup(additionalGroupToCreate);
                var additionalGroupId = additionalGroup.Id;
                createdGroupIds.Add(additionalGroupId);
            }

            // Getting all groups for a specific page
            var response = PathsConfig.FaceSdk.GroupApi.GetAllGroups(2, 2);

            // Assertions
            Assert.That(response, Is.Not.Null, "No response received");
            Assert.That(response.Items, Is.Not.Null, "No items in the response");
            Assert.Multiple(() =>
            {
                Assert.That(response.Items, Has.Count.EqualTo(2),
                    "Expected 2 items on page, but got different items count");
                Assert.That(response.Page, Is.EqualTo(2), "Expected page to be 2, but got different page number");
            });
        }
        catch (ApiException e)
        {
            Console.Error.WriteLine("ApiException occurred: " + e.ErrorCode + e.Message);
            throw;
        }
        finally
        {
            // Clean up - delete all created groups
            foreach (var id in createdGroupIds)
            {
                try
                {
                    PathsConfig.FaceSdk.GroupApi.DeleteGroup(id);
                }
                catch (ApiException e)
                {
                    Console.Error.WriteLine($"Failed to delete group with ID {id}: {e.ErrorCode + e.Message}");
                }
            }

            _groupId = null;
        }
    }

    [Test]
    public void ShouldGetAllPersonsByGroupId()
    {
        try
        {
            // Create a person with metadata
            var personFields = new PersonFields
            {
                Name = NameA,
                Groups = new List<Guid> { _groupId!.Value },
                Metadata = _baseMetadata
            };

            // Create a person
            var createdPerson = PathsConfig.FaceSdk.PersonApi.CreatePerson(personFields);
            var personId = createdPerson.Id;

            // Get all persons by groupId
            var personsResponse = PathsConfig.FaceSdk.GroupApi.GetAllPersonsByGroupId(1, 1, _groupId.Value);
            Assert.That(personsResponse, Is.Not.Null, "No response received");
            Assert.That(personsResponse.Items.Any(person => person.Id == personId), Is.True,
                "Created person not found in the group");
            Assert.Multiple(() =>
            {
                Assert.That(personsResponse.Items.Any(person => person.Name == NameA), Is.True,
                    "Name of Person is incorrect");
                Assert.That(personsResponse.Items.Any(person => person.Groups[0] == _groupId), Is.True,
                    "GroupId of Person is incorrect");
            });
        }
        catch (ApiException e)
        {
            Console.Error.WriteLine("ApiException occurred: " + e.ErrorCode + e.Message);
            throw;
        }
    }

    [Test]
    public void ShouldGetAGroup()
    {
        try
        {
            // Get all groups and take the first group_id that appears
            var groupsResponse = PathsConfig.FaceSdk.GroupApi.GetAllGroups(1, 1);
            Assert.That(groupsResponse.Items?.Count, Is.GreaterThan(0), "No groups found");

            Debug.Assert(groupsResponse.Items != null, "groupsResponse.Items != null");
            var firstGroupId = groupsResponse.Items[0].Id;
            _groupResponse = PathsConfig.FaceSdk.GroupApi.GetGroup(firstGroupId);
            Assert.That(_groupResponse, Is.Not.Null, "No response received");
            Assert.That(_groupResponse.Id, Is.EqualTo(firstGroupId),
                $"Expected group name to be {firstGroupId}, but got {_groupResponse.Id} instead.");
        }
        catch (ApiException e)
        {
            Console.Error.WriteLine("ApiException occurred: " + e.ErrorCode + e.Message);
        }
    }

    [Test]
    public void ShouldUpdateAGroup()
    {
        try
        {
            // Updating group with a new name and metadata
            const string newName = "updated_test_group";

            Dictionary<string, object> updatedMetadata = new Dictionary<string, object>
            {
                { "description", "This is a test group" }
            };

            var updatedGroupToCreate = new GroupToCreate
            {
                Name = newName,
                Metadata = updatedMetadata
            };

            Debug.Assert(_groupId != null, nameof(_groupId) + " != null");
            PathsConfig.FaceSdk.GroupApi.UpdateGroup(_groupId.Value, updatedGroupToCreate);

            // Fetching the updated group and verifying the update
            Debug.Assert(_groupId != null, nameof(_groupId) + " != null");
            var updatedGroupResponse = PathsConfig.FaceSdk.GroupApi.GetGroup(_groupId.Value);
            Assert.That(updatedGroupResponse.Name, Is.EqualTo(newName), "Group name not updated");
            Assert.That(updatedGroupResponse.Metadata.TryGetValue("description", out var value),
                Is.EqualTo(updatedMetadata.TryGetValue("description", out var value1)),
                $"Expected group name to be {value1}, but got {value} instead.");
        }
        catch (ApiException e)
        {
            Console.Error.WriteLine("ApiException occurred: " + e.ErrorCode + e.Message);
        }
    }

    [Test]
    public void ShouldUpdatePersonsInGroup()
    {
        try
        {
            // Creating some persons and adding them to the group
            Debug.Assert(_groupId != null, nameof(_groupId) + " != null");
            var personFieldsA = new PersonFields
            {
                Name = NameA,
                Groups = new List<Guid> { _groupId.Value },
                Metadata = _baseMetadata
            };

            PathsConfig.FaceSdk.PersonApi.CreatePerson(personFieldsA);

            // Getting the group's persons
            var initialPersonsResponse = PathsConfig.FaceSdk.GroupApi.GetAllPersonsByGroupId(1, 10, _groupId.Value);
            var initialPersonIds = initialPersonsResponse.Items?.Select(person => person.Id).ToList();

            // Removing them by updating group
            var updateGroup = new UpdateGroup
            {
                RemoveItems = initialPersonIds
            };
            PathsConfig.FaceSdk.GroupApi.UpdatePersonsInGroup(_groupId.Value, updateGroup);

            // Creating new persons and adding them to the group
            var newPersonNames = new List<string> { "New Person A", "New Person B" };
            var newPersonIds = new List<Guid>();
            var index = 0;
            for (; index < newPersonNames.Count; index++)
            {
                var name = newPersonNames[index];
                var newPersonFields = new PersonFields
                {
                    Name = name,
                    Groups = new List<Guid> { _groupId.Value },
                    Metadata = _baseMetadata
                };
                var newPersonId = PathsConfig.FaceSdk.PersonApi.CreatePerson(newPersonFields).Id;
                newPersonIds.Add(newPersonId);
            }

            // Testing that they're here
            var updatedPersonsResponse = PathsConfig.FaceSdk.GroupApi.GetAllPersonsByGroupId(1, 10, _groupId.Value);
            var updatedPersonIds = updatedPersonsResponse.Items?.Select(person => person.Id).ToList();
            var updatedPersonNames = updatedPersonsResponse.Items?.Select(person => person.Name).ToList();

            // Sort the arrays before comparing
            newPersonIds.Sort();
            updatedPersonIds?.Sort();
            newPersonNames.Sort();
            updatedPersonNames?.Sort();

            Assert.That(updatedPersonIds, Is.EqualTo(newPersonIds), "Updated person ids don't match");
            Assert.That(updatedPersonNames, Is.EqualTo(newPersonNames), "Updated person names don't match");
            Assert.That(updatedPersonIds?.Count, Is.EqualTo(2),
                "Expected 2 persons in updated list but got a different number");
        }
        catch (ApiException e)
        {
            Console.Error.WriteLine("ApiException occurred: " + e.Message);
        }
    }


    [Test]
    public void ShouldCreateGroupWithoutMetadata()
    {
        var groupToCreate = new GroupToCreate
        {
            Name = TestGroupName
        };
        _groupResponse = PathsConfig.FaceSdk.GroupApi.CreateGroup(groupToCreate);
        var newGroupId = _groupResponse.Id;
        PathsConfig.FaceSdk.GroupApi.DeleteGroup(newGroupId);
    }
}