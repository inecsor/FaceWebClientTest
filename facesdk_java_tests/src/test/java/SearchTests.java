import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.regula.facesdk.webclient.ApiException;
import com.regula.facesdk.webclient.gen.model.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SearchTests {
    private UUID groupId;
    private UUID personId;
    private static final String nameA = "Person A";
    Map<String, Object> baseMetadata = new HashMap<>() {{
        put("description", "This is a test group");
    }};

    private void standardAssertions(SearchResult response, UUID personId) {
        assertNotNull(response, "No response received");
        assertNotNull(response.getPersons(), "Person list is null");
        SearchPerson person = null;
        for (SearchPerson p : response.getPersons()) {
            if (Objects.requireNonNull(p.getId()).equals(personId)) {
                person = p;
                break;
            }
        }
        assertNotNull(person, "Person ID " + personId + " not found in the persons list");
        assertNotNull(Objects.requireNonNull(person.getImages()).get(0).getPath(), "The image path for person ID " + personId + " is empty");
    }

    @BeforeEach
    void setUp() throws ApiException, IOException {
        GroupToCreate groupToCreate = new GroupToCreate();
        String testGroupName = "test";
        groupToCreate.setName(testGroupName);
        groupToCreate.setMetadata(baseMetadata);
        Group createdGroup = PathsConfig.faceSdk.groupApi.createGroup(groupToCreate);
        groupId = createdGroup.getId();
        PersonFields personFields = new PersonFields();
        personFields.setName(nameA);
        personFields.setGroups(Collections.singletonList(groupId));
        personFields.setMetadata(baseMetadata);
        Person createdPerson = PathsConfig.faceSdk.personApi.createPerson(personFields);
        personId = createdPerson.getId();
        byte[] imageContent = PathsConfig.readImageBytes(PathsConfig.FACE3_PATH);
        AddImageToPersonRequestImage image = new AddImageToPersonRequestImage();
        image.setContent(imageContent);
        image.setContentType("image/jpeg");
        AddImageToPersonRequest imageFields = new AddImageToPersonRequest();
        imageFields.setImage(image);
        PathsConfig.faceSdk.personApi.addImageToPerson(personId, imageFields);
    }


    @AfterEach
    void tearDown() throws ApiException {
        // Clean up - delete the person and group
        if (personId != null) {
            PathsConfig.faceSdk.personApi.deletePerson(personId);
        }
        if (groupId != null) {
            PathsConfig.faceSdk.groupApi.deleteGroup(groupId);
        }
    }

    @Test
    void shouldSearchWithLimitAndThreshold() {
        try {
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.setGroupIds(Arrays.asList(groupId));
            byte[] imageContent = PathsConfig.readImageBytes(PathsConfig.FACE1_PATH);
            AddImageToPersonRequestImage image = new AddImageToPersonRequestImage();
            image.setContentType("image/jpeg");
            image.setContent(imageContent);
            searchRequest.setImage(image);
            searchRequest.setLimit(10);
            searchRequest.setThreshold(0.8f);

            SearchResult response = PathsConfig.faceSdk.searchApi.search(searchRequest);
            standardAssertions(response, personId);
        } catch (ApiException e) {
            System.err.println("ApiException occurred: " + e.getResponseBody());
            throw e;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    void shouldSearchWithBackgroundAndCrop() {
        try {
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.setGroupIds(Arrays.asList(groupId));
            byte[] imageContent = PathsConfig.readImageBytes(PathsConfig.FACE1_PATH);
            AddImageToPersonRequestImage image = new AddImageToPersonRequestImage();
            image.setContentType("image/jpeg");
            image.setContent(imageContent);
            searchRequest.setImage(image);
            OutputImageParams oiParams = new OutputImageParams();
            oiParams.setBackgroundColor(Arrays.asList(128, 128, 128));
            Crop crop = new Crop();
            crop.setType(FaceImageQualityAlignType.fromValue(0));
            crop.setPadColor(Arrays.asList(0, 0, 0));
            crop.setSize(Arrays.asList(300, 400));
            oiParams.setCrop(crop);
            searchRequest.outputImageParams(oiParams);
            SearchResult response = PathsConfig.faceSdk.searchApi.search(searchRequest);
            standardAssertions(response, personId);
            SearchPerson person = response.getPersons().stream()
                    .filter(p -> p.getId().equals(personId))
                    .findFirst()
                    .orElse(null);
            assertTrue(person != null && person.getDetection() != null
                            && person.getDetection().getCrop() != null,
                    "Crop data not found in person's detection");
        } catch (ApiException e) {
            System.err.println("ApiException occurred: " + e.getResponseBody());
            throw e;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    void shouldSearchWithResize() {
        try {
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.setGroupIds(Arrays.asList(groupId));
            byte[] imageContent = PathsConfig.readImageBytes(PathsConfig.FACE1_PATH);
            AddImageToPersonRequestImage image = new AddImageToPersonRequestImage();
            image.setContentType("image/jpeg");
            image.setContent(imageContent);
            searchRequest.setImage(image);
            ResizeOptions resOpt = new ResizeOptions();
            resOpt.setHeight(867);
            resOpt.setWidth(1300);
            resOpt.setQuality(99);
            Objects.requireNonNull(searchRequest.getImage()).setResizeOptions(resOpt);
            SearchResult response = PathsConfig.faceSdk.searchApi.search(searchRequest);
            standardAssertions(response, personId);
        } catch (ApiException e) {
            System.err.println("ApiException occurred: " + e.getResponseBody());
            throw e;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    void shouldSearchWithUrl() {
        try {
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.setGroupIds(Arrays.asList(groupId));
            AddImageToPersonRequestImage image = new AddImageToPersonRequestImage();
            image.setImageUrl("https://img.freepik.com/free-photo/portrait-beautiful-blond-woman-with-trendy-hairstyle_23-2149430891.jpg?t=st=1712577121~exp=1712577721~hmac=e4fb2fa9517e9bf0953bcc7eda15059d131fec68541bee602b8885f6e99bbc9b");
            searchRequest.setImage(image);
            SearchResult response = PathsConfig.faceSdk.searchApi.search(searchRequest);

            assertNotNull(response, "No response received");
            assertTrue(Objects.requireNonNull(response.getPersons()).stream().anyMatch(person -> Objects.requireNonNull(person.getId()).equals(personId)), "Expected person not found in search results");
        } catch (ApiException e) {
            System.err.println("ApiException occurred: " + e.getResponseBody());
            throw e;
        }
    }
}
