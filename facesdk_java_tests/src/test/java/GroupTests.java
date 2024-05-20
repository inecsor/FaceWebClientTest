import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.regula.facesdk.webclient.ApiException;
import com.regula.facesdk.webclient.gen.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

class GroupTests {

    private UUID groupId;
    private Group groupResponse;
    private final String testGroupName = "test";
    private final String nameA = "Person A";
    Map<String, Object> baseMetadata = new HashMap<>() {{
        put("description", "This is a test group");
    }};

    @BeforeEach
    void setUp() {
        GroupToCreate groupToCreate = new GroupToCreate();
        groupToCreate.setName(testGroupName);
        groupToCreate.setMetadata(baseMetadata);
        groupResponse = PathsConfig.faceSdk.groupApi.createGroup(groupToCreate);
        Assertions.assertNotNull(groupResponse.getId(), "No id field in the returned group");
        groupId = groupResponse.getId();
    }


    @AfterEach
    void tearDown() {
        if (groupId != null) {
            try {
                PathsConfig.faceSdk.groupApi.deleteGroup(groupId);
            } catch (ApiException e) {
                System.out.println("Error during group deletion: " + e.getMessage());
                // Handle other necessary cleanup, if any
            }
        }
    }


    @Test
    void shouldCreateAndDeleteGroup() {
        try {
            Assertions.assertNotNull(groupResponse, "No response received");
            Assertions.assertEquals(testGroupName, groupResponse.getName(),
                    "Expected group name to be " + testGroupName +
                            ", but got " + groupResponse.getName() + " instead.");
            Assertions.assertEquals(baseMetadata.get("description"),
                    groupResponse.getMetadata().get("description"),
                    "Expected metadata description to be " + baseMetadata +
                            ", but got " + groupResponse.getMetadata() + " instead.");
        } catch (ApiException e) {
            System.err.println("ApiException occurred: " + e.getResponseBody());
        }
    }

    @Test
    void shouldGetAllGroups() {
        List<UUID> createdGroupIds = new ArrayList<>();
        createdGroupIds.add(groupId);

        try {
            // Creating additional groups
            for (int i = 1; i < 3; i++) {
                GroupToCreate additionalGroupToCreate = new GroupToCreate();
                additionalGroupToCreate.setName(testGroupName);
                additionalGroupToCreate.setMetadata(baseMetadata);
                Group additionalGroup = PathsConfig.faceSdk.groupApi.createGroup(additionalGroupToCreate);
                UUID additionalGroupId = additionalGroup.getId();
                Assertions.assertNotNull(additionalGroupId, "No id field in the returned group");
                createdGroupIds.add(additionalGroupId);
            }

            // Getting all groups for a specific page
            GroupPage response = PathsConfig.faceSdk.groupApi.getAllGroups(2, 2);

            // Assertions
            Assertions.assertNotNull(response, "No response received");
            Assertions.assertEquals(2, response.getItems().size(), "Expected 2 items on page, but got different items count");
            Assertions.assertEquals(2, response.getPage(), "Expected page to be 2, but got different page number");
        } catch (ApiException e) {
            System.err.println("ApiException occurred: " + e.getResponseBody());
            throw e;
        } finally {
            // Clean up - delete all created groups
            for (UUID id : createdGroupIds) {
                try {
                    PathsConfig.faceSdk.groupApi.deleteGroup(id);
                } catch (ApiException e) {
                    System.err.println("Failed to delete group with ID " + id + ": " + e.getResponseBody());
                }
            }
        }
    }


    @Test
    void shouldGetAllPersonsByGroupId() {
        try {
            // Create a person with metadata
            PersonFields personFields = new PersonFields();
            personFields.setName(nameA);
            personFields.setGroups(List.of(groupId));
            personFields.setMetadata(baseMetadata);

            // Create a person
            Person createdPerson = PathsConfig.faceSdk.personApi.createPerson(personFields);
            UUID personId = createdPerson.getId();

            // Get all persons by groupId
            PersonsPage personsResponse = PathsConfig.faceSdk.groupApi.getAllPersonsByGroupId(1, 1, groupId);
            Assertions.assertNotNull(personsResponse, "No response received");

            // Check if the created person is in the group
            Assertions.assertTrue(
                    Objects.requireNonNull(personsResponse.getItems()).stream()
                            .anyMatch(person -> Objects.equals(person.getId(), personId)),
                    "Created person not found in the group"
            );
            Person retrievedPerson = personsResponse.getItems().stream()
                    .filter(person -> Objects.equals(person.getId(), personId))
                    .findFirst()
                    .orElse(null);

            Assertions.assertNotNull(retrievedPerson, "Retrieved person is null");
            Assertions.assertEquals(nameA, retrievedPerson.getName(),
                    "Name of Person is incorrect");
            Assertions.assertEquals(
                    baseMetadata.get("description"),
                    retrievedPerson.getMetadata().get("description"),
                    "Metadata of the group is incorrect"
            );
            Assertions.assertTrue(
                    retrievedPerson.getGroups().contains(groupId),
                    "group_id is incorrect"
            );

        } catch (ApiException e) {
            System.err.println("ApiException occurred: " + e.getResponseBody());
            throw e;
        }
    }

    @Test
    void shouldGetAGroup() {
        try {
            // Get all groups and take the first group_id that appears
            GroupPage groupsResponse = PathsConfig.faceSdk.groupApi.getAllGroups(1, 1);
            Assertions.assertTrue(Objects.requireNonNull(groupsResponse.getItems()).size() > 0, "No groups found");
            UUID firstGroupId = groupsResponse.getItems().get(0).getId();

            // Using obtained id to get chosen group
            Group groupResponse = PathsConfig.faceSdk.groupApi.getGroup(firstGroupId);
            Assertions.assertNotNull(groupResponse, "No response received");
            Assertions.assertEquals(groupResponse.getId(), firstGroupId,
                    "group_id is incorrect");
        } catch (ApiException e) {
            System.err.println("ApiException occurred: " + e.getResponseBody());
        }
    }

    @Test
    void shouldUpdateAGroup() {
        try {
            // Updating group with a new name and metadata
            String newName = "updated_test_group";
            Map<String, Object> updatedMetadata = new HashMap<>() {{
                put("description", "updated meta");
            }};
            GroupToCreate updatedGroupToCreate = new GroupToCreate();
            updatedGroupToCreate.setName(newName);
            updatedGroupToCreate.setMetadata(updatedMetadata);
            PathsConfig.faceSdk.groupApi.updateGroup(groupId, updatedGroupToCreate);
            // Fetching the updated group and verifying the update
            Group updatedGroupResponse = PathsConfig.faceSdk.groupApi.getGroup(groupId);
            Assertions.assertEquals(newName, updatedGroupResponse.getName(),
                    "Group name wasn't updated");
            Assertions.assertEquals(updatedMetadata.get("description"),
                    updatedGroupResponse.getMetadata().get("description"),
                    "Group metadata wasn't updated");
        } catch (ApiException e) {
            System.err.println("ApiException occurred: " + e.getResponseBody());
        }
    }

    @Test
    void shouldUpdatePersonsInGroup() {
        try {
            // Creating some persons and adding them to the group
            PersonFields personFieldsA = new PersonFields();
            personFieldsA.setName(nameA);
            personFieldsA.setGroups(Collections.singletonList(groupId));
            personFieldsA.setMetadata(baseMetadata);
            PathsConfig.faceSdk.personApi.createPerson(personFieldsA);
            // Getting the group's persons
            PersonsPage initialPersonsResponse = PathsConfig.faceSdk.groupApi.getAllPersonsByGroupId(1, 10, groupId);
            List<UUID> initialPersonIds = Objects.requireNonNull(initialPersonsResponse.getItems()).stream()
                    .map(Person::getId)
                    .collect(Collectors.toList());

            // Removing them by updating group
            UpdateGroup updateGroup = new UpdateGroup();
            updateGroup.setRemoveItems(initialPersonIds);
            PathsConfig.faceSdk.groupApi.updatePersonsInGroup(groupId, updateGroup);

            // Creating new persons and adding them to the group
            List<String> newPersonNames = new ArrayList<>(List.of("New Person A", "New Person B"));
            List<UUID> newPersonIds = new ArrayList<>();
            for (String name : newPersonNames) {
                PersonFields newPersonFields = new PersonFields();
                newPersonFields.setName(name);
                newPersonFields.setGroups(Collections.singletonList(groupId));
                newPersonFields.setMetadata(baseMetadata);
                UUID newPersonId = PathsConfig.faceSdk.personApi.createPerson(newPersonFields).getId();
                newPersonIds.add(newPersonId);
            }

            // Testing that they're here
            PersonsPage updatedPersonsResponse = PathsConfig.faceSdk.groupApi.getAllPersonsByGroupId(1, 10, groupId);
            List<UUID> updatedPersonIds = new ArrayList<>();
            for (Person person : Objects.requireNonNull(updatedPersonsResponse.getItems())) {
                UUID id = person.getId();
                updatedPersonIds.add(id);
            }
            List<String> updatedPersonNames = new ArrayList<>();
            for (Person person : Objects.requireNonNull(updatedPersonsResponse.getItems())) {
                String name = person.getName();
                updatedPersonNames.add(name);
            }

            // Sort the arrays before comparing
            Collections.sort(updatedPersonIds);
            Collections.sort(newPersonIds);
            Collections.sort(updatedPersonNames);
            Collections.sort(newPersonNames);

            Assertions.assertEquals(newPersonIds, updatedPersonIds, "Updated person ids don't match");
            Assertions.assertEquals(newPersonNames, updatedPersonNames, "Updated person names don't match");
            Assertions.assertEquals(2, updatedPersonIds.size(), "Expected 2 persons in updated list but got a different number");
        } catch (ApiException e) {
            System.err.println("ApiException occurred: " + e.getMessage());
        }
    }

    @Test
    void ShouldCreateGroupWithoutMetadata() {
        GroupToCreate groupToCreate = new GroupToCreate();
        groupToCreate.setName(testGroupName);
        groupResponse = PathsConfig.faceSdk.groupApi.createGroup(groupToCreate);
        UUID newGroupId = groupResponse.getId();
        PathsConfig.faceSdk.groupApi.deleteGroup(newGroupId);
    }
}