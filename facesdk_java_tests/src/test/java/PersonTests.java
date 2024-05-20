import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.epam.reportportal.junit5.ReportPortalExtension;
import com.regula.facesdk.webclient.ApiException;
import com.regula.facesdk.webclient.gen.model.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ReportPortalExtension.class)
class PersonTests {

    private UUID groupId;
    private UUID personId;
    final String testGroupName = "test";
    private static final String nameA = "Person A";
    private static final String nameB = "Person B";
    Map<String, Object> baseMetadata = new HashMap<>() {{
        put("description", "This is a test group");
    }};
    private Person createdPerson;

    @BeforeEach
    void setUp() {
        try {
            // Create a test group
            GroupToCreate groupToCreate = new GroupToCreate();
            groupToCreate.setName(testGroupName);
            groupToCreate.setMetadata(baseMetadata);
            Group createdGroup = PathsConfig.faceSdk.groupApi.createGroup(groupToCreate);
            groupId = createdGroup.getId();
            assertNotNull(groupId, "No id field in the returned group");
            PersonFields personFields = new PersonFields();
            personFields.setName(nameA);
            personFields.setGroups(List.of(groupId));
            personFields.setMetadata(baseMetadata);

            createdPerson = PathsConfig.faceSdk.personApi.createPerson(personFields);
            personId = createdPerson.getId();
            assertNotNull(personId, "No id field in the returned person");
        } catch (ApiException e) {
            System.err.println("ApiException occurred during setup: " + e.getResponseBody());
            throw e;
        }
    }


    @AfterEach
    void tearDown() {
        // Delete the test person
        if (personId != null) {
            try {
                PathsConfig.faceSdk.personApi.deletePerson(personId);
            } catch (ApiException e) {
                System.out.println("Error during person deletion: " + e.getMessage());
            }
        }

        // Delete the test group
        if (groupId != null) {
            try {
                PathsConfig.faceSdk.groupApi.deleteGroup(groupId);
            } catch (ApiException e) {
                System.out.println("Error during group deletion: " + e.getMessage());
            }
        }
    }

    @Test
    void shouldCreatePerson() {
        try {
            // Assertions
            assertNotNull(createdPerson, "No response received");
            assertEquals(nameA, createdPerson.getName(), "The name of the person is incorrect");
            assertEquals(groupId, createdPerson.getGroups().get(0), "The groupId of the person is incorrect");
            assertEquals(baseMetadata.get("description"), createdPerson.getMetadata().get("description"), "The description of the group is incorrect");
        } catch (ApiException e) {
            System.err.println("ApiException occurred: " + e.getResponseBody());
            throw e;
        }
    }

    @Test
    void shouldGetAPerson() {
        try {
            // Get the test person
            Person retrievedPerson = PathsConfig.faceSdk.personApi.getPerson(personId);

            // Assertions
            assertNotNull(retrievedPerson, "No response received");
            assertEquals(personId, retrievedPerson.getId(), "Expected person ID to match");
        } catch (ApiException e) {
            System.err.println("ApiException occurred: " + e.getResponseBody());
            throw e;
        }
    }

    @Test
    void shouldUpdateAPerson() {
        try {
            // Update the test person
            String newDescription = "upd";
            Map<String, Object> updatedMetadata = new HashMap<>();
            updatedMetadata.put("description", newDescription);

            PersonToUpdateFields updatedPersonFields = new PersonToUpdateFields();
            updatedPersonFields.setName(nameB);
            updatedPersonFields.setMetadata(updatedMetadata);

            PathsConfig.faceSdk.personApi.updatePerson(personId, updatedPersonFields);

            // Get the updated person
            Person updatedPerson = PathsConfig.faceSdk.personApi.getPerson(personId);

            // Assertions
            assertNotNull(updatedPerson, "No response received");
            assertEquals(nameB, updatedPerson.getName(), "Person name not updated");
            assertEquals(newDescription, Objects.requireNonNull(updatedPerson.getMetadata()).get("description"), "Description not updated");
        } catch (ApiException e) {
            System.err.println("ApiException occurred: " + e.getResponseBody());
            throw e;
        }
    }

    @Test
    void shouldDeleteAPerson() {
        try {
            // Delete the test person
            PathsConfig.faceSdk.personApi.deletePerson(personId);

            // Get persons in the group and ensure the test person is deleted
            PersonsPage personsResponse = PathsConfig.faceSdk.groupApi.getAllPersonsByGroupId(1, 1, groupId);

            // Assertions
            assertNotNull(personsResponse, "No response received");
            assertTrue(Objects.requireNonNull(personsResponse.getItems()).isEmpty(), "Expected no persons in the group");
        } catch (ApiException e) {
            System.err.println("ApiException occurred: " + e.getResponseBody());
            throw e;
        }
    }

    @Test
    void testGetPersonImages() {
        try {
            byte[] imageContent = PathsConfig.readImageBytes(PathsConfig.FACE1_PATH);
            AddImageToPersonRequestImage image = new AddImageToPersonRequestImage();
            image.setContent(imageContent);
            AddImageToPersonRequest imageFields = new AddImageToPersonRequest();
            imageFields.setImage(image);
            PathsConfig.faceSdk.personApi.addImageToPerson(personId, imageFields);
            ImagePage addedImage = PathsConfig.faceSdk.personApi.getAllImagesByPersonId(1, 1, personId);
            assertNotNull(addedImage, "No response received");
            assertNotNull(addedImage.getItems().get(0).getId(), "Image ID should not be null");
            assertNotNull(addedImage.getItems().get(0).getPath(), "Image path should not be null");
        } catch (Exception e) {
            System.err.println("Exception occurred: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldGetAllGroupsAssociatedWithAPerson() {
        try {
            // Create a second test group
            GroupToCreate secondGroupToCreate = new GroupToCreate();
            secondGroupToCreate.setName("secondTestGroup");
            secondGroupToCreate.setMetadata(baseMetadata);

            Group secondGroup = PathsConfig.faceSdk.groupApi.createGroup(secondGroupToCreate);
            UUID secondGroupId = secondGroup.getId();

            // Create a new person associated with both groups
            PersonFields newPersonFields = new PersonFields();
            newPersonFields.setName(nameA);
            newPersonFields.setGroups(Arrays.asList(groupId, secondGroupId)); // Assuming groupId is a UUID
            newPersonFields.setMetadata(baseMetadata);

            Person newPerson = PathsConfig.faceSdk.personApi.createPerson(newPersonFields);
            UUID newPersonId = newPerson.getId();
            GroupPage groupsResponse;
            groupsResponse = PathsConfig.faceSdk.personApi.getAllGroupsByPersonId(1, 2, newPersonId);

            // Assertions
            assertNotNull(groupsResponse, "No response received");
            assertNotNull(groupsResponse.getItems(), "No items in the response");
            assertEquals(2, groupsResponse.getItems().size(), "Expected 2 groups, but got a different number");
            assertTrue(groupsResponse.getItems().stream().anyMatch(group -> Objects.requireNonNull(group.getId()).equals(groupId)), "The first group not found in group IDs");
            assertTrue(groupsResponse.getItems().stream().anyMatch(group -> Objects.equals(group.getId(), secondGroupId)), "The second group not found in group IDs");

            // Clean up - delete the second group
            PathsConfig.faceSdk.groupApi.deleteGroup(secondGroupId);
        } catch (ApiException e) {
            System.err.println("ApiException occurred: " + e.getResponseBody());
            throw e;
        }
    }

    @Test
    void shouldAddImageToPerson() {
        try {
            byte[] imageContent = PathsConfig.readImageBytes(PathsConfig.FACE1_PATH);
            AddImageToPersonRequestImage image = new AddImageToPersonRequestImage();
            image.setContent(imageContent);
            AddImageToPersonRequest imageFields = new AddImageToPersonRequest();
            imageFields.setImage(image);
            AddImageToPersonResponse addedImage = PathsConfig.faceSdk.personApi.addImageToPerson(personId, imageFields);
            // Assertions for the returned response
            assertNotNull(addedImage, "No response received");
            assertEquals("image/jpeg", addedImage.getContentType(), "Expected contentType to be 'image/jpeg'");
            assertNotNull(addedImage.getId(), "Image ID should not be null");

            // Get the updated person
            Person updatedPerson = PathsConfig.faceSdk.personApi.getPerson(personId);
            // Assertions
            assertNotNull(updatedPerson, "No response received");
            assertEquals(personId, updatedPerson.getId(), "Expected personId to match");
        } catch (Exception e) {
            System.err.println("Exception occurred: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }


    @Test
    void createPersonWithoutMetadata() {
        try {
            // Create a test group
            GroupToCreate groupToCreate = new GroupToCreate();
            groupToCreate.setName(testGroupName);
            Group createdGroup = PathsConfig.faceSdk.groupApi.createGroup(groupToCreate);
            UUID newGroupId = createdGroup.getId();
            assertNotNull(groupId, "No id field in the returned group");
            PersonFields personFields = new PersonFields();
            personFields.setName(nameA);
            personFields.setGroups(List.of(groupId)); // Assuming groupId is a UUID
            Person createdPerson = PathsConfig.faceSdk.personApi.createPerson(personFields);
            UUID newPersonId = createdPerson.getId();
            PathsConfig.faceSdk.personApi.deletePerson(newPersonId);
            PathsConfig.faceSdk.groupApi.deleteGroup(newGroupId);
        } catch (ApiException e) {
            System.err.println("ApiException occurred: " + e.getResponseBody());
            throw e;
        }
    }
}

