const {faceSdk, readImageBytes, ...paths} = require('./misc/pathsAndUrls');
const chai = require('chai');
const assert = chai.assert;
const fs = require('fs');


describe('Search Tests', function () {

    let baseMetadata = {'description': 'This is a test group'}
    let nameA = "Person A"
    let testGroupName = "testGroup";
    let group_id, person_id;

    function basicAssertions(response) {
        assert.ok(response.persons, "'persons' key not found in the response");
        const person_ids = response.persons.map(person => person.id);
        assert.include(person_ids, person_id, `Expected person_id ${person_id} to be in search results`);
        const person = response.persons.find(p => p.id === person_id);
        assert.isNotNull(person, `Expected person with id ${person_id} to be found`);
        assert.isNotEmpty(person.images, `Expected images for person_id ${person_id}`);
    }

    beforeEach(async function () {
        const groupToCreate = {
            name: testGroupName,
            metadata: baseMetadata
        };
        const groupResponse = await faceSdk.groupApi.createGroup(groupToCreate);
        group_id = groupResponse.id;
        const personFields = {
            name: nameA,
            groups: [group_id],
            metadata: baseMetadata
        };
        const personResponse = await faceSdk.personApi.createPerson(personFields);
        assert.ok(personResponse, "No response received");
        person_id = personResponse.id;
        const image = fs.readFileSync(paths.face3Path);
        // Add image to person
        const imageFields = {
            image: {
                content: image,
                contentType: 'image/jpeg' // Adjust the content type if necessary
            }
        };
        const addImageResponse = await faceSdk.personApi.addImageToPerson(person_id, imageFields);
        assert.isNotNull(addImageResponse.id, 'Image ID should not be null');
    });

    afterEach(async function () {
        // Delete group and person
        if (group_id) {
            await faceSdk.groupApi.deleteGroup(group_id);
        }
        if (person_id) {
            await faceSdk.personApi.deletePerson(person_id);
        }
    });

    it('should add image to person and find it via search, test limit and threshold', async function () {
        // Search for person
        const searchRequest = {
            groupIds: [group_id],
            image: {
                content: fs.readFileSync(paths.face1Path).toString('base64'),
                contentType: 'image/jpeg'
            },
            limit: 10,
            threshold: 0.8
        };
        const response = await faceSdk.searchApi.search(searchRequest);
        basicAssertions(response)
    });

    it('should add image to person and find it via search, test background and crop',
        async function () {
            // Search for person
            const searchRequest = {
                groupIds: [group_id],
                image: {
                    content: fs.readFileSync(paths.face1Path).toString('base64'),
                    contentType: 'image/jpeg'
                },
                outputImageParams: {
                    backgroundColor: [128, 128, 128],
                    crop: {type: 0, padColor: [0, 0, 0], size: [300, 400]},
                },
            };
            const response = await faceSdk.searchApi.search(searchRequest);
            basicAssertions(response)
            const person = response.persons.find((p) => p.id === person_id);
            // Assert that 'crop' key exists in person's detection
            assert.ok('crop' in person.detection, "Crop data not found in person's detection");
            // Assert that 'crop' data is not empty
            assert.ok(person.detection.crop !== '', "Crop data is empty");
        });

    it('should add image to person and find it via search with URL',
        async function () {

            const searchRequest = {
                groupIds: [group_id],
                image: {
                    imageUrl: "https://img.freepik.com/free-photo/portrait-beautiful-blond-woman-with-trendy-hairstyle_23-2149430891.jpg?t=st=1712577121~exp=1712577721~hmac=e4fb2fa9517e9bf0953bcc7eda15059d131fec68541bee602b8885f6e99bbc9b"
                },
            };
            const response = await faceSdk.searchApi.search(searchRequest);
            basicAssertions(response)
        });

    it('should add image to person and find it via search, test resize',
        async function () {

            const searchRequest = {
                groupIds: [group_id],
                image: {
                    content: fs.readFileSync(paths.face1Path).toString('base64'),
                    contentType: 'image/jpeg',
                    resizeOptions: {
                        height: 867,
                        width: 1300,
                        quality: 99
                    }
                },
            };
            const response = await faceSdk.searchApi.search(searchRequest);
            basicAssertions(response)
        });

});
